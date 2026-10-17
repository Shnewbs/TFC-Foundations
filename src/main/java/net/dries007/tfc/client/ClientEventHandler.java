/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client;

import net.dries007.tfc.common.entities.prey.WingedPrey;


import net.minecraft.ChatFormatting;
import net.dries007.tfc.client.BlockTintRegistration.Color;
import net.dries007.tfc.client.ItemTintRegistration;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.model.animal.camel.AdultCamelModel;
import net.minecraft.client.model.animal.equine.DonkeyModel;
import net.minecraft.client.model.animal.fish.CodModel;
import net.minecraft.client.model.animal.goat.GoatModel;
import net.minecraft.client.model.object.cart.MinecartModel;
import net.minecraft.client.model.object.boat.RaftModel;
import net.minecraft.client.model.animal.squid.SquidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.LecternRenderer;
import net.minecraft.client.renderer.entity.ArmadilloRenderer;
import net.minecraft.client.renderer.entity.DolphinRenderer;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.entity.FoxRenderer;
import net.minecraft.client.renderer.entity.FrogRenderer;
import net.minecraft.client.renderer.entity.LeashKnotRenderer;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.OcelotRenderer;
import net.minecraft.client.renderer.entity.PandaRenderer;
import net.minecraft.client.renderer.entity.RabbitRenderer;
import net.dries007.tfc.client.model.TFCItemProperties;
import net.dries007.tfc.client.model.StandaloneBlockModels;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterCustomEnvironmentEffectRendererEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterPresetEditorsEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.client.extensions.BlowpipeExtension;
import net.dries007.tfc.client.model.BlockModelRegistration;
import net.dries007.tfc.client.model.ContainedFluidModel;
import net.dries007.tfc.client.model.MoldTableBlockModel;
import net.dries007.tfc.client.model.TrimmedItemModel;
import net.dries007.tfc.client.model.entity.AlpacaModel;
import net.dries007.tfc.client.model.entity.AnemometerModel;
import net.dries007.tfc.client.model.entity.ArcticCharModel;
import net.dries007.tfc.client.model.entity.BactrianCamelModel;
import net.dries007.tfc.client.model.entity.BisonModel;
import net.dries007.tfc.client.model.entity.BlackBearModel;
import net.dries007.tfc.client.model.entity.BluegillModel;
import net.dries007.tfc.client.model.entity.BoarModel;
import net.dries007.tfc.client.model.entity.BongoModel;
import net.dries007.tfc.client.model.entity.BurbotModel;
import net.dries007.tfc.client.model.entity.CalendarClockModel;
import net.dries007.tfc.client.model.entity.CaribouModel;
import net.dries007.tfc.client.model.entity.CougarModel;
import net.dries007.tfc.client.model.entity.CrocodileModel;
import net.dries007.tfc.client.model.entity.DeerModel;
import net.dries007.tfc.client.model.entity.DirewolfModel;
import net.dries007.tfc.client.model.entity.DogModel;
import net.dries007.tfc.client.model.entity.DuckModel;
import net.dries007.tfc.client.model.entity.GazelleModel;
import net.dries007.tfc.client.model.entity.GrizzlyBearModel;
import net.dries007.tfc.client.model.entity.GrouseModel;
import net.dries007.tfc.client.model.entity.HorseChestLayer;
import net.dries007.tfc.client.model.entity.HorseshoeCrabModel;
import net.dries007.tfc.client.model.entity.HyenaModel;
import net.dries007.tfc.client.model.entity.IsopodModel;
import net.dries007.tfc.client.model.entity.JavelinModel;
import net.dries007.tfc.client.model.entity.JellyfishModel;
import net.dries007.tfc.client.model.entity.JerboaModel;
import net.dries007.tfc.client.model.entity.LemmingModel;
import net.dries007.tfc.client.model.entity.LeopardSealModel;
import net.dries007.tfc.client.model.entity.LionModel;
import net.dries007.tfc.client.model.entity.LobsterModel;
import net.dries007.tfc.client.model.entity.ManateeModel;
import net.dries007.tfc.client.model.entity.MongooseModel;
import net.dries007.tfc.client.model.entity.MooseModel;
import net.dries007.tfc.client.model.entity.MuksunModel;
import net.dries007.tfc.client.model.entity.MuskOxModel;
import net.dries007.tfc.client.model.entity.NorthernPikeModel;
import net.dries007.tfc.client.model.entity.OrcaModel;
import net.dries007.tfc.client.model.entity.PacuModel;
import net.dries007.tfc.client.model.entity.PeacockBassModel;
import net.dries007.tfc.client.model.entity.PeafowlModel;
import net.dries007.tfc.client.model.entity.PenguinModel;
import net.dries007.tfc.client.model.entity.PheasantModel;
import net.dries007.tfc.client.model.entity.PolarBearModel;
import net.dries007.tfc.client.model.entity.QuailModel;
import net.dries007.tfc.client.model.entity.RatModel;
import net.dries007.tfc.client.model.entity.RedPiranhaModel;
import net.dries007.tfc.client.model.entity.SabertoothModel;
import net.dries007.tfc.client.model.entity.SpottedGudgeonModel;
import net.dries007.tfc.client.model.entity.TFCChickenModel;
import net.dries007.tfc.client.model.entity.TFCCowModel;
import net.dries007.tfc.client.model.entity.TFCCatModel;
import net.dries007.tfc.client.model.entity.TFCGoatModel;
import net.dries007.tfc.client.model.entity.TFCPigModel;
import net.dries007.tfc.client.model.entity.TFCSheepModel;
import net.dries007.tfc.client.model.entity.TFCTurtleModel;
import net.dries007.tfc.client.model.entity.TFCWolfModel;
import net.dries007.tfc.client.model.entity.TigerModel;
import net.dries007.tfc.client.model.entity.TilapiaModel;
import net.dries007.tfc.client.model.entity.TurkeyModel;
import net.dries007.tfc.client.model.entity.VaneModel;
import net.dries007.tfc.client.model.entity.WaterWheelModel;
import net.dries007.tfc.client.model.entity.WildebeestModel;
import net.dries007.tfc.client.model.entity.WindmillBladeLatticeModel;
import net.dries007.tfc.client.model.entity.WindmillBladeModel;
import net.dries007.tfc.client.model.entity.WindmillBladeRusticModel;
import net.dries007.tfc.client.model.entity.YakModel;
import net.dries007.tfc.client.overworld.LevelRendererExtension;
import net.dries007.tfc.client.overworld.StarsReloadListener;
import net.dries007.tfc.client.particle.AnimatedParticle;
import net.dries007.tfc.client.particle.BubbleColumnDownParticle;
import net.dries007.tfc.client.particle.BubbleColumnUpParticle;
import net.dries007.tfc.client.particle.BubbleParticle;
import net.dries007.tfc.client.particle.FallingLeafParticle;
import net.dries007.tfc.client.particle.FluidDripParticle;
import net.dries007.tfc.client.particle.GlintParticleProvider;
import net.dries007.tfc.client.particle.LeafParticle;
import net.dries007.tfc.client.particle.SleepParticle;
import net.dries007.tfc.client.particle.SparkParticle;
import net.dries007.tfc.client.particle.SteamParticle;
import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.client.particle.VariableHeightSmokeParticle;
import net.dries007.tfc.client.particle.WaterFlowParticle;
import net.dries007.tfc.client.particle.WindParticle;
import net.dries007.tfc.client.render.blockentity.AnemometerBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.AnvilBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.AxleBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.BarrelBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.BellowsBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.BladedAxleBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.BowlBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.BuoyBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.CalendarClockBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.ChannelBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.CharcoalForgeBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.CrankshaftBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.CrucibleBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.FirepitBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.GlassBasinBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.GrillBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.HotPouredGlassBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.JavelinItemRenderer;
import net.dries007.tfc.client.render.blockentity.LobsterTrapBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.LoomBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.MoldBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.NestBoxBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.PanItemRenderer;
import net.dries007.tfc.client.render.blockentity.PitKilnBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.PlacedItemBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.PotBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.PowerLoomBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.QuernBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.SluiceBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.TFCBellBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.TFCChestBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.TFCHangingSignBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.TFCSignBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.ToolRackBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.TripHammerBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.VaneBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.WaterWheelBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.WindmillBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.PowderkegBlockEntityRenderer;
import net.dries007.tfc.client.render.entity.AnimalRenderer;
import net.dries007.tfc.client.render.entity.DogRenderer;
import net.dries007.tfc.client.render.entity.GlowArrowRenderer;
import net.dries007.tfc.client.render.entity.JellyfishRenderer;
import net.dries007.tfc.client.render.entity.JerboaRenderer;
import net.dries007.tfc.client.render.entity.LemmingRenderer;
import net.dries007.tfc.client.render.entity.MongooseRenderer;
import net.dries007.tfc.client.render.entity.OctopoteuthisRenderer;
import net.dries007.tfc.client.render.entity.OviparousRenderer;
import net.dries007.tfc.client.render.entity.PenguinRenderer;
import net.dries007.tfc.client.render.entity.RatRenderer;
import net.dries007.tfc.client.render.entity.SimpleMobRenderer;
import net.dries007.tfc.client.render.entity.TFCBoatRenderer;
import net.dries007.tfc.client.render.entity.TFCCatRenderer;
import net.dries007.tfc.client.render.entity.TFCChestBoatRenderer;
import net.dries007.tfc.client.render.entity.TFCChestedHorseRenderer;
import net.dries007.tfc.client.render.entity.TFCCodRenderer;
import net.dries007.tfc.client.render.entity.TFCFishingHookRenderer;
import net.dries007.tfc.client.render.entity.TFCHorseRenderer;
import net.dries007.tfc.client.render.entity.TFCPufferfishRenderer;
import net.dries007.tfc.client.render.entity.TFCSalmonRenderer;
import net.dries007.tfc.client.render.entity.TFCSquidRenderer;
import net.dries007.tfc.client.render.entity.TFCTropicalFishRenderer;
import net.dries007.tfc.client.render.entity.ThrownJavelinRenderer;
import net.dries007.tfc.client.render.entity.BactrianCamelRenderer;
import net.dries007.tfc.client.screen.AnvilPlanScreen;
import net.dries007.tfc.client.screen.AnvilScreen;
import net.dries007.tfc.client.screen.BarrelScreen;
import net.dries007.tfc.client.screen.BlastFurnaceScreen;
import net.dries007.tfc.client.screen.CalendarScreen;
import net.dries007.tfc.client.screen.CharcoalForgeScreen;
import net.dries007.tfc.client.screen.ClimateScreen;
import net.dries007.tfc.client.screen.CreateTFCWorldScreen;
import net.dries007.tfc.client.screen.CrucibleScreen;
import net.dries007.tfc.client.screen.FireboxScreen;
import net.dries007.tfc.client.screen.FirepitScreen;
import net.dries007.tfc.client.screen.GrillScreen;
import net.dries007.tfc.client.screen.KnappingScreen;
import net.dries007.tfc.client.screen.LargeVesselScreen;
import net.dries007.tfc.client.screen.MoldLikeAlloyScreen;
import net.dries007.tfc.client.screen.NestBoxScreen;
import net.dries007.tfc.client.screen.NutritionScreen;
import net.dries007.tfc.client.screen.PotScreen;
import net.dries007.tfc.client.screen.PowderkegScreen;
import net.dries007.tfc.client.screen.SaladScreen;
import net.dries007.tfc.client.screen.ScribingTableScreen;
import net.dries007.tfc.client.screen.SewingTableScreen;
import net.dries007.tfc.client.screen.SmallVesselInventoryScreen;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.TooltipBlock;
import net.dries007.tfc.common.blocks.plant.KrummholzBlock;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.soil.ConnectedGrassBlock;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.container.TFCContainerTypes;
import net.dries007.tfc.common.entities.TFCEntities;
import net.dries007.tfc.common.entities.aquatic.Fish;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.mixin.client.accessor.BiomeColorsAccessor;
import net.dries007.tfc.util.Helpers;

import static net.dries007.tfc.common.blocks.wood.Wood.BlockType.*;

public final class ClientEventHandler
{
    /**
     * Texture locations for both vanilla and TFC fluid textures
     */
    public static final Identifier WATER_STILL = Helpers.identifierMC("block/water_still");
    public static final Identifier WATER_FLOW = Helpers.identifierMC("block/water_flow");
    public static final Identifier WATER_OVERLAY = Helpers.identifierMC("block/water_overlay");
    /**
     * @see net.minecraft.client.renderer.ScreenEffectRenderer#UNDERWATER_LOCATION
     */
    public static final Identifier UNDERWATER_LOCATION = Helpers.identifierMC("textures/misc/underwater.png");

    public static final Identifier MOLTEN_STILL = Helpers.identifier("block/molten_still");
    public static final Identifier MOLTEN_FLOW = Helpers.identifier("block/molten_flow");


    public static void init(ModContainer mod, IEventBus bus)
    {
        bus.addListener(ClientEventHandler::clientSetup);
        bus.addListener(ClientForgeEventHandler::registerDebugEntries);
        bus.addListener(ClientEventHandler::registerMenuScreens);
        bus.addListener(ClientEventHandler::registerModelLoaders);
        bus.addListener(ClientEventHandler::registerSpriteSources);
        bus.addListener(TFCItemProperties::register);
        bus.addListener(ClientEventHandler::registerSpecialRenderers);
        bus.addListener(BlockModelRegistration::registerLoaders);
        bus.addListener(BlockModelRegistration::registerBlockStateModels);
        bus.addListener(MoldTableBlockModel::registerStandaloneModels);
        bus.addListener(ClientEventHandler::registerSpecialModels);
        bus.addListener(ClientEventHandler::registerColorHandlerBlocks);
        bus.addListener(ClientEventHandler::registerColorHandlerItems);
        bus.addListener(ClientEventHandler::registerColorResolvers);
        bus.addListener(ClientEventHandler::registerParticleFactories);
        bus.addListener(ClientEventHandler::registerClientReloadListeners);
        bus.addListener(ClientEventHandler::registerEntityRenderers);
        bus.addListener(ClientEventHandler::registerTooltipFactories);
        bus.addListener(ClientEventHandler::registerKeyBindings);
        bus.addListener(ClientEventHandler::registerLayerDefinitions);
        bus.addListener(ClientEventHandler::registerPresetEditors);
        bus.addListener(ClientEventHandler::registerExtensions);
        bus.addListener(FluidModelRegistration::registerModels);
        bus.addListener(ClientEventHandler::registerDimensionEffects);
        bus.addListener(LevelRendererExtension::registerPipelines);
        bus.addListener(IngameOverlays::registerOverlays);

        mod.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SuppressWarnings("deprecation")
    public static void clientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {

            TFCBlocks.WOODS.forEach((wood, map) -> {
                HorseChestLayer.registerChest(map.get(CHEST).get().asItem(), Helpers.identifier("textures/entity/chest/horse/" + wood.getSerializedName() + ".png"));
                HorseChestLayer.registerChest(map.get(TRAPPED_CHEST).get().asItem(), Helpers.identifier("textures/entity/chest/horse/" + wood.getSerializedName() + ".png"));
                HorseChestLayer.registerChest(map.get(BARREL).get().asItem(), Helpers.identifier("textures/entity/chest/horse/" + wood.getSerializedName() + "_barrel.png"));
            });
        });

        // 26.1 classifies baked quads from texture transparency. Fluids use
        // FluidModelRegistration, and the ModelBlockRenderer mixin retains fast leaves.

        // Misc
        BiomeColorsAccessor.accessor$setWaterColorsResolver(TFCColors.FRESH_WATER);

        for (Wood wood : Wood.VALUES)
        {
            Sheets.addWoodType(wood.getVanillaWoodType());
        }
    }

    public static void registerMenuScreens(RegisterMenuScreensEvent event)
    {
        event.register(TFCContainerTypes.CALENDAR.get(), CalendarScreen::new);
        event.register(TFCContainerTypes.NUTRITION.get(), NutritionScreen::new);
        event.register(TFCContainerTypes.CLIMATE.get(), ClimateScreen::new);
        event.register(TFCContainerTypes.SALAD.get(), SaladScreen::new);
        event.register(TFCContainerTypes.WORKBENCH.get(), CraftingScreen::new);
        event.register(TFCContainerTypes.FIREPIT.get(), FirepitScreen::new);
        event.register(TFCContainerTypes.GRILL.get(), GrillScreen::new);
        event.register(TFCContainerTypes.POT.get(), PotScreen::new);
        event.register(TFCContainerTypes.POWDERKEG.get(), PowderkegScreen::new);
        event.register(TFCContainerTypes.CHARCOAL_FORGE.get(), CharcoalForgeScreen::new);
        event.register(TFCContainerTypes.FIREBOX.get(), FireboxScreen::new);
        event.register(TFCContainerTypes.NEST_BOX.get(), NestBoxScreen::new);
        event.register(TFCContainerTypes.CRUCIBLE.get(), CrucibleScreen::new);
        event.register(TFCContainerTypes.BARREL.get(), BarrelScreen::new);
        event.register(TFCContainerTypes.ANVIL.get(), AnvilScreen::new);
        event.register(TFCContainerTypes.ANVIL_PLAN.get(), AnvilPlanScreen::new);
        event.register(TFCContainerTypes.BLAST_FURNACE.get(), BlastFurnaceScreen::new);
        event.register(TFCContainerTypes.CHEST_9x2.get(), ContainerScreen::new);
        event.register(TFCContainerTypes.CHEST_9x4.get(), ContainerScreen::new);
        event.register(TFCContainerTypes.KNAPPING.get(), KnappingScreen::new);
        event.register(TFCContainerTypes.SMALL_VESSEL_INVENTORY.get(), SmallVesselInventoryScreen::new);
        event.register(TFCContainerTypes.MOLD_LIKE_ALLOY.get(), MoldLikeAlloyScreen::new);
        event.register(TFCContainerTypes.LARGE_VESSEL.get(), LargeVesselScreen::new);
        event.register(TFCContainerTypes.SCRIBING_TABLE.get(), ScribingTableScreen::new);
        event.register(TFCContainerTypes.SEWING_TABLE.get(), SewingTableScreen::new);
    }

    public static void registerTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event)
    {
        event.register(TooltipBlock.Instance.class, ClientInventoryTooltip::new);
    }

    public static void registerKeyBindings(RegisterKeyMappingsEvent event)
    {
        event.registerCategory(TFCKeyBindings.CATEGORY);
        event.register(TFCKeyBindings.PLACE_BLOCK);
        event.register(TFCKeyBindings.CYCLE_CHISEL_MODE);
        event.register(TFCKeyBindings.STACK_FOOD);
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        // Entities
        event.registerEntityRenderer(TFCEntities.FALLING_BLOCK.get(), FallingBlockRenderer::new);
        event.registerEntityRenderer(TFCEntities.FISHING_BOBBER.get(), TFCFishingHookRenderer::new);
        event.registerEntityRenderer(TFCEntities.THROWN_JAVELIN.get(), ThrownJavelinRenderer::new);
        event.registerEntityRenderer(TFCEntities.GLOW_ARROW.get(), GlowArrowRenderer::new);
        event.registerEntityRenderer(TFCEntities.SEAT.get(), NoopRenderer::new);
        event.registerEntityRenderer(TFCEntities.ROPE_KNOT.get(), LeashKnotRenderer::new);
        event.registerEntityRenderer(TFCEntities.CHEST_MINECART.get(), ctx -> new MinecartRenderer(ctx, RenderHelpers.layerId("chest_minecart")));
        event.registerEntityRenderer(TFCEntities.HOLDING_MINECART.get(), ctx -> new MinecartRenderer(ctx, RenderHelpers.layerId("holding_minecart")));
        for (Wood wood : Wood.VALUES)
        {
            event.registerEntityRenderer(TFCEntities.BOATS.get(wood).get(), ctx -> new TFCBoatRenderer(ctx, wood.getSerializedName()));
            event.registerEntityRenderer(TFCEntities.CHEST_BOATS.get(wood).get(), ctx -> new TFCChestBoatRenderer(ctx, wood.getSerializedName()));
        }
        event.registerEntityRenderer(TFCEntities.COD.get(), TFCCodRenderer::new);
        event.registerEntityRenderer(TFCEntities.FRESHWATER_FISH.get(Fish.SALMON).get(), TFCSalmonRenderer::new);
        event.registerEntityRenderer(TFCEntities.FRESHWATER_FISH.get(Fish.LARGEMOUTH_BASS).get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, CodModel::new, "largemouth_bass").flops().build());
        event.registerEntityRenderer(TFCEntities.FRESHWATER_FISH.get(Fish.SMALLMOUTH_BASS).get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, CodModel::new, "smallmouth_bass").flops().build());
        event.registerEntityRenderer(TFCEntities.FRESHWATER_FISH.get(Fish.LAKE_TROUT).get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, CodModel::new, "lake_trout").flops().build());
        event.registerEntityRenderer(TFCEntities.FRESHWATER_FISH.get(Fish.RAINBOW_TROUT).get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, CodModel::new, "rainbow_trout").flops().build());
        event.registerEntityRenderer(TFCEntities.FRESHWATER_FISH.get(Fish.CRAPPIE).get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, CodModel::new, "crappie").flops().build());
        event.registerEntityRenderer(TFCEntities.FRESHWATER_FISH.get(Fish.BLUEGILL).get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, BluegillModel::new, "bluegill").flops().build());
        event.registerEntityRenderer(TFCEntities.FRESHWATER_FISH.get(Fish.NORTHERN_PIKE).get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, NorthernPikeModel::new, "northern_pike").flops().build());
        event.registerEntityRenderer(TFCEntities.FRESHWATER_FISH.get(Fish.BURBOT).get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, BurbotModel::new, "burbot").flops().build());
        event.registerEntityRenderer(TFCEntities.FRESHWATER_FISH.get(Fish.ARCTIC_CHAR).get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, ArcticCharModel::new, "arctic_char").flops().build());
        event.registerEntityRenderer(TFCEntities.FRESHWATER_FISH.get(Fish.MUKSUN).get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, MuksunModel::new, "muksun").flops().build());
        event.registerEntityRenderer(TFCEntities.FRESHWATER_FISH.get(Fish.TILAPIA).get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, TilapiaModel::new, "tilapia").flops().build());
        event.registerEntityRenderer(TFCEntities.FRESHWATER_FISH.get(Fish.SPOTTED_GUDGEON).get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, SpottedGudgeonModel::new, "spotted_gudgeon").flops().build());
        event.registerEntityRenderer(TFCEntities.FRESHWATER_FISH.get(Fish.PEACOCK_BASS).get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, PeacockBassModel::new, "peacock_bass").flops().build());
        event.registerEntityRenderer(TFCEntities.FRESHWATER_FISH.get(Fish.PACU).get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, PacuModel::new, "pacu").flops().build());
        event.registerEntityRenderer(TFCEntities.FRESHWATER_FISH.get(Fish.RED_PIRANHA).get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, RedPiranhaModel::new, "red_piranha").flops().build());
        event.registerEntityRenderer(TFCEntities.TROPICAL_FISH.get(), TFCTropicalFishRenderer::new);
        event.registerEntityRenderer(TFCEntities.PUFFERFISH.get(), TFCPufferfishRenderer::new);
        event.registerEntityRenderer(TFCEntities.JELLYFISH.get(), JellyfishRenderer::new);
        event.registerEntityRenderer(TFCEntities.LOBSTER.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, LobsterModel::new, "lobster").build());
        event.registerEntityRenderer(TFCEntities.CRAYFISH.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, LobsterModel::new, "crayfish").build());
        event.registerEntityRenderer(TFCEntities.ISOPOD.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, IsopodModel::new, "isopod").build());
        event.registerEntityRenderer(TFCEntities.HORSESHOE_CRAB.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, HorseshoeCrabModel::new, "horseshoe_crab").build());
        event.registerEntityRenderer(TFCEntities.DOLPHIN.get(), DolphinRenderer::new);
        event.registerEntityRenderer(TFCEntities.ORCA.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, OrcaModel::new, "orca").build());
        event.registerEntityRenderer(TFCEntities.MANATEE.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, ManateeModel::new, "manatee").build());
        event.registerEntityRenderer(TFCEntities.TURTLE.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, TFCTurtleModel::new, "turtle").build());
        event.registerEntityRenderer(TFCEntities.PENGUIN.get(), PenguinRenderer::new);
        event.registerEntityRenderer(TFCEntities.LEOPARD_SEAL.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, LeopardSealModel::new, "seal").build());
        event.registerEntityRenderer(TFCEntities.FROG.get(), FrogRenderer::new);
        event.registerEntityRenderer(TFCEntities.POLAR_BEAR.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, PolarBearModel::new, "polar_bear").shadow(0.9f).build());
        event.registerEntityRenderer(TFCEntities.GRIZZLY_BEAR.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, GrizzlyBearModel::new, "grizzly_bear").shadow(0.9f).scale(1.1f).build());
        event.registerEntityRenderer(TFCEntities.BLACK_BEAR.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, BlackBearModel::new, "black_bear").shadow(0.9f).scale(0.9f).build());
        event.registerEntityRenderer(TFCEntities.COUGAR.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, CougarModel::new, "cougar").shadow(0.8f).build());
        event.registerEntityRenderer(TFCEntities.PANTHER.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, CougarModel::new, "panther").shadow(0.8f).build());
        event.registerEntityRenderer(TFCEntities.LION.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, LionModel::new, "lion").shadow(0.8f).build());
        event.registerEntityRenderer(TFCEntities.SABERTOOTH.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, SabertoothModel::new, "sabertooth").shadow(0.8f).build());
        event.registerEntityRenderer(TFCEntities.TIGER.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, TigerModel::new, "tiger").shadow(0.8f).build());
        event.registerEntityRenderer(TFCEntities.CROCODILE.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, CrocodileModel::new, "crocodile").shadow(0.8f).build());
        event.registerEntityRenderer(TFCEntities.WOLF.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, TFCWolfModel::new, "wolf").shadow(0.5f).scale(1.1f).build());
        event.registerEntityRenderer(TFCEntities.HYENA.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, HyenaModel::new, "hyena").shadow(0.5f).scale(1.1f).build());
        event.registerEntityRenderer(TFCEntities.DIREWOLF.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, DirewolfModel::new, "direwolf").shadow(0.9f).build());
        event.registerEntityRenderer(TFCEntities.SQUID.get(), ctx -> new TFCSquidRenderer<>(ctx, new SquidModel(RenderHelpers.bakeSimple(ctx, "squid"))));
        event.registerEntityRenderer(TFCEntities.OCTOPOTEUTHIS.get(), ctx -> new OctopoteuthisRenderer(ctx, new SquidModel(RenderHelpers.bakeSimple(ctx, "glow_squid"))));
        event.registerEntityRenderer(TFCEntities.PIG.get(), ctx -> new AnimalRenderer<>(ctx, new TFCPigModel(RenderHelpers.bakeSimple(ctx, "pig")), "pig"));
        event.registerEntityRenderer(TFCEntities.COW.get(), ctx -> new AnimalRenderer<>(ctx, new TFCCowModel(RenderHelpers.bakeSimple(ctx, "cow")), "cow"));
        event.registerEntityRenderer(TFCEntities.GOAT.get(), ctx -> new AnimalRenderer<>(ctx, new TFCGoatModel(RenderHelpers.bakeSimple(ctx, "goat")), "goat"));
        event.registerEntityRenderer(TFCEntities.YAK.get(), ctx -> new AnimalRenderer<>(ctx, new YakModel(RenderHelpers.bakeSimple(ctx, "yak")), "yak"));
        event.registerEntityRenderer(TFCEntities.ALPACA.get(), ctx -> new AnimalRenderer<>(ctx, new AlpacaModel(RenderHelpers.bakeSimple(ctx, "alpaca")), "alpaca"));
        event.registerEntityRenderer(TFCEntities.SHEEP.get(), ctx -> new AnimalRenderer<>(ctx, new TFCSheepModel(RenderHelpers.bakeSimple(ctx, "sheep")), "sheep"));
        event.registerEntityRenderer(TFCEntities.MUSK_OX.get(), ctx -> new AnimalRenderer<>(ctx, new MuskOxModel(RenderHelpers.bakeSimple(ctx, "musk_ox")), "musk_ox"));
        event.registerEntityRenderer(TFCEntities.CHICKEN.get(), ctx -> new OviparousRenderer<>(ctx, new TFCChickenModel(RenderHelpers.bakeSimple(ctx, "chicken")), "chicken", "rooster", "chick"));
        event.registerEntityRenderer(TFCEntities.DUCK.get(), ctx -> new OviparousRenderer<>(ctx, new DuckModel(RenderHelpers.bakeSimple(ctx, "duck")), "duck", "drake", "duckling"));
        event.registerEntityRenderer(TFCEntities.QUAIL.get(), ctx -> new OviparousRenderer<>(ctx, new QuailModel(RenderHelpers.bakeSimple(ctx, "quail")), "quail", "quail_male", "quail_chick"));
        event.registerEntityRenderer(TFCEntities.RABBIT.get(), RabbitRenderer::new);
        event.registerEntityRenderer(TFCEntities.FOX.get(), FoxRenderer::new);
        event.registerEntityRenderer(TFCEntities.PANDA.get(), PandaRenderer::new);
        event.registerEntityRenderer(TFCEntities.OCELOT.get(), OcelotRenderer::new);
        event.registerEntityRenderer(TFCEntities.BONGO.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, BongoModel::new, "bongo").shadow(0.6f).build());
        event.registerEntityRenderer(TFCEntities.CARIBOU.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, CaribouModel::new, "caribou").shadow(0.6f).build());
        event.registerEntityRenderer(TFCEntities.DEER.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, DeerModel::new, "deer").shadow(0.6f).hasBabyTexture().build());
        event.registerEntityRenderer(TFCEntities.GAZELLE.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, GazelleModel::new, "gazelle").shadow(0.6f).build());
        event.registerEntityRenderer(TFCEntities.GROUSE.get(), ctx -> new SimpleMobRenderer.Builder<WingedPrey, GrouseModel>(ctx, GrouseModel::new, "grouse").shadow(0.5f).texture(e -> RenderHelpers.getGenderedTexture(e, "grouse")).build());
        event.registerEntityRenderer(TFCEntities.PHEASANT.get(), ctx -> new SimpleMobRenderer.Builder<WingedPrey, PheasantModel>(ctx, PheasantModel::new, "pheasant").shadow(0.5f).texture(e -> RenderHelpers.getGenderedTexture(e, "pheasant")).build());
        event.registerEntityRenderer(TFCEntities.TURKEY.get(), ctx -> new SimpleMobRenderer.Builder<WingedPrey, TurkeyModel>(ctx, TurkeyModel::new, "turkey").shadow(0.5f).texture(e -> RenderHelpers.getGenderedTexture(e, "turkey")).build());
        event.registerEntityRenderer(TFCEntities.PEAFOWL.get(), ctx -> new SimpleMobRenderer.Builder<WingedPrey, PeafowlModel>(ctx, PeafowlModel::new, "peafowl").shadow(0.5f).texture(e -> RenderHelpers.getGenderedTexture(e, "peafowl")).build());
        event.registerEntityRenderer(TFCEntities.MULE.get(), ctx -> new TFCChestedHorseRenderer<>(ctx, 0.92F, RenderHelpers.layerId("mule"), "mule"));
        event.registerEntityRenderer(TFCEntities.DONKEY.get(), ctx -> new TFCChestedHorseRenderer<>(ctx, 0.87F, RenderHelpers.layerId("donkey"), "donkey"));
        event.registerEntityRenderer(TFCEntities.HORSE.get(), TFCHorseRenderer::new);
        event.registerEntityRenderer(TFCEntities.DROMEDARY_CAMEL.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, net.dries007.tfc.client.model.entity.DromedaryCamelModel::new, "dromedary_camel").shadow(0.7f).texture((e) -> Identifier.withDefaultNamespace("textures/entity/camel/camel.png")).build());
        event.registerEntityRenderer(TFCEntities.BACTRIAN_CAMEL.get(), ctx -> new BactrianCamelRenderer<>(ctx, new BactrianCamelModel(RenderHelpers.bakeSimple(ctx, "bactrian_camel")), 0.6F));
        event.registerEntityRenderer(TFCEntities.RAT.get(), RatRenderer::new);
        event.registerEntityRenderer(TFCEntities.JERBOA.get(), JerboaRenderer::new);
        event.registerEntityRenderer(TFCEntities.LEMMING.get(), LemmingRenderer::new);
        event.registerEntityRenderer(TFCEntities.MONGOOSE.get(), MongooseRenderer::new);
        event.registerEntityRenderer(TFCEntities.CAT.get(), TFCCatRenderer::new);
        event.registerEntityRenderer(TFCEntities.DOG.get(), DogRenderer::new);
        event.registerEntityRenderer(TFCEntities.BOAR.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, BoarModel::new, "boar").build());
        event.registerEntityRenderer(TFCEntities.WILDEBEEST.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, WildebeestModel::new, "wildebeest").build());
        event.registerEntityRenderer(TFCEntities.MOOSE.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, MooseModel::new, "moose").shadow(1.0f).scale(0.8f).build());
        event.registerEntityRenderer(TFCEntities.BISON.get(), ctx -> new SimpleMobRenderer.Builder<>(ctx, BisonModel::new, "bison").build());
        event.registerEntityRenderer(TFCEntities.ARMADILLO.get(), ArmadilloRenderer::new);

        // BEs
        event.registerBlockEntityRenderer(TFCBlockEntities.FIREPIT.get(), FirepitBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.POT.get(), PotBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.GRILL.get(), GrillBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.PLACED_ITEM.get(), PlacedItemBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.PIT_KILN.get(), PitKilnBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.SHELF.get(), PlacedItemBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.QUERN.get(), QuernBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.CHEST.get(), TFCChestBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.TRAPPED_CHEST.get(), TFCChestBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.LOOM.get(), LoomBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.POWER_LOOM.get(), PowerLoomBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.SLUICE.get(), SluiceBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.BELLOWS.get(), BellowsBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.TOOL_RACK.get(), ToolRackBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.SIGN.get(), TFCSignBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.HANGING_SIGN.get(), TFCHangingSignBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.BARREL.get(), BarrelBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.CRUCIBLE.get(), CrucibleBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.LECTERN.get(), LecternRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.ANVIL.get(), AnvilBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.CHARCOAL_FORGE.get(), CharcoalForgeBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.NEST_BOX.get(), NestBoxBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.BELL.get(), TFCBellBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.BOWL.get(), BowlBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.HOT_POURED_GLASS.get(), HotPouredGlassBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.GLASS_BASIN.get(), GlassBasinBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.AXLE.get(), AxleBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.BLADED_AXLE.get(), BladedAxleBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.TRIP_HAMMER.get(), TripHammerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.WATER_WHEEL.get(), WaterWheelBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.WINDMILL.get(), WindmillBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.VANE.get(), VaneBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.ANEMOMETER.get(), AnemometerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.CRANKSHAFT.get(), CrankshaftBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.BELL.get(), TFCBellBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.CHANNEL.get(), ChannelBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.MOLD_TABLE.get(), MoldBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.CALENDAR_CLOCK.get(), CalendarClockBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.POWDERKEG.get(), PowderkegBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.BUOY.get(), BuoyBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(TFCBlockEntities.LOBSTER_TRAP.get(), LobsterTrapBlockEntityRenderer::new);
    }

    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        LayerDefinition boatLayer = BoatModel.createBoatModel();
        LayerDefinition raftLayer = RaftModel.createRaftModel();
        LayerDefinition chestLayer = BoatModel.createChestBoatModel();
        LayerDefinition chestRaftLayer = RaftModel.createChestRaftModel();
        for (Wood wood : Wood.VALUES)
        {
            event.registerLayerDefinition(TFCBoatRenderer.boatName(wood.getSerializedName()), wood == Wood.PALM ? () -> raftLayer : () -> boatLayer);
            event.registerLayerDefinition(TFCChestBoatRenderer.chestBoatName(wood.getSerializedName()), wood == Wood.PALM ? () -> chestRaftLayer : () -> chestLayer);
        }
        event.registerLayerDefinition(RenderHelpers.layerId("bluegill"), BluegillModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("crappie"), CodModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("smallmouth_bass"), CodModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("largemouth_bass"), CodModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("rainbow_trout"), CodModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("lake_trout"), CodModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("arctic_char"), ArcticCharModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("burbot"), BurbotModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("muksun"), MuksunModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("northern_pike"), NorthernPikeModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("pacu"), PacuModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("peacock_bass"), PeacockBassModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("red_piranha"), RedPiranhaModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("spotted_gudgeon"), SpottedGudgeonModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("tilapia"), TilapiaModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("jellyfish"), JellyfishModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("lobster"), LobsterModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("crayfish"), LobsterModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("horseshoe_crab"), HorseshoeCrabModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("isopod"), IsopodModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("orca"), OrcaModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("manatee"), ManateeModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("turtle"), TFCTurtleModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("penguin"), PenguinModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("seal"), LeopardSealModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("polar_bear"), PolarBearModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("grizzly_bear"), GrizzlyBearModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("black_bear"), BlackBearModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("cougar"), CougarModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("panther"), CougarModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("lion"), LionModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("sabertooth"), SabertoothModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("tiger"), TigerModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("crocodile"), CrocodileModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("wolf"), TFCWolfModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("hyena"), HyenaModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("direwolf"), DirewolfModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("squid"), SquidModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("glow_squid"), SquidModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("pig"), () -> TFCPigModel.createTFCBodyLayer(CubeDeformation.NONE));
        event.registerLayerDefinition(RenderHelpers.layerId("cow"), TFCCowModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("goat"), GoatModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("yak"), YakModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("alpaca"), AlpacaModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("sheep"), TFCSheepModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("musk_ox"), MuskOxModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("chicken"), TFCChickenModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("duck"), DuckModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("quail"), QuailModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("bongo"), BongoModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("caribou"), CaribouModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("deer"), DeerModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("gazelle"), GazelleModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("moose"), MooseModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("grouse"), GrouseModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("pheasant"), PheasantModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("turkey"), TurkeyModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("peafowl"), PeafowlModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("rat"), RatModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("lemming"), LemmingModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("jerboa"), JerboaModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("mongoose"), MongooseModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("dog"), DogModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("cat"), () -> TFCCatModel.createBodyLayer(CubeDeformation.NONE));
        event.registerLayerDefinition(RenderHelpers.layerId("cat_collar"), () -> TFCCatModel.createBodyLayer(new CubeDeformation(0.01f)));
        event.registerLayerDefinition(RenderHelpers.layerId("boar"), BoarModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("wildebeest"), WildebeestModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("bison"), BisonModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("javelin"), JavelinModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("chest_minecart"), MinecartModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("holding_minecart"), MinecartModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("bell_body"), net.minecraft.client.model.object.bell.BellModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("water_wheel"), WaterWheelModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("windmill_blade"), WindmillBladeModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("windmill_blade_lattice"), WindmillBladeLatticeModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("windmill_blade_rustic"), WindmillBladeRusticModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("donkey_saddle"), () -> DonkeyModel.createSaddleLayer(1F));
        event.registerLayerDefinition(RenderHelpers.layerId("mule_saddle"), () -> DonkeyModel.createSaddleLayer(1F));
        event.registerLayerDefinition(RenderHelpers.layerId("horse_chest"), () -> DonkeyModel.createBodyLayer(1F));
        event.registerLayerDefinition(RenderHelpers.layerId("mule"), () -> DonkeyModel.createBodyLayer(1F));
        event.registerLayerDefinition(RenderHelpers.layerId("donkey"), () -> DonkeyModel.createBodyLayer(1F));
        event.registerLayerDefinition(RenderHelpers.layerId("dromedary_camel"), AdultCamelModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("bactrian_camel"), BactrianCamelModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("water_wheel"), WaterWheelModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("vane"), VaneModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("anemometer"), AnemometerModel::createBodyLayer);
        event.registerLayerDefinition(RenderHelpers.layerId("calendar_clock"), CalendarClockModel::createBodyLayer);
    }


    public static void registerSpecialModels(ModelEvent.RegisterStandalone event)
    {
        for (String metal : new String[] {"native_copper", "native_silver", "native_gold", "cassiterite"})
        {
            register(event, Helpers.identifier("item/pan/" + metal + "/result"));

            for (Rock rock : Rock.values())
            {
                register(event, Helpers.identifier("item/pan/" + metal + "/" + rock.getSerializedName() + "_half"));
                register(event, Helpers.identifier("item/pan/" + metal + "/" + rock.getSerializedName() + "_full"));
            }
        }

        PlacedItemBlockEntityRenderer.MODELS.forEach((item, model) -> register(event, model.model()));
        FirepitBlockEntityRenderer.BURN_STAGE_MODELS.values().forEach(list -> list.forEach(id -> register(event, id)));

        register(event, CrankshaftBlockEntityRenderer.WHEEL_MODEL);
        TFCBlocks.BUOYS.values().forEach(buoy -> register(event, BuiltInRegistries.BLOCK.getKey(buoy.get()).withPrefix("block/")));

        TFCConfig.CLIENT.additionalSpecialModels.get().forEach(s -> register(event, Identifier.parse(s)));
    }

    private static void register(ModelEvent.RegisterStandalone event, Identifier id)
    {
        StandaloneBlockModels.register(event, id);
    }

    public static void registerSpriteSources(net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent event)
    {
        event.register(Helpers.identifier("item_directory"), TFCItemSpriteSource.CODEC);
    }

    public static void registerModelLoaders(net.neoforged.neoforge.client.event.RegisterItemModelsEvent event)
    {
        event.register(Helpers.identifier("fluid_container"), ContainedFluidModel.Unbaked.CODEC);
        event.register(Helpers.identifier("trim"), TrimmedItemModel.Unbaked.CODEC);
        event.register(Helpers.identifier("pan"), PanItemRenderer.Unbaked.CODEC);
    }

    public static void registerSpecialRenderers(net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent event)
    {
        event.register(Helpers.identifier("javelin"), JavelinItemRenderer.Unbaked.CODEC);
    }

    public static void registerColorHandlerBlocks(RegisterColorHandlersEvent.BlockTintSources nativeEvent)
    {
        final var event = new BlockTintRegistration(nativeEvent);
        final Color grassColor = (state, level, pos, tintIndex) -> TFCColors.getGrassColor(pos, tintIndex);
        final Color tallGrassColor = (state, level, pos, tintIndex) -> TFCColors.getTallGrassColor(pos, tintIndex);
        final Color foliageColor = (state, level, pos, tintIndex) -> TFCColors.getFoliageColor(pos, tintIndex);
        final Color grassBlockColor = (state, level, pos, tintIndex) -> state.getValue(ConnectedGrassBlock.SNOWY) || tintIndex != 1 ? -1 : grassColor.getColor(state, level, pos, tintIndex);
        final Color waterBlockColor = (state, level, pos, tintIndex) -> TFCColors.getWaterColor(pos);

        TFCBlocks.SOIL.get(SoilBlockType.GRASS).values().forEach(reg -> event.register(grassBlockColor, reg.get()));
        TFCBlocks.SOIL.get(SoilBlockType.CLAY_GRASS).values().forEach(reg -> event.register(grassBlockColor, reg.get()));
        event.register(grassBlockColor, TFCBlocks.PEAT_GRASS.get());
        event.register(grassBlockColor, TFCBlocks.KAOLIN_CLAY_GRASS.get());

        TFCBlocks.PLANTS.forEach((plant, reg) -> {
            if (plant.isBlockTinted())
                event.register(
                    plant.usesWaterTint() ?
                        waterBlockColor :
                        plant.isTallGrass() ?
                            tallGrassColor :
                            plant.isSeasonal() ?
                                (state, level, pos, tintIndex) -> TFCColors.getSeasonalFoliageColor(pos, tintIndex, 145) :
                                plant.isFoliage() ?
                                    foliageColor :
                                    grassColor, reg.get());
        });
        TFCBlocks.POTTED_PLANTS.forEach((plant, reg) -> {
            if (plant.isFlowerpotTinted())
                event.register(grassColor, reg.get());
        });
        TFCBlocks.WOODS.forEach((wood, reg) -> event.register(
            wood.isConifer() ?
                foliageColor :
                (state, level, pos, tintIndex) -> TFCColors.getSeasonalFoliageColor(pos, tintIndex, wood.autumnIndex()),
            reg.get(Wood.BlockType.LEAVES).get()));

        TFCBlocks.WOODS.forEach((wood, reg) -> event.register(
            wood.isConifer() ?
                blockColor(0xcf7d13) :
                (state, level, pos, tintIndex) -> TFCColors.getSeasonalFoliageColor(pos, tintIndex, wood.autumnIndex()),
            reg.get(Wood.BlockType.FALLEN_LEAVES).get()));

        event.register((state, level, pos, tintIndex) -> TFCColors.getSeasonalFoliageColor(pos, tintIndex, Wood.PINE.autumnIndex()), TFCBlocks.POTTED_PINE_KRUMMHOLZ.get());
        event.register((state, level, pos, tintIndex) -> TFCColors.getSeasonalFoliageColor(pos, tintIndex, Wood.DOUGLAS_FIR.autumnIndex()), TFCBlocks.POTTED_DOUGLAS_FIR_KRUMMHOLZ.get());
        event.register((state, level, pos, tintIndex) -> TFCColors.getSeasonalFoliageColor(pos, tintIndex, Wood.SPRUCE.autumnIndex()), TFCBlocks.POTTED_SPRUCE_KRUMMHOLZ.get());
        event.register((state, level, pos, tintIndex) -> TFCColors.getSeasonalFoliageColor(pos, tintIndex, Wood.WHITE_CEDAR.autumnIndex()), TFCBlocks.POTTED_WHITE_CEDAR_KRUMMHOLZ.get());
        event.register((state, level, pos, tintIndex) -> TFCColors.getSeasonalFoliageColor(pos, tintIndex, Wood.ASPEN.autumnIndex()), TFCBlocks.POTTED_ASPEN_KRUMMHOLZ.get());
        event.register((state, level, pos, tintIndex) -> state.getValue(KrummholzBlock.SNOWY) ? -1 : TFCColors.getSeasonalFoliageColor(pos, tintIndex, Wood.PINE.autumnIndex()), TFCBlocks.PINE_KRUMMHOLZ.get());
        event.register((state, level, pos, tintIndex) -> state.getValue(KrummholzBlock.SNOWY) ? -1 : TFCColors.getSeasonalFoliageColor(pos, tintIndex, Wood.DOUGLAS_FIR.autumnIndex()), TFCBlocks.DOUGLAS_FIR_KRUMMHOLZ.get());
        event.register((state, level, pos, tintIndex) -> state.getValue(KrummholzBlock.SNOWY) ? -1 : TFCColors.getSeasonalFoliageColor(pos, tintIndex, Wood.SPRUCE.autumnIndex()), TFCBlocks.SPRUCE_KRUMMHOLZ.get());
        event.register((state, level, pos, tintIndex) -> state.getValue(KrummholzBlock.SNOWY) ? -1 : TFCColors.getSeasonalFoliageColor(pos, tintIndex, Wood.WHITE_CEDAR.autumnIndex()), TFCBlocks.WHITE_CEDAR_KRUMMHOLZ.get());
        event.register((state, level, pos, tintIndex) -> state.getValue(KrummholzBlock.SNOWY) ? -1 : TFCColors.getSeasonalFoliageColor(pos, tintIndex, Wood.ASPEN.autumnIndex()), TFCBlocks.ASPEN_KRUMMHOLZ.get());

        TFCBlocks.WILD_CROPS.forEach((crop, reg) -> event.register(grassColor, reg.get()));

        event.register(waterBlockColor, TFCBlocks.SALT_WATER.get(), TFCBlocks.SEA_ICE.get(), TFCBlocks.RIVER_WATER.get());
        event.register(blockColor(0x5FB5B8), TFCBlocks.SPRING_WATER.get());
    }

    private static Color blockColor(int color)
    {
        return (state, level, pos, tintIndex) -> color;
    }

    public static void registerColorResolvers(RegisterColorHandlersEvent.ColorResolvers event)
    {
        event.register(TFCColors.SALT_WATER);
        event.register(TFCColors.FRESH_WATER);
    }

    public static void registerColorHandlerItems(RegisterColorHandlersEvent.ItemTintSources nativeEvent)
    {
        final var event = new ItemTintRegistration();
        final ItemTintRegistration.Color grassColor = (stack, tintIndex) -> TFCColors.getGrassColor(null, tintIndex);
        final ItemTintRegistration.Color seasonalFoliageColor = (stack, tintIndex) -> TFCColors.getFoliageColor(null, tintIndex);

        TFCBlocks.PLANTS.forEach((plant, reg) -> {
            if (plant.isItemTinted())
                event.register(plant.isSeasonal() ? seasonalFoliageColor : grassColor, reg.get());
        });
        TFCBlocks.WOODS.forEach((key, value) -> event.register(seasonalFoliageColor, value.get(Wood.BlockType.FALLEN_LEAVES).get(), value.get(LEAVES).get()));
        TFCBlocks.WILD_CROPS.forEach((key, value) -> event.register(grassColor, value.get().asItem()));
        event.register(seasonalFoliageColor, TFCBlocks.PINE_KRUMMHOLZ.get().asItem());
        event.register(seasonalFoliageColor, TFCBlocks.DOUGLAS_FIR_KRUMMHOLZ.get().asItem());
        event.register(seasonalFoliageColor, TFCBlocks.WHITE_CEDAR_KRUMMHOLZ.get().asItem());
        event.register(seasonalFoliageColor, TFCBlocks.SPRUCE_KRUMMHOLZ.get().asItem());
        event.register(seasonalFoliageColor, TFCBlocks.ASPEN_KRUMMHOLZ.get().asItem());

    }

    public static void registerClientReloadListeners(AddClientReloadListenersEvent event)
    {
        // Color maps
        // We maintain a series of color maps independent and beyond the vanilla color maps
        // Sky, Fog, Water and Water Fog color to replace hardcoded per-biome water colors
        // Grass and foliage (which we replace vanilla's anyway, but use our own for better indexing)
        // Foliage winter and fall (for deciduous trees which have leaves which change color during those seasons)
        event.addListener(TFCColors.SKY_COLORS_LOCATION, new ColorMapReloadListener(TFCColors::setSkyColors, TFCColors.SKY_COLORS_LOCATION));
        event.addListener(TFCColors.FOG_COLORS_LOCATION, new ColorMapReloadListener(TFCColors::setFogColors, TFCColors.FOG_COLORS_LOCATION));
        event.addListener(TFCColors.WATER_COLORS_LOCATION, new ColorMapReloadListener(TFCColors::setWaterColors, TFCColors.WATER_COLORS_LOCATION));
        event.addListener(TFCColors.WATER_FOG_COLORS_LOCATION, new ColorMapReloadListener(TFCColors::setWaterFogColors, TFCColors.WATER_FOG_COLORS_LOCATION));
        event.addListener(TFCColors.GRASS_COLORS_LOCATION, new ColorMapReloadListener(TFCColors::setGrassColors, TFCColors.GRASS_COLORS_LOCATION));
        event.addListener(TFCColors.TALL_GRASS_COLORS_LOCATION, new ColorMapReloadListener(TFCColors::setTallGrassColors, TFCColors.TALL_GRASS_COLORS_LOCATION));
        event.addListener(TFCColors.FOLIAGE_COLORS_LOCATION, new ColorMapReloadListener(TFCColors::setFoliageColors, TFCColors.FOLIAGE_COLORS_LOCATION));
        // Summer and ordinary foliage intentionally read the same PNG, but event keys identify consumers, not textures.
        event.addListener(Helpers.identifier("colormap/foliage_summer"), new ColorMapReloadListener(TFCColors::setFoliageSummerColors, TFCColors.FOLIAGE_SUMMER_COLORS_LOCATION));
        event.addListener(TFCColors.FOLIAGE_FALL_COLORS_LOCATION, new ColorMapReloadListener(TFCColors::setFoliageFallColors, TFCColors.FOLIAGE_FALL_COLORS_LOCATION));

        event.addListener(Helpers.identifier("stars"), new StarsReloadListener());
    }

    public static void registerParticleFactories(RegisterParticleProvidersEvent event)
    {
        event.registerSpriteSet(TFCParticles.BUBBLE.get(), BubbleParticle.Provider::new);
        event.registerSpriteSet(TFCParticles.BUBBLE_COLUMN_UP.get(), BubbleColumnUpParticle.Provider::new);
        event.registerSpriteSet(TFCParticles.BUBBLE_COLUMN_DOWN.get(), BubbleColumnDownParticle.Provider::new);
        event.registerSpriteSet(TFCParticles.WATER_FLOW.get(), WaterFlowParticle.Provider::new);
        event.registerSpriteSet(TFCParticles.STEAM.get(), SteamParticle.Provider::new);
        event.registerSpriteSet(TFCParticles.NITROGEN.get(), set -> new GlintParticleProvider(set, ChatFormatting.AQUA));
        event.registerSpriteSet(TFCParticles.PHOSPHORUS.get(), set -> new GlintParticleProvider(set, ChatFormatting.GOLD));
        event.registerSpriteSet(TFCParticles.POTASSIUM.get(), set -> new GlintParticleProvider(set, ChatFormatting.LIGHT_PURPLE));
        event.registerSpriteSet(TFCParticles.COMPOST_READY.get(), set -> new GlintParticleProvider(set, ChatFormatting.GRAY));
        event.registerSpriteSet(TFCParticles.COMPOST_ROTTEN.get(), set -> new GlintParticleProvider(set, ChatFormatting.DARK_RED));
        event.registerSpriteSet(TFCParticles.SLEEP.get(), SleepParticle.Provider::new);
        event.registerSpriteSet(TFCParticles.LEAF.get(), set -> new LeafParticle.Provider(set, true));
        event.registerSpriteSet(TFCParticles.SNOWFLAKE.get(), FallingLeafParticle.SimpleProvider::new);
        event.registerSpriteSet(TFCParticles.FLYING_SNOWFLAKE.get(), WindParticle.Provider::new);
        event.registerSpriteSet(TFCParticles.WIND.get(), WindParticle.Provider::new);
        event.registerSpriteSet(TFCParticles.FALLING_LEAF.get(), set -> new FallingLeafParticle.Provider(set, true));
        event.registerSpriteSet(TFCParticles.FEATHER.get(), set -> new LeafParticle.Provider(set, false));
        event.registerSpriteSet(TFCParticles.SPARK.get(), SparkParticle.Provider::new);
        TFCParticles.BUTTERFLIES.values().forEach(fly -> event.registerSpriteSet(fly.get(), AnimatedParticle.Provider::new));
        TFCParticles.MOTHS.values().forEach(moth -> event.registerSpriteSet(moth.get(), AnimatedParticle.Provider::new));
        event.registerSpriteSet(TFCParticles.FLUID_DRIP.get(), set -> FluidDripParticle.provider(set, FluidDripParticle.FluidHangParticle::new));
        event.registerSpriteSet(TFCParticles.FLUID_FALL.get(), set -> FluidDripParticle.provider(set, FluidDripParticle.FluidFallAndLandParticle::new));
        event.registerSpriteSet(TFCParticles.FLUID_LAND.get(), set -> FluidDripParticle.provider(set, FluidDripParticle.FluidLandParticle::new));
        event.registerSpriteSet(TFCParticles.BARREL_DRIP.get(), set -> FluidDripParticle.provider(set, FluidDripParticle.BarrelDripParticle::new));
        event.registerSpriteSet(TFCParticles.BARREL_SPILL.get(), set -> FluidDripParticle.provider(set, FluidDripParticle.BarrelSpillParticle::new));

        for (int i = 0; i < 5; i++)
        {
            final int lifetime = i * 80 + 10;
            event.registerSpriteSet(TFCParticles.SMOKES.get(i).get(), set -> new VariableHeightSmokeParticle.Provider(set, lifetime));
        }
    }

    public static void registerPresetEditors(RegisterPresetEditorsEvent event)
    {
        event.register(TerraFirmaCraft.PRESET, CreateTFCWorldScreen::new);
    }

    public static void registerExtensions(RegisterClientExtensionsEvent event)
    {
        event.registerItem(BlowpipeExtension.INSTANCE,
            TFCItems.BLOWPIPE.get(),
            TFCItems.BLOWPIPE_WITH_GLASS.get(),
            TFCItems.CERAMIC_BLOWPIPE.get(),
            TFCItems.CERAMIC_BLOWPIPE_WITH_GLASS.get());

        FluidModelRegistration.registerExtensions(event);
    }

    public static void registerDimensionEffects(RegisterCustomEnvironmentEffectRendererEvent event)
    {
        event.registerSkyboxRenderer(LevelRendererExtension.EFFECTS, LevelRendererExtension.INSTANCE);
        event.registerWeatherEffectRenderer(LevelRendererExtension.EFFECTS, LevelRendererExtension.INSTANCE);
    }
}
