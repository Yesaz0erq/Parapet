package dev.parapet.client;

import dev.parapet.Parapet;
import dev.parapet.ParapetFurniture;
import dev.parapet.entity.AfricanGreyEntity;
import dev.parapet.entity.CaiqueEntity;
import dev.parapet.entity.CockatielEntity;
import dev.parapet.entity.EclectusEntity;
import dev.parapet.entity.GreenCheekConureEntity;
import dev.parapet.entity.LovebirdEntity;
import dev.parapet.entity.MonkParakeetEntity;
import dev.parapet.entity.PetParrotEntity;
import dev.parapet.entity.SulfurCrestedCockatooEntity;
import dev.parapet.entity.SunConureEntity;
import java.util.Set;
import java.util.function.Supplier;
import dev.parapet.guide.ParrotFieldGuide;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;

public final class ParapetClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ParrotKeys.initialize();
        ClientPlayNetworking.registerGlobalReceiver(ParrotFieldGuide.Open.TYPE, (payload, context) ->
            context.client().gui.setScreen(new ParrotFieldGuideScreen(Set.copyOf(payload.species()))));
        BlockEntityRendererRegistry.register(ParapetFurniture.CAGE_FITTINGS, CageFittingsRenderer::new);
        CageWorkbenchScreen.register();
        ParrotInteractionScreen.register();
        register(Parapet.BUDGERIGAR, "budgerigar", BudgerigarModel::createBodyLayer, 0.84F, ParrotClips.Size.SMALL);
        register(Parapet.COCKATIEL, "cockatiel", ParrotShapes::cockatiel, CockatielEntity.MODEL_SCALE, ParrotClips.Size.MEDIUM);
        register(Parapet.GREEN_CHEEK_CONURE, "green_cheek_conure", ParrotShapes::greenCheekConure,
            GreenCheekConureEntity.MODEL_SCALE, ParrotClips.Size.MEDIUM);
        register(Parapet.LOVEBIRD, "lovebird", ParrotShapes::lovebird, LovebirdEntity.MODEL_SCALE, ParrotClips.Size.SMALL);
        register(Parapet.MONK_PARAKEET, "monk_parakeet", ParrotShapes::monkParakeet, MonkParakeetEntity.MODEL_SCALE, ParrotClips.Size.MEDIUM);
        register(Parapet.CAIQUE, "caique", CaiqueModel::createBodyLayer, CaiqueEntity.MODEL_SCALE, ParrotClips.Size.MEDIUM);
        register(Parapet.SUN_CONURE, "sun_conure", SunConureModel::createBodyLayer,
            SunConureEntity.MODEL_SCALE, ParrotClips.Size.MEDIUM);
        register(Parapet.ECLECTUS, "eclectus", EclectusModel::createBodyLayer,
            EclectusEntity.MODEL_SCALE, ParrotClips.Size.LARGE);
        register(Parapet.AFRICAN_GREY, "african_grey", AfricanGreyModel::createBodyLayer,
            AfricanGreyEntity.MODEL_SCALE, ParrotClips.Size.MEDIUM);
        register(Parapet.SULFUR_CRESTED_COCKATOO, "sulfur_crested_cockatoo", SulfurCrestedCockatooModel::createBodyLayer,
            SulfurCrestedCockatooEntity.MODEL_SCALE, ParrotClips.Size.LARGE);
        for (MacawBody body : MacawBody.values()) {
            body.registerLayer();
        }
        Parapet.MACAWS.forEach((species, type) -> {
            MacawBody body = MacawBody.forSpecies(species);
            EntityRenderers.register(type, context -> new PetParrotRenderer<>(context, body.layer,
                "macaw", body.modelScale(species), body.animationSize));
        });
        Parapet.COMPANION_PARROTS.forEach((species, type) -> {
            ParrotClips.Size size = switch (species) {
                case BOURKES_PARROT -> ParrotClips.Size.SMALL;
                case BLUE_FRONTED_AMAZON, YELLOW_HEADED_AMAZON, ORANGE_WINGED_AMAZON -> ParrotClips.Size.LARGE;
                default -> ParrotClips.Size.MEDIUM;
            };
            register(type, species.id(), () -> CompanionGeometry.createBodyLayer(species.id()), species.modelScale(), size);
        });
    }

    private static <T extends PetParrotEntity> void register(EntityType<T> type, String species,
                                                              Supplier<LayerDefinition> geometry, float modelScale, ParrotClips.Size size) {
        ModelLayerLocation layer = new ModelLayerLocation(Parapet.id(species), "main");
        ModelLayerRegistry.registerModelLayer(layer, geometry::get);
        EntityRenderers.register(type, context -> new PetParrotRenderer<>(context, layer, species, modelScale, size));
    }
}
