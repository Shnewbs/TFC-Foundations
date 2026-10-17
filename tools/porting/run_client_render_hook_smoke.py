"""Check native client hook descriptors/anchors and real item-state tint reuse without game stubs."""
import json
import os
from pathlib import Path
import re
import shutil
import subprocess


def main():
    out = Path('port-diagnostics')
    cp = (out / 'classpath.txt').read_text().strip()
    java = Path(os.environ['JAVA_HOME']) / 'bin' if os.environ.get('JAVA_HOME') else Path('')
    classes = out / 'client-render-hook-classes'
    empty = out / 'client-render-hook-empty-sourcepath'
    shutil.rmtree(classes, ignore_errors=True)
    classes.mkdir(parents=True)
    empty.mkdir(exist_ok=True)
    sources = [
        'src/main/java/net/dries007/tfc/client/ItemRenderTint.java',
        'src/main/java/net/dries007/tfc/client/BlockTintRegistration.java',
        'src/main/java/net/dries007/tfc/mixin/client/accessor/ItemStackRenderStateAccessor.java',
        'src/main/java/net/dries007/tfc/mixin/client/BreakingItemParticleMixin.java',
        'tools/porting/ClientRenderHookSmoke.java',
    ]
    log = []
    def run(command):
        result = subprocess.run(command, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=120)
        log.append(result.stdout)
        if result.returncode:
            raise RuntimeError(result.stdout)
        return result.stdout
    run([str(java / 'javac'), '--release', '25', '-proc:none', '-classpath', cp,
         '-sourcepath', str(empty), '-d', str(classes), *sources])
    print(run([str(java / 'java'), '-ea', '-classpath', str(classes) + os.pathsep + cp, 'ClientRenderHookSmoke']).strip())
    tint_indices = set()
    def tints(value):
        if isinstance(value, dict):
            for key, child in value.items():
                if key == 'tintindex': tint_indices.add(child)
                else: tints(child)
        elif isinstance(value, list):
            for child in value: tints(child)
    for root in ('src/main/resources/assets/tfc/models', 'src/generated/resources/assets/tfc/models'):
        for path in Path(root).rglob('*.json'): tints(json.loads(path.read_text()))
    assert tint_indices == {0, 1}, tint_indices
    for path in ('soil/ConnectedGrassBlock.java', 'plant/KrummholzBlock.java'):
        assert 'SNOWY = BlockStateProperties.SNOWY;' in (Path('src/main/java/net/dries007/tfc/common/blocks') / path).read_text()
    assert 'Set.of(BlockStateProperties.SNOWY)' in Path('src/main/java/net/dries007/tfc/client/BlockTintRegistration.java').read_text()
    count = 0
    api = {}
    def inspect(name):
        if name not in api:
            api[name] = run([str(java / 'javap'), '-p', '-s', '-c', '-classpath', cp, name])
        return api[name]
    def method(name, method_name, descriptor, anchor=None):
        nonlocal count
        text = inspect(name)
        blocks = re.split(r'\n  (?=\S)', text)
        matches = [b for b in blocks if method_name + '(' in b.split('\n')[0] and 'descriptor: ' + descriptor + '\n' in b]
        assert len(matches) == 1, (name, method_name, descriptor)
        if anchor:
            assert anchor in matches[0], (name, method_name, anchor)
        count += 1
    method('net.minecraft.client.particle.BreakingItemParticle$Provider', 'createParticle',
           '(Lnet/minecraft/core/particles/ItemParticleOption;Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/util/RandomSource;)Lnet/minecraft/client/particle/Particle;')
    method('net.minecraft.client.particle.WaterDropParticle', 'tick', '()V',
           'BlockState.getCollisionShape:(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;')
    method('net.minecraft.client.renderer.block.FluidRenderer', 'isNeighborSameFluid',
           '(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/material/FluidState;)Z', 'Fluid.isSame:')
    method('net.minecraft.client.renderer.block.FluidRenderer', 'getHeight',
           '(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)F', 'Fluid.isSame:')
    method('net.minecraft.client.renderer.entity.player.AvatarRenderer', 'extractRenderState',
           '(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V', 'heldOnHead:')
    method('net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer', 'submitArmWithItem',
           '(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V')
    method('net.minecraft.client.model.monster.skeleton.SkeletonModel', 'setupAnim',
           '(Lnet/minecraft/client/renderer/entity/state/SkeletonRenderState;)V', 'SkeletonRenderState.isHoldingBow:Z')
    method('net.minecraft.client.renderer.item.CuboidItemModelWrapper', 'update',
           '(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/item/ItemModelResolver;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/world/entity/ItemOwner;I)V', 'ItemStackRenderState.newLayer:')
    method('net.minecraft.client.renderer.ItemInHandRenderer', 'renderItem',
           '(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V')
    method('net.neoforged.neoforge.client.event.RenderHandEvent', 'getSubmitNodeCollector',
           '()Lnet/minecraft/client/renderer/SubmitNodeCollector;')
    assert 'THROW_TRIDENT' in inspect('net.minecraft.client.model.HumanoidModel$ArmPose')
    assert 'private final java.util.List<net.minecraft.client.color.item.ItemTintSource> tints;' in inspect('net.minecraft.client.renderer.item.CuboidItemModelWrapper')
    config = json.loads(Path('src/main/resources/tfc.mixins.json').read_text())
    entries = config.get('mixins', []) + config.get('client', [])
    for entry in entries:
        assert (Path('src/main/java') / (config['package'] + '.' + entry).replace('.', '/')).with_suffix('.java').is_file(), entry
    assert 'client.AvatarRendererMixin' in entries and 'client.accessor.ItemStackRenderStateAccessor' in entries
    assert 'client.compat.sodium.ItemColorsMixin' not in entries
    (out / 'client-render-hook-smoke.log').write_text('\n'.join(log))
    print(f'PASS: {count} native hook method/anchor contracts, arm-pose enum, item tint field and all registered Mixin source paths. No transformed Mixin, GPU or gameplay test.')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
