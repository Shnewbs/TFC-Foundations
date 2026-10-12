/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.model.entity.CalendarClockModel;
import net.dries007.tfc.common.blockentities.CalendarClockBlockEntity;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.devices.CalendarClockBlock;
import net.dries007.tfc.util.Helpers;

public class CalendarClockBlockEntityRenderer implements BlockEntityRenderer<CalendarClockBlockEntity, CalendarClockBlockEntityRenderer.RenderState>
{
    public static final Identifier TEXTURE = Helpers.identifier("textures/entity/calendar_clock.png");
    public static final Identifier TEXTURE_MONTH = Helpers.identifier("textures/entity/calendar_clock_month.png");

    private final CalendarClockModel model;

    public CalendarClockBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        this.model = new CalendarClockModel(context.bakeLayer(RenderHelpers.layerId("calendar_clock")));
    }

    @Override
    public RenderState createRenderState()
    {
        return new RenderState();
    }

    @Override
    public void extractRenderState(CalendarClockBlockEntity clock, RenderState state, float partialTick,
        Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress)
    {
        BlockEntityRenderer.super.extractRenderState(clock, state, partialTick, cameraPosition, breakProgress);
        state.hasLevel = clock.getLevel() != null;
        final float[] angles = clock.getAngles();
        state.modelState = new CalendarClockModel.State(angles[0], angles[1], angles[2]);
        state.facing = clock.getBlockState().getValue(BlockStateProperties.FACING);
        state.monthMode = clock.getBlockState().getValue(TFCBlockStateProperties.CLOCK_MODE) == CalendarClockBlock.Mode.MONTH;
    }

    @Override
    public void submit(RenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera)
    {
        if (!state.hasLevel)
        {
            return;
        }

        stack.pushPose();

        stack.translate(0.5f, -0f, 0.5f);
        stack.translate(0, 1.0625f + 0.4375f, 0);
        stack.scale(-1, -1, 1);

        switch (state.facing)
        {
            case NORTH ->
            {
                stack.translate(0f, 1f, -1f);
                stack.mulPose(Axis.XP.rotationDegrees(90));
            }
            case SOUTH ->
            {
                stack.translate(0f, 1f, 1f);
                stack.mulPose(Axis.XN.rotationDegrees(90));
                stack.mulPose(Axis.YN.rotationDegrees(180));
            }
            case EAST ->
            {
                stack.translate(-1f, 1f, 0f);
                stack.mulPose(Axis.ZN.rotationDegrees(90));
                stack.mulPose(Axis.YP.rotationDegrees(90));
            }
            case WEST ->
            {
                stack.translate(1f, 1f, 0f);
                stack.mulPose(Axis.ZP.rotationDegrees(90));
                stack.mulPose(Axis.YN.rotationDegrees(90));
            }
            case DOWN ->
            {
                stack.translate(0f, 2f, 0f);
                stack.mulPose(Axis.ZP.rotationDegrees(180));
            }
        }

        collector.submitModel(model, state.modelState, stack, state.monthMode ? TEXTURE_MONTH : TEXTURE,
            state.lightCoords, OverlayTexture.NO_OVERLAY, 0, state.breakProgress);

        stack.popPose();
    }

    public static class RenderState extends BlockEntityRenderState
    {
        public boolean hasLevel;
        public CalendarClockModel.State modelState = new CalendarClockModel.State(0, 0, 0);
        public Direction facing = Direction.UP;
        public boolean monthMode;
    }
}
