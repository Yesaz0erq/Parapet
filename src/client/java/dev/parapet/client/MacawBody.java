package dev.parapet.client;

import dev.parapet.Parapet;
import dev.parapet.entity.MacawSpecies;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

enum MacawBody {
    ARA("macaw_ara", MacawAraModel::createBodyLayer, 0.01569309F, ParrotClips.Size.LARGE),
    SHORT_ARA("macaw_short_ara", MacawShortAraModel::createBodyLayer, 0.02095898F, ParrotClips.Size.LARGE),
    HEAVY("macaw_heavy", MacawHeavyModel::createBodyLayer, 0.01554399F, ParrotClips.Size.LARGE),
    SLENDER("macaw_slender", MacawSlenderModel::createBodyLayer, 0.02032883F, ParrotClips.Size.LARGE),
    MINI("macaw_mini", MacawMiniModel::createBodyLayer, 0.02638974F, ParrotClips.Size.MEDIUM);

    final ModelLayerLocation layer;
    final ParrotClips.Size animationSize;
    private final Supplier<LayerDefinition> geometry;
    private final float scalePerCm;

    MacawBody(String id, Supplier<LayerDefinition> geometry, float scalePerCm, ParrotClips.Size animationSize) {
        this.layer = new ModelLayerLocation(Parapet.id(id), "main");
        this.geometry = geometry;
        this.scalePerCm = scalePerCm;
        this.animationSize = animationSize;
    }

    void registerLayer() {
        ModelLayerRegistry.registerModelLayer(layer, geometry::get);
    }

    // Each rig has a different bill-to-tail length at scale 1.
    float modelScale(MacawSpecies species) {
        return scalePerCm * species.lengthCm();
    }

    static MacawBody forSpecies(MacawSpecies species) {
        return switch (species) {
            case BLUE_AND_YELLOW, BLUE_THROATED, MILITARY, GREAT_GREEN, SCARLET, RED_AND_GREEN -> ARA;
            case CHESTNUT_FRONTED, RED_FRONTED -> SHORT_ARA;
            case HYACINTH, LEAR -> HEAVY;
            case SPIX, RED_BELLIED -> SLENDER;
            case BLUE_HEADED, BLUE_WINGED, YELLOW_COLLARED, RED_SHOULDERED -> MINI;
        };
    }
}
