package dev.parapet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.parapet.Parapet;
import dev.parapet.block.ParrotCageBlock;
import dev.parapet.cage.CageMeal;
import dev.parapet.entity.ParrotEcology;
import dev.parapet.entity.PetParrotEntity;
import dev.parapet.entity.PetParrotActivity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

final class PetParrotRenderer<T extends PetParrotEntity> extends MobRenderer<T, PetParrotRenderer.State, PetParrotModel> {
    private final Map<String, Identifier> textures = new HashMap<>();
    private final Map<UUID, PostureTransition> transitions = new WeakHashMap<>();
    private final String textureDirectory;
    private final float modelScale;
    private final float wingTempo;

    static final class State extends LivingEntityRenderState {
        Identifier texture;
        boolean sitting;
        PetParrotActivity activity = PetParrotActivity.IDLE;
        PetParrotActivity outgoingPosture = PetParrotActivity.IDLE;
        float activityTicks;
        float outgoingTicks;
        float outgoingWeight;
        float flightAmount;
        float horizontalSpeed;
        float verticalSpeed;
        float bank;
        float individualPhase;
        float wingTempo;
        ParrotCageBlock.Size cage;
        CageBirdMotion cageMotion;
        CageMeal cageMeal;
        float speciesScale = 1F;
    }

    PetParrotRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer, String species,
                      float modelScale, ParrotClips.Size size) {
        super(context, new PetParrotModel(context.bakeLayer(layer), size, species),
            size.shadowRadius);
        textureDirectory = "textures/entity/" + species + "/";
        this.modelScale = modelScale;
        wingTempo = switch (species) {
            case "lovebird" -> 1.48F;
            case "budgerigar" -> 1.40F;
            case "green_cheek_conure", "caique" -> 1.12F;
            case "sun_conure" -> 1.06F;
            case "cockatiel" -> 1.10F;
            default -> 1.0F;
        };
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.texture = textures.computeIfAbsent(entity.getVisualPlumageId(), this::textureFor);
        state.sitting = entity.isInSittingPose();
        state.activity = entity.getActivity();
        state.activityTicks = entity.getActivityTicks(partialTick);
        state.flightAmount = entity.getFlightAmount(partialTick);
        state.horizontalSpeed = (float) Math.hypot(entity.getX() - entity.xo, entity.getZ() - entity.zo);
        state.verticalSpeed = (float) (entity.getY() - entity.yo);
        state.bank = Mth.clamp(Mth.wrapDegrees(entity.yBodyRot - entity.yBodyRotO) * Mth.DEG_TO_RAD, -0.40F, 0.40F);
        UUID individual = entity.getUUID();
        state.individualPhase = Math.floorMod(individual.hashCode(), 97) * 3.73F;
        state.wingTempo = wingTempo;
        state.outgoingPosture = PetParrotActivity.IDLE;
        state.outgoingTicks = 0F;
        state.outgoingWeight = 0F;
        state.cage = entity.getCageSize();
        if (state.cage != null) {
            state.speciesScale = modelScale;
            state.cageMotion = CageBirdMotion.forSpecies(ParrotEcology.forBird(entity));
            state.cageMeal = entity.getCageMeal();
            transitions.remove(individual);
            return;
        }
        PostureTransition transition = transitions.get(individual);
        if (transition == null && hasRelease(state.activity)) {
            transition = new PostureTransition();
            transitions.put(individual, transition);
        }
        if (transition != null && !transition.sample(state)) {
            transitions.remove(individual);
        }
    }

    private static boolean hasRelease(PetParrotActivity activity) {
        return activity == PetParrotActivity.BATHE || activity == PetParrotActivity.DRINK || activity.isNaturalDetail();
    }

    /** Keep a short release when a bird wakes, takes off or is interrupted halfway through a gesture. */
    private static final class PostureTransition {
        private PetParrotActivity previous = PetParrotActivity.IDLE;
        private PetParrotActivity releasing = PetParrotActivity.IDLE;
        private float previousAge, previousTicks, changedAt, releasedTicks;

        boolean sample(State state) {
            float gap = state.ageInTicks - previousAge;
            if (gap < 0F || gap >= 8F) {
                releasing = PetParrotActivity.IDLE;
            } else if (previous != state.activity && hasRelease(previous)) {
                releasing = previous;
                changedAt = state.ageInTicks;
                releasedTicks = previousTicks;
            }
            float elapsed = Math.max(0F, state.ageInTicks - changedAt);
            float duration = releasing == PetParrotActivity.NIGHT_ROOST ? 7F : 4F;
            if (elapsed >= duration) {
                releasing = PetParrotActivity.IDLE;
            }
            if (releasing != PetParrotActivity.IDLE) {
                state.outgoingPosture = releasing;
                state.outgoingTicks = Math.min(releasing.durationTicks(), releasedTicks + elapsed);
                float t = elapsed / duration;
                state.outgoingWeight = 1F - t * t * (3F - 2F * t);
            }
            previous = state.activity;
            previousAge = state.ageInTicks;
            previousTicks = state.activityTicks;
            return hasRelease(previous) || releasing != PetParrotActivity.IDLE;
        }
    }

    private Identifier textureFor(String plumage) {
        return Parapet.id(textureDirectory + plumage + ".png");
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return state.texture;
    }

    @Override
    protected void scale(State state, PoseStack pose) {
        // Vanilla copies the bird's shared getAgeScale() into this state and applies attribute scale separately.
        float size = modelScale * state.ageScale;
        pose.scale(size, size, size);
    }
}
