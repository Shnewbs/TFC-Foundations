/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.overworld;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;
import net.neoforged.neoforge.client.CustomWeatherEffectRenderer;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.client.ClimateRenderCache;
import net.dries007.tfc.client.render.blockentity.CapturedGeometry;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.IBlockRain;
import net.dries007.tfc.common.blocks.devices.CharcoalForgeBlock;
import net.dries007.tfc.common.blocks.devices.FirepitBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateModel;
import net.dries007.tfc.util.tracker.WeatherHelpers;

import static net.dries007.tfc.client.overworld.SkyGeometry.*;

/** Native environment renderers. World access is restricted to extraction and rain ticks. */

public final class LevelRendererExtension implements CustomSkyboxRenderer, CustomWeatherEffectRenderer
{
    public static final LevelRendererExtension INSTANCE = new LevelRendererExtension();
    public static void registerPipelines(net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent event)
    {
        event.registerPipeline(COLORED_STARS);
        event.registerPipeline(COVER);
    }

    public static final Identifier EFFECTS = Helpers.identifier("overworld");
    private static final ContextKey<Frame> FRAME = new ContextKey<>(Helpers.identifier("overworld_frame"));

    private record WeatherBatch(Identifier texture, CapturedGeometry.Snapshot geometry) {}
    private record Frame(SkyPos sun, SkyPos moon, SkyPos stars, int phase, float starBrightness,
        CapturedGeometry.Snapshot catalog, List<WeatherBatch> weather) {}

    private static final RenderPipeline COLORED_STARS = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
        .withLocation(Helpers.identifier("pipeline/colored_stars"))
        .withVertexShader("core/position_color").withFragmentShader("core/position_color")
        .withColorTargetState(new ColorTargetState(BlendFunction.OVERLAY))
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS).build();
    private static final RenderPipeline COVER = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
        .withLocation(Helpers.identifier("pipeline/celestial_cover"))
        .withVertexShader("core/position_color").withFragmentShader("core/position_color")
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS).build();
    private static final Identifier[] RAIN_LOCATIONS = new Identifier[] {
        Helpers.identifier("textures/environment/rain_0.png"),
        Helpers.identifier("textures/environment/rain_1.png"),
        Helpers.identifier("textures/environment/rain_2.png"),
        Helpers.identifier("textures/environment/rain_3.png"),
    };

    private static final Identifier[] SNOW_LOCATIONS = new Identifier[] {
        Helpers.identifier("textures/environment/snow_0.png"),
        Helpers.identifier("textures/environment/snow_1.png"),
        Helpers.identifier("textures/environment/snow_2.png"),
        Helpers.identifier("textures/environment/snow_3.png"),
    };
    private static final float RAIN_MAX_ANGLE = 20 * Mth.DEG_TO_RAD;
    private static final float SNOW_MAX_ANGLE = 30 * Mth.DEG_TO_RAD;

    private final float[] rainSizeX = new float[1024];
    private final float[] rainSizeZ = new float[1024];
    private int rainSoundTime;
    private volatile CapturedGeometry.Snapshot catalog = new CapturedGeometry.Snapshot(List.of());
    private @Nullable CapturedGeometry.Snapshot uploadedCatalog;
    private @Nullable GpuBuffer starBuffer;
    private int starIndexCount;

    private LevelRendererExtension()
    {
        // Native WeatherEffectRenderer's column directions, without reflection or GPU work at class initialization.
        for (int z = 0; z < 32; z++) for (int x = 0; x < 32; x++)
        {
            final float dx = x - 16, dz = z - 16;
            final float length = Mth.sqrt(dx * dx + dz * dz);
            rainSizeX[z * 32 + x] = length == 0 ? 0 : -dz / length;
            rainSizeZ[z * 32 + x] = length == 0 ? 0 : dx / length;
        }
    }

    public void updateStars(List<Star> stars)
    {
        final CapturedGeometry vertices = new CapturedGeometry();
        final RandomSource random = RandomSource.create(42L);
        for (Star star : stars) drawStar(vertices, star, random);
        // Reload workers publish CPU values only. GPU replacement happens on the render thread.
        catalog = vertices.snapshot();
    }

    public static void extract(ExtractLevelRenderStateEvent event)
    {
        final ClientLevel level = event.getLevel();
        final LevelRenderState state = event.getRenderState();
        // Match the former OVERWORLD_EFFECTS registration; other skybox types keep their own renderers.
        if (level.dimensionType().skybox() != DimensionType.Skybox.OVERWORLD) return;
        final Camera camera = event.getCamera();
        final float partialTick = event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        final BlockPos pos = camera.blockPosition();
        final float stars = camera.attributeProbe().getValue(EnvironmentAttributes.STAR_BRIGHTNESS, partialTick);
        state.setRenderData(FRAME, new Frame(
            ClientSolarCalculatorBridge.getSunPosition(level, pos),
            ClientSolarCalculatorBridge.getMoonPosition(level, pos),
            ClientSolarCalculatorBridge.getStarPosition(level, pos),
            ClientSolarCalculatorBridge.getMoonPhase(), stars, INSTANCE.catalog,
            INSTANCE.captureWeather(level, event.getRenderTick(), partialTick, camera.position())));
    }

    @Override
    public boolean renderSky(LevelRenderState levelState, SkyRenderState sky, Matrix4fc modelView, Runnable setupFog)
    {
        final Frame frame = levelState.getRenderData(FRAME);
        if (frame == null) return false;
        setupFog.run();
        final PoseStack stack = new PoseStack();
        stack.mulPose(new Matrix4f(modelView));
        draw(skyDisc(16), stack.last().pose(), ARGB.vector4fFromARGB32(0xFF000000 | sky.skyColor), RenderPipelines.SKY, null, false);

        final float sunriseAlpha = ARGB.alphaFloat(sky.sunriseAndSunsetColor);
        if (sunriseAlpha > 0)
        {
            final CapturedGeometry fan = new CapturedGeometry();
            final Matrix4f pose = rotateTo(stack, SkyPos.of(-Mth.HALF_PI, frame.sun().azimuth() + Mth.PI));
            final int rgb = sky.sunriseAndSunsetColor & 0xFFFFFF;
            fan.addVertex(pose, 0, 100, 0).setColor(sky.sunriseAndSunsetColor);
            for (int i = 0; i <= 16; i++)
            {
                final float angle = i * Mth.TWO_PI / 16f;
                fan.addVertex(pose, Mth.sin(angle) * 120, Mth.cos(angle) * 120, -Mth.cos(angle) * 40 * sunriseAlpha).setColor(rgb);
            }
            draw(fan.snapshot(), new Matrix4f(), new Vector4f(1), RenderPipelines.SUNRISE_SUNSET, null, false);
        }

        if (uploadedCatalog != frame.catalog() && frame.catalog().vertices().isEmpty() && starBuffer != null)
        {
            starBuffer.close();
            starBuffer = null;
            uploadedCatalog = frame.catalog();
        }
        final float nightAlpha = frame.starBrightness() * sky.rainBrightness;
        if (nightAlpha > 0 && !frame.catalog().vertices().isEmpty())
        {
            if (uploadedCatalog != frame.catalog())
            {
                final GpuBuffer replacement;
                try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(frame.catalog().vertices().size() * DefaultVertexFormat.POSITION_COLOR.getVertexSize()))
                {
                    final BufferBuilder buffer = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                    frame.catalog().draw(new PoseStack().last(), buffer);
                    try (MeshData mesh = buffer.buildOrThrow())
                    {
                        replacement = RenderSystem.getDevice().createBuffer(() -> "TFC colored star catalog", GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
                        starIndexCount = mesh.drawState().indexCount();
                    }
                }
                if (starBuffer != null) starBuffer.close();
                starBuffer = replacement;
                uploadedCatalog = frame.catalog();
            }
            drawBuffer(starBuffer, starIndexCount, frame.catalog().vertices().size(),
                rotateSkyTo(stack, frame.stars()), new Vector4f(nightAlpha), COLORED_STARS, null, false);
        }
        if (sky.shouldRenderDarkDisc)
        {
            stack.pushPose();
            stack.translate(0, 12, 0);
            draw(skyDisc(-16), stack.last().pose(), new Vector4f(0, 0, 0, 1), RenderPipelines.SKY, null, false);
            stack.popPose();
        }

        final Matrix4f sun = rotateTo(stack, frame.sun());
        final Matrix4f moon = rotateTo(stack, frame.moon());
        final CapturedGeometry cover = new CapturedGeometry();
        for (Matrix4f pose : List.of(sun, moon))
        {
            cover.addVertex(pose, -7.5f, 100, -7.5f).setColor(0xFF000000 | sky.skyColor);
            cover.addVertex(pose, 7.5f, 100, -7.5f).setColor(0xFF000000 | sky.skyColor);
            cover.addVertex(pose, 7.5f, 100, 7.5f).setColor(0xFF000000 | sky.skyColor);
            cover.addVertex(pose, -7.5f, 100, 7.5f).setColor(0xFF000000 | sky.skyColor);
        }
        draw(cover.snapshot(), new Matrix4f(), new Vector4f(1), COVER, null, false);
        final TextureAtlas atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.CELESTIALS);
        draw(celestial(atlas.getSprite(Identifier.withDefaultNamespace("sun")), false), sun,
            new Vector4f(1, 1, 1, sky.rainBrightness), RenderPipelines.CELESTIAL, atlas, false);
        final MoonPhase phase = MoonPhase.values()[Math.floorMod(frame.phase(), MoonPhase.COUNT)];
        draw(celestial(atlas.getSprite(Identifier.withDefaultNamespace("moon/" + phase.getSerializedName())), true), moon,
            new Vector4f(1, 1, 1, (0.2f + 0.8f * frame.starBrightness()) * sky.rainBrightness), RenderPipelines.CELESTIAL, atlas, false);
        return true;
    }

    /** Only native GPU handles, textures and captured vertices are used during drawing. */
    private static void draw(CapturedGeometry.Snapshot geometry, Matrix4f matrix, Vector4f color,
        RenderPipeline pipeline, @Nullable AbstractTexture texture, boolean weather)
    {
        if (geometry.vertices().isEmpty()) return;
        final GpuBuffer vertexBuffer;
        final int indexCount;
        final int vertexCount;
        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(geometry.vertices().size() * pipeline.getVertexFormat().getVertexSize()))
        {
            final BufferBuilder buffer = new BufferBuilder(bytes, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
            geometry.draw(new PoseStack().last(), buffer);
            try (MeshData mesh = buffer.buildOrThrow())
            {
                vertexBuffer = pipeline.getVertexFormat().uploadImmediateVertexBuffer(mesh.vertexBuffer());
                indexCount = mesh.drawState().indexCount();
                vertexCount = mesh.drawState().vertexCount();
            }
        }
        drawBuffer(vertexBuffer, indexCount, vertexCount, matrix, color, pipeline, texture, weather);
    }

    private static void drawBuffer(GpuBuffer vertexBuffer, int indexCount, int vertexCount, Matrix4f matrix,
        Vector4f color, RenderPipeline pipeline, @Nullable AbstractTexture texture, boolean weather)
    {
        final var transforms = RenderSystem.getDynamicUniforms().writeTransform(matrix, color, new Vector3f(), new Matrix4f());
        final RenderTarget target = weather ? OutputTarget.WEATHER_TARGET.getRenderTarget() : Minecraft.getInstance().getMainRenderTarget();
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
            () -> "TFC environment", target.getColorTextureView(), OptionalInt.empty(), target.getDepthTextureView(), OptionalDouble.empty()))
        {
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setVertexBuffer(0, vertexBuffer);
            if (texture != null) pass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
            if (weather) pass.bindTexture("Sampler2", Minecraft.getInstance().gameRenderer.lightmap(),
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
            if (pipeline.getVertexFormatMode() == VertexFormat.Mode.QUADS)
            {
                final var indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
                pass.setIndexBuffer(indices.getBuffer(indexCount), indices.type());
                pass.drawIndexed(0, 0, indexCount, 1);
            }
            else pass.draw(0, vertexCount);
        }
    }

    @Override
    public boolean renderSnowAndRain(LevelRenderState state, WeatherRenderState weather, MultiBufferSource buffers, Vec3 camPos)
    {
        final Frame frame = state.getRenderData(FRAME);
        if (frame == null) return false;
        final RenderPipeline pipeline = Minecraft.useShaderTransparency() ? RenderPipelines.WEATHER_DEPTH_WRITE : RenderPipelines.WEATHER_NO_DEPTH_WRITE;
        for (WeatherBatch batch : frame.weather())
            draw(batch.geometry(), new Matrix4f(RenderSystem.getModelViewMatrix()), new Vector4f(1), pipeline,
                Minecraft.getInstance().getTextureManager().getTexture(batch.texture()), true);
        return true;
    }

    private List<WeatherBatch> captureWeather(ClientLevel level, int ticks, float partialTick, Vec3 camera)
    {
        final List<WeatherBatch> batches = new ArrayList<>();
        Identifier texture = RAIN_LOCATIONS[0];
        final float rainLevel = ClimateRenderCache.INSTANCE.getRainLevel(partialTick);

        if (rainLevel > 0)
        {

            final float camX = (float) camera.x;
            final float camY = (float) camera.y;
            final float camZ = (float) camera.z;

            final int blockX = Mth.floor(camX);
            final int blockY = Mth.floor(camY);
            final int blockZ = Mth.floor(camZ);

            final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

            cursor.set(blockX, blockY, blockZ);

            final ClimateModel model = Climate.get(level);
            final long calendarTick = Calendars.get(level).getCalendarTicks();
            final float climateRain = model.getRain(calendarTick);
            final float climateRainfall = model.getInstantRainfall(level, cursor);
            final float rainIntensity = WeatherHelpers.calculateRealRainIntensity(climateRain, climateRainfall);
            final float currentTick = ticks + partialTick;

            CapturedGeometry buffer = null;

            // Vanilla switches between 5 and 10 based on if fancy graphics is enabled or not. We want to gradually increase the amount
            // in more intense precipitation, starting at 5 for most storms (but also applying other effects for lighter precipitation)
            //
            // However, we still limit ourselves if the user is not using fancy graphics (boo!)
            final int blockRadius = ClientHelpers.useFancyGraphics()
                ? (int) Mth.map(rainIntensity, 0f, 1f, 5, 15)
                : 5;

            int stateFlag = -1;

            Vec2 wind = ClimateRenderCache.INSTANCE.getWind();

            // max angle is at ~50 kmh

            final float xAngleRain = Mth.TWO_PI / (360f / Mth.clampedMap(wind.x, -0.4f, 0.4f, -RAIN_MAX_ANGLE, RAIN_MAX_ANGLE));
            final float zAngleRain = Mth.TWO_PI / (360f / Mth.clampedMap(wind.y, -0.4f, 0.4f, -RAIN_MAX_ANGLE, RAIN_MAX_ANGLE));
            final float xAngleSnow = Mth.TWO_PI / (360f / Mth.clampedMap(wind.x, -0.4f, 0.4f, -SNOW_MAX_ANGLE, SNOW_MAX_ANGLE));
            final float zAngleSnow = Mth.TWO_PI / (360f / Mth.clampedMap(wind.y, -0.4f, 0.4f, -SNOW_MAX_ANGLE, SNOW_MAX_ANGLE));

            final float defaultXOffsetRain = (float) (Math.tan(xAngleRain * Mth.RAD_TO_DEG) * 10);
            final float defaultZOffsetRain = (float) (Math.tan(zAngleRain * Mth.RAD_TO_DEG) * 10);

            final float defaultXOffsetSnow = (float) (Math.tan(xAngleSnow * Mth.RAD_TO_DEG) * 10);
            final float defaultZOffsetSnow = (float) (Math.tan(zAngleSnow * Mth.RAD_TO_DEG) * 10);

            for (int z = blockZ - blockRadius; z <= blockZ + blockRadius; z++)
            {
                for (int x = blockX - blockRadius; x <= blockX + blockRadius; x++)
                {
                    final int rainSizeIndex = (z - blockZ + 16) * 32 + x - blockX + 16;
                    final float rainSizeX = this.rainSizeX[rainSizeIndex] * 0.5f;
                    final float rainSizeZ = this.rainSizeZ[rainSizeIndex] * 0.5f;

                    cursor.set(x, camY, z);

                    // Modified: biomes always have precipitation, we don't need to query the biome for this
                    int yHeight = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                    int minY = blockY - blockRadius;
                    int maxY = blockY + blockRadius;
                    if (minY < yHeight)
                    {
                        minY = yHeight;
                    }

                    if (maxY < yHeight)
                    {
                        maxY = yHeight;
                    }

                    final int y = Math.max(yHeight, blockY);
                    if (minY != maxY)
                    {
                        final RandomSource random = RandomSource.create(x * x * 3121L + x * 45238971L ^ z * z * 418711L + z * 13761L);
                        cursor.set(x, minY, z);

                        if (model.getInstantTemperature(level, cursor) > 0) // If positive temperature, then raining
                        {
                            if (stateFlag != 0)
                            {
                                if (stateFlag >= 0)
                                {
                                    batches.add(new WeatherBatch(texture, buffer.snapshot()));
                                }

                                stateFlag = 0;

                                // select the texture based on the amount of rain
                                texture = RAIN_LOCATIONS[Mth.clamp(Mth.floor(rainIntensity * 4.0f), 0, 3)];

                                buffer = new CapturedGeometry();
                            }

                            // Mojang magic
                            int i3 = ticks & 131071;
                            int j3 = x * x * 3121 + x * 45238971 + z * z * 418711 + z * 13761 & 0xFF;
                            float f2 = 3.0F + random.nextFloat();
                            float f3 = -((i3 + j3) + partialTick) / 32.0F * f2;
                            float v = f3 % 32.0F;
                            double d2 = x + 0.5 - camX;
                            double d3 = z + 0.5 - camZ;

                            // Vanilla calculates the alpha based on the rain level, which is reasonable as it allows rain to fade in and out.
                            // We additionally use the rain intensity, which causes rain to be milder at low intensity rainfall
                            //
                            // Note we also fix a bug here by clamping, so alpha does not go negative, which causes random non-alpha rainfall on
                            // far-away blocks. This affects us more as we have more distant rainfall (15 vs. 10 max in vanilla)
                            float f6 = (float) Math.sqrt(d2 * d2 + d3 * d3) / blockRadius;
                            final float alpha = Mth.clamp((1.0F - f6 * f6) * 0.5F + 0.5F, 0f, 1f) * rainLevel;

                            float height = maxY - minY;

                            float xOffset = defaultXOffsetRain;
                            float zOffset = defaultZOffsetRain;
                            if (height != 10)
                            {
                                // avoid extra math for most of these quads
                                zOffset = (float) (Math.tan(zAngleRain * Mth.RAD_TO_DEG) * height);
                                xOffset = (float) (Math.tan(xAngleRain * Mth.RAD_TO_DEG) * height);
                            }

                            cursor.set(x, y, z);

                            final int light = LevelRenderer.getLightCoords(level, cursor);

                            Vector3f vert0 = new Vector3f(x - camX - rainSizeX + 0.5f, maxY - camY, z - camZ - rainSizeZ + 0.5f);
                            Vector3f vert1 = new Vector3f(x - camX + rainSizeX + 0.5f, maxY - camY, z - camZ + rainSizeZ + 0.5f);
                            Vector3f vert2 = new Vector3f(x - camX + rainSizeX + 0.5f, minY - camY, z - camZ + rainSizeZ + 0.5f);
                            Vector3f vert3 = new Vector3f(x - camX - rainSizeX + 0.5f, minY - camY, z - camZ - rainSizeZ + 0.5f);

                            vert0 = vert0.add(-xOffset / 2, 0, -zOffset / 2);
                            vert1 = vert1.add(-xOffset / 2, 0, -zOffset / 2);
                            vert2 = vert2.add(xOffset / 2, 0, zOffset / 2);
                            vert3 = vert3.add(xOffset / 2, 0, zOffset / 2);

                            buffer.addVertex(vert0)
                                .setUv(0.0F, minY * 0.25F + v)
                                .setColor(1.0F, 1.0F, 1.0F, alpha)
                                .setLight(light);

                            buffer.addVertex(vert1)
                                .setUv(1.0F, minY * 0.25F + v)
                                .setColor(1.0F, 1.0F, 1.0F, alpha)
                                .setLight(light);

                            buffer.addVertex(vert2)
                                .setUv(1.0F, maxY * 0.25F + v)
                                .setColor(1.0F, 1.0F, 1.0F, alpha)
                                .setLight(light);

                            buffer.addVertex(vert3)
                                .setUv(0.0F, maxY * 0.25F + v)
                                .setColor(1.0F, 1.0F, 1.0F, alpha)
                                .setLight(light);

                        }
                        else
                        {
                            // Otherwise, snow
                            if (stateFlag != 1)
                            {
                                if (stateFlag >= 0)
                                {
                                    batches.add(new WeatherBatch(texture, buffer.snapshot()));
                                }

                                stateFlag = 1;

                                texture = SNOW_LOCATIONS[Mth.clamp(Mth.floor(rainIntensity * 4.0f), 0, 3)];

                                buffer = new CapturedGeometry();
                            }

                            float speed = Mth.clampedMap(wind.length(), 0, 0.7f, 1, 16);
                            // Mojang magic
                            float f8 = -((float) (ticks & 511) + partialTick) / (512f / speed);
                            float f9 = (float) (random.nextDouble() + currentTick * 0.01 * random.nextGaussian());
                            float f10 = (float) (random.nextDouble() + (currentTick * random.nextGaussian()) * 0.001);
                            double d4 = (double) x + 0.5 - camX;
                            double d5 = (double) z + 0.5 - camZ;
                            float f11 = (float) Math.sqrt(d4 * d4 + d5 * d5) / (float) blockRadius;
                            final float alpha = ((1.0F - f11 * f11) * 0.3F + 0.5F) * rainLevel;
                            cursor.set(x, y, z);

                            // A hack from Mojang to make snow not turn dark in low-light environments
                            final int light = LevelRenderer.getLightCoords(level, cursor);
                            final int lightV = light >> 16 & 65535;
                            final int lightU = light & 65535;
                            final int brightLightV = (lightV * 3 + 240) / 4;
                            final int brightLightU = (lightU * 3 + 240) / 4;

                            float height = maxY - minY;

                            // avoid extra math for most of these quads
                            float xoffset = defaultXOffsetSnow;
                            float zoffset = defaultZOffsetSnow;
                            if (height != 10)
                            {
                                xoffset = (float) (Math.tan(xAngleSnow * Mth.RAD_TO_DEG) * height);
                                zoffset = (float) (Math.tan(zAngleSnow * Mth.DEG_TO_RAD) * height);
                            }

                            cursor.set(x, y, z);

                            Vector3f vert0 = new Vector3f(x - camX - rainSizeX + 0.5f, maxY - camY, z - camZ - rainSizeZ + 0.5f);
                            Vector3f vert1 = new Vector3f(x - camX + rainSizeX + 0.5f, maxY - camY, z - camZ + rainSizeZ + 0.5f);
                            Vector3f vert2 = new Vector3f(x - camX + rainSizeX + 0.5f, minY - camY, z - camZ + rainSizeZ + 0.5f);
                            Vector3f vert3 = new Vector3f(x - camX - rainSizeX + 0.5f, minY - camY, z - camZ - rainSizeZ + 0.5f);

                            vert0 = vert0.add(-xoffset / 2, 0, -zoffset / 2);
                            vert1 = vert1.add(-xoffset / 2, 0, -zoffset / 2);
                            vert2 = vert2.add(xoffset / 2, 0, zoffset / 2);
                            vert3 = vert3.add(xoffset / 2, 0, zoffset / 2);

                            buffer.addVertex(vert0)
                                .setUv(0.0F + f9, minY * 0.25F + f8 + f10)
                                .setColor(1.0F, 1.0F, 1.0F, alpha)
                                .setUv2(brightLightU, brightLightV);
                            buffer.addVertex(vert1)
                                .setUv(1.0F + f9, minY * 0.25F + f8 + f10)
                                .setColor(1.0F, 1.0F, 1.0F, alpha)
                                .setUv2(brightLightU, brightLightV);
                            buffer.addVertex(vert2)
                                .setUv(1.0F + f9, maxY * 0.25F + f8 + f10)
                                .setColor(1.0F, 1.0F, 1.0F, alpha)
                                .setUv2(brightLightU, brightLightV);
                            buffer.addVertex(vert3)
                                .setUv(0.0F + f9, maxY * 0.25F + f8 + f10)
                                .setColor(1.0F, 1.0F, 1.0F, alpha)
                                .setUv2(brightLightU, brightLightV);
                        }
                    }
                }
            }

            if (stateFlag >= 0)
            {
                batches.add(new WeatherBatch(texture, buffer.snapshot()));
            }

        }
        return List.copyOf(batches);
    }

    @Override
    public boolean tickRain(ClientLevel level, int ticks, Camera camera)
    {
        final Minecraft minecraft = Minecraft.getInstance();
        final float rainLevel = ClimateRenderCache.INSTANCE.getRainLevel(0f);
        final boolean isSnowing = ClimateRenderCache.INSTANCE.getInstantTemperature() < 0;
        if (rainLevel > 0.0F)
        {
            final RandomSource random = RandomSource.create(ticks * 312987231L);
            final BlockPos cameraPos = BlockPos.containing(camera.position());
            final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

            // Calculate rainfall via the climate
            final float rainIntensity = calculateRainIntensity(level, cameraPos);

            // Include all factors that vanilla does (fancy graphics, particle settings, rain level), but also include rain intensity
            final float adjustedRainIntensity = rainLevel
                * Mth.clampedMap(rainIntensity, 0, 0.3f, 0, 1)
                * (ClientHelpers.useFancyGraphics() ? 1f : 0.5f)
                * (minecraft.options.particles().get() == ParticleStatus.DECREASED ? 0.7f : 1f);
            final int particleAmount = isSnowing ? 0 : (int) (100f * adjustedRainIntensity * adjustedRainIntensity);

            // Modification from vanilla, since we are using a cursor, we track if we added any particles rather than checking
            // if the position is not null, so we can make sounds for them
            boolean addedAnyParticles = false;

            if (!isSnowing) // If snowing no particles/sound
            {
                for (int n = 0; n < particleAmount; n++)
                {
                    final int dx = random.nextInt(21) - 10;
                    final int dz = random.nextInt(21) - 10;

                    cursor.setWithOffset(cameraPos, dx, 0, dz);
                    cursor.setY(level.getHeight(Heightmap.Types.MOTION_BLOCKING, cursor.getX(), cursor.getZ()));

                    if (cursor.getY() > level.getMinY() && cursor.getY() <= cameraPos.getY() + 10 && cursor.getY() >= cameraPos.getY() - 10)
                    {
                        cursor.move(Direction.DOWN);
                        addedAnyParticles = true;

                        // Don't check the biome, as all overworld biomes should support rain, and we checked that above

                        // With minimal particle options, we still search for a position (as we use it to determine if we need to play rain sounds), but we
                        // stop at the first one, before spawning any particles for it.
                        if (minecraft.options.particles().get() == ParticleStatus.MINIMAL)
                        {
                            break;
                        }

                        final double offsetX = random.nextDouble();
                        final double offsetZ = random.nextDouble();
                        final BlockState state = level.getBlockState(cursor);
                        final FluidState fluid = level.getFluidState(cursor);

                        // Handle `IBlockRain`, which needs to pretend the block is a solid block.
                        final VoxelShape shape = state.getBlock() instanceof IBlockRain
                            ? Shapes.block()
                            : state.getCollisionShape(level, cursor);
                        final double offsetY = Math.max(
                            shape.max(Direction.Axis.Y, offsetX, offsetZ),
                            fluid.getHeight(level, cursor)
                        );

                        final ParticleOptions options = !fluid.is(FluidTags.LAVA)
                            && !Helpers.isBlock(state, TFCTags.Blocks.SMOKES_IN_RAIN)
                            && !CampfireBlock.isLitCampfire(state)
                            && !(Helpers.isBlock(state, TFCBlocks.FIREPIT.get()) && state.getValue(FirepitBlock.LIT))
                            && !(Helpers.isBlock(state, TFCBlocks.CHARCOAL_FORGE.get()) && state.getValue(CharcoalForgeBlock.HEAT) > 0)
                            ? ParticleTypes.RAIN
                            : ParticleTypes.SMOKE;
                        level.addParticle(options, cursor.getX() + offsetX, cursor.getY() + offsetY, cursor.getZ() + offsetZ, 0.0, 0.0, 0.0);
                    }
                }
            }

            if (!isSnowing && addedAnyParticles && random.nextInt(3) < rainSoundTime++)
            {
                rainSoundTime = 0;

                // Modification, we use the adjusted rain intensity to control the volume here. Default to the max being the same volume as vanilla, but
                // with the minimum being much quieter.
                final float adjustedRainSoundIntensity = rainLevel * Mth.clampedMap(rainIntensity, 0, 0.4f, 0, 1);

                if (cursor.getY() > cameraPos.getY() + 1 && level.getHeight(Heightmap.Types.MOTION_BLOCKING, cameraPos.getX(), cameraPos.getZ()) > Mth.floor(cameraPos.getY()))
                {
                    level.playLocalSound(cursor, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, adjustedRainSoundIntensity * 0.1f, 0.5f, false);
                }
                else
                {
                    level.playLocalSound(cursor, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, adjustedRainSoundIntensity * 0.2f, 1.0f, false);
                }
            }
        }

        return true;
    }

    private float calculateRainIntensity(Level level, BlockPos cameraPos)
    {
        final ClimateModel model = Climate.get(level);
        final long calendarTick = Calendars.get(level).getCalendarTicks();
        final float climateRain = model.getRain(calendarTick);
        final float climateRainfall = model.getInstantRainfall(level, cameraPos);
        return WeatherHelpers.calculateRealRainIntensity(climateRain, climateRainfall);
    }

}
