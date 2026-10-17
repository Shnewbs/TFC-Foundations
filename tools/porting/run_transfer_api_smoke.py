"""Test production transaction journals and inspect native transfer/mixin contracts without game stubs."""
import os
from pathlib import Path
import shutil
import subprocess
import re


def main():
    out = Path('port-diagnostics')
    cp = (out / 'classpath.txt').read_text().strip()
    classes = out / 'transfer-api-classes'
    empty = out / 'transfer-api-empty-sourcepath'
    shutil.rmtree(classes, ignore_errors=True)
    classes.mkdir(parents=True)
    empty.mkdir(exist_ok=True)
    java = Path(os.environ['JAVA_HOME']) / 'bin'
    transfer = Path('src/main/java/net/dries007/tfc/common/capabilities/transfer')
    sources = [str(p) for p in transfer.glob('*.java') if p.name != 'TransferAccess.java']
    sources += ['src/main/java/net/dries007/tfc/common/capabilities/FluidTankCallback.java',
                'src/main/java/net/dries007/tfc/common/capabilities/InventoryFluidTank.java', 'tools/porting/TransferJournalSmoke.java']
    log = []
    def run(command):
        result = subprocess.run(command, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=120)
        log.append(result.stdout)
        if result.returncode: raise RuntimeError(result.stdout)
        return result.stdout
    run([str(java/'javac'), '--release', '25', '-proc:none', '-classpath', cp, '-sourcepath', str(empty), '-d', str(classes), *sources])
    print(run([str(java/'java'), '-ea', '-classpath', str(classes) + os.pathsep + cp, 'TransferJournalSmoke']).strip())
    stack = run([str(java/'javap'), '-p', '-s', '-classpath', cp, 'net.minecraft.world.item.ItemStack'])
    assert '(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/PatchedDataComponentMap;)V' in stack
    mixin = Path('src/main/java/net/dries007/tfc/mixin/ItemStackMixin.java').read_text()
    assert '<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/PatchedDataComponentMap;)V' in mixin
    chunk = run([str(java/'javap'), '-p', '-c', '-l', '-classpath', cp, 'net.minecraft.world.level.chunk.LevelChunk'])
    method = chunk[chunk.index('setBlockState(net.minecraft.core.BlockPos'):]
    method = method[:method.index('\n  public ', 1)]
    assert 'oldState' in method
    anchor = method.index('BlockState.is:(Ljava/lang/Object;)Z')
    assert anchor < method.index('BlockEntity.preRemoveSideEffects:') < method.index('Method removeBlockEntity:')
    display = run([str(java/'javap'), '-p', '-s', '-classpath', cp, 'net.minecraft.world.item.crafting.display.SlotDisplay$ItemStackContentsFactory'])
    assert '(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;' in display
    food = run([str(java/'javap'), '-p', '-s', '-classpath', cp, 'net.minecraft.world.food.FoodProperties'])
    assert '(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/component/Consumable;)V' in food
    entity = run([str(java/'javap'), '-p', '-s', '-classpath', cp, 'net.minecraft.world.entity.Entity'])
    assert '(Ljava/util/List;Lnet/minecraft/world/entity/InsideBlockEffectApplier$StepBasedCollector;)V' in entity
    assert '(Lnet/minecraft/tags/TagKey;)Z' in entity
    tag_loader = run([str(java/'javap'), '-p', '-s', '-classpath', cp, 'net.minecraft.tags.TagLoader'])
    assert 'tryBuildTag(' in tag_loader and '(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/List;)Lcom/mojang/datafixers/util/Either;' in tag_loader
    mining = run([str(java/'javap'), '-p', '-s', '-classpath', cp, 'net.minecraft.server.level.ServerPlayerGameMode'])
    assert '(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;ZLnet/minecraft/world/item/ItemStack;)Z' in mining
    for interface in ('IGhostBlockHandler', 'IHighlightHandler'):
        common = Path('src/main/java/net/dries007/tfc/client/' + interface + '.java').read_text()
        assert 'net.minecraft.client' not in common and 'CustomBlockOutlineRenderer' not in common
    mixins = Path('src/main/java/net/dries007/tfc/mixin')
    assert 'IPlayerInfo.get(player).eat(food)' in (mixins/'FoodPropertiesMixin.java').read_text()
    assert 'getEyeInFluidType' not in (mixins/'EntityMixin.java').read_text()
    assert Path('src/main/java/net/dries007/tfc/common/fluids/TFCFluids.java').read_text().count('.isWaterLike(true)') == 2
    climate = (mixins/'client/EnvironmentAttributeSystemMixin.java').read_text()
    assert 'EnvironmentAttributes.FOG_COLOR' in climate and 'EnvironmentAttributes.WATER_FOG_COLOR' in climate
    assert not (mixins/'client/FogRendererMixin.java').exists()
    assert 'ClientForgeEventHandler::onResourceLoadFinished' in Path('src/main/java/net/dries007/tfc/client/ClientForgeEventHandler.java').read_text()
    assert 'require = 0' not in (mixins/'TagLoaderMixin.java').read_text()
    assert 'require = 0' not in (mixins/'JsonResourceReloadMixin.java').read_text()
    # Every custom backing store reached by native providers must own a journal; sided wrappers share those owners.
    root = Path('src/main/java/net/dries007/tfc/common')
    for name, contract in {
        'capabilities/InventoryItemHandler.java': 'extends TransactionalItemStackHandler',
        'capabilities/InventoryFluidTank.java': 'transfers.beforeChange();',
        'blockentities/CrucibleBlockEntity.java': 'TransferJournal<FluidAlloy>',
        'blockentities/BlastFurnaceBlockEntity.java': 'transfer.TransactionalItemStackHandler',
        'blockentities/MoldTableBlockEntity.java': 'inventory.prepareExternalMutation();',
        'blockentities/TFCChestBlockEntity.java': 'VanillaContainerWrapper.of(first)',
    }.items(): assert contract in (root / name).read_text(), name
    saved = (transfer/'TransactionalItemStackHandler.java').read_text()
    assert 'getComponentsPatch()' in saved and 'restorePatch(patch)' in saved
    assert saved.index('original.setCount(count)') < saved.index('original.getComponents()).restorePatch(patch)')
    assert 'stack.copy()' not in saved
    for path in Path('src/main/java').rglob('*.java'):
        assert not re.search(r'Capabilities\.(?:FluidHandler|ItemHandler)\.', path.read_text()), path
    (out/'transfer-api-smoke.log').write_text('\n'.join(log))
    print('PASS native transfer compilation, storage-owner coverage and native constructor/display/removal bytecode/source anchors. No resource/game bootstrap or transformed Mixin validation.')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
