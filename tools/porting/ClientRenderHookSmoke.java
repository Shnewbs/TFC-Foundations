/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.dries007.tfc.client.ItemRenderTint;
import net.dries007.tfc.mixin.client.accessor.ItemStackRenderStateAccessor;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Native state fixtures and real private-field descriptors; no transformed Mixin or game bootstrap. */
public final class ClientRenderHookSmoke
{
    private static int checks;
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Render hook assertion " + checks); }
    private static final class State extends ItemStackRenderState
    {
        final List<Object> identities = new ArrayList<>();
        @Override public void appendModelIdentityElement(Object value) { identities.add(value); }
    }

    public static void main(String[] args) throws Exception
    {
        final State state = new State();
        final var layersField = ItemStackRenderState.class.getDeclaredField("layers");
        final var countField = ItemStackRenderState.class.getDeclaredField("activeLayerCount");
        layersField.setAccessible(true); countField.setAccessible(true);
        for (var accessor : ItemStackRenderStateAccessor.class.getDeclaredMethods())
        {
            final var field = ItemStackRenderState.class.getDeclaredField(accessor.getAnnotation(Accessor.class).value());
            check(field.getType() == accessor.getReturnType());
            check(!java.lang.reflect.Modifier.isStatic(field.getModifiers()));
        }
        check(ItemRenderTint.firstColor((ItemStackRenderState.LayerRenderState[]) layersField.get(state), 0) == -1);
        ItemRenderTint.tintLastUncoloredLayer(state, (ItemStackRenderState.LayerRenderState[]) layersField.get(state), 0, 0xFF88CC33);
        check(state.identities.isEmpty());
        for (int frame = 0; frame < 8; frame++)
        {
            state.clear(); state.identities.clear();
            final var first = state.newLayer();
            final var last = state.newLayer();
            final var layers = (ItemStackRenderState.LayerRenderState[]) layersField.get(state);
            final int count = countField.getInt(state);
            check(count == 2 && first != last);
            check(ItemRenderTint.firstColor(layers, count) == -1);
            final int color = 0xFF88CC33 + frame;
            ItemRenderTint.tintLastUncoloredLayer(state, layers, count, color);
            check(last.tintLayers().size() == 1 && last.tintLayers().getInt(0) == color);
            check(first.tintLayers().isEmpty());
            check(state.identities.equals(List.of(color)));
            ItemRenderTint.tintLastUncoloredLayer(state, layers, count, -1);
            check(last.tintLayers().getInt(0) == color && state.identities.size() == 1);
            first.tintLayers().add(0x7FABCDEF);
            check(ItemRenderTint.firstColor(layers, count) == 0x7FABCDEF);
            state.clear();
            check(ItemRenderTint.firstColor(layers, countField.getInt(state)) == -1);
            check(first.tintLayers().isEmpty() && last.tintLayers().isEmpty());
        }
        System.out.println("PASS " + checks + " native tint-layer identity/default/non-overwrite/reuse and accessor checks; no game or transformed Mixin startup.");
    }
}
