package dev.parapet.client;

import dev.parapet.entity.PetParrotActivity;
import dev.parapet.cage.CageMeal;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

final class PetParrotModel extends EntityModel<PetParrotRenderer.State> {
    private final ParrotClips clips;
    private final ParrotClips.Size size;
    private final ModelPart bird;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart crest;
    private final ModelPart tail;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final CageFeedingPose cageFeeding;

    PetParrotModel(ModelPart root, ParrotClips.Size size, String species) {
        super(root);
        this.size = size;
        clips = new ParrotClips(root, size, species);
        bird = root.getChild("bird");
        body = bird.getChild("body");
        head = bird.getChild("head");
        crest = head.hasChild("crest") ? head.getChild("crest") : null;
        tail = bird.getChild("tail");
        leftWing = bird.getChild("left_wing");
        rightWing = bird.getChild("right_wing");
        leftLeg = bird.getChild("left_leg");
        rightLeg = bird.getChild("right_leg");
        cageFeeding = new CageFeedingPose(root, head, clips.cageFeed);
    }

    @Override
    public void setupAnim(PetParrotRenderer.State state) {
        super.setupAnim(state);
        float time = state.ageInTicks + state.individualPhase;
        if (state.cage != null) {
            animateInCage(state, time);
            return;
        }
        float flight = state.flightAmount;
        float grounded = 1 - flight;
        float actionFade = Math.min(state.activityTicks, state.activity.durationTicks() - state.activityTicks);
        float action = grounded * Mth.clamp(actionFade / 3.0F, 0F, 1F);
        // The night pose holds its final keyframe until dawn; the head stays in the shoulder throughout.
        if (state.activity == PetParrotActivity.NIGHT_ROOST) {
            action = grounded * Mth.clamp(state.activityTicks / 12F, 0F, 1F);
        }
        action = Math.max(action, grounded * state.outgoingWeight);
        float freedomToLook = 1 - action * 0.85F;
        head.yRot += Mth.clamp(state.yRot, -65F, 65F) * Mth.DEG_TO_RAD * freedomToLook;
        head.xRot += Mth.clamp(state.xRot, -35F, 40F) * Mth.DEG_TO_RAD * freedomToLook;

        if (grounded > 0) {
            animateOnGround(state, time, grounded);
            if (state.outgoingWeight > 0F) {
                animatePosture(state.outgoingPosture, state.outgoingTicks, state.individualPhase, time,
                    grounded * state.outgoingWeight);
            }
        }
        if (flight > 0) {
            // Sustained forward movement allows gliding. Hovering and climbing require full wingbeats.
            float cruise = Mth.clamp((state.horizontalSpeed - 0.045F) * 12F, 0F, 1F);
            float climbing = Mth.clamp((state.verticalSpeed + 0.018F) * 14F, 0F, 1F);
            float glide = cruise * (1 - climbing) * Mth.clamp(0.64F + 0.55F * Mth.sin(time * 0.065F), 0F, 1F);
            long wingTime = (long) (time * 50F * state.wingTempo);
            clips.flap.apply(wingTime, flight * (1 - glide));
            clips.glide.apply((long) (time * 50F), flight * glide);
            bird.zRot += state.bank * flight;
            head.zRot -= state.bank * flight * 0.65F;
            tail.xRot -= Mth.clamp(state.verticalSpeed, -0.18F, 0.18F) * flight;
        }
        if (crest != null) {
            if (clips.crestDisplay != null) {
                crest.xRot -= 0.21F * flight;
                if (state.activity != PetParrotActivity.RAISE_CREST && state.activity != PetParrotActivity.GREET) {
                    crest.xRot += 0.06F * grounded * Mth.sin(time * 0.12F);
                }
            } else {
                float perk = state.activity == PetParrotActivity.GREET
                    ? Mth.sin(Mth.PI * state.activityTicks / PetParrotActivity.GREET.durationTicks()) : 0F;
                crest.xRot -= 0.16F * perk;
                crest.xRot += 0.26F * flight + 0.025F * Mth.sin(time * 0.13F);
            }
        }
    }

    private void animateInCage(PetParrotRenderer.State state, float time) {
        CageBirdMotion motion = state.cageMotion;
        // Tighter quarters keep the tail and head turns closer to the perch.
        float room = switch (state.cage) {
            case STANDARD -> 0.62F;
            case LARGE -> 0.76F;
            case DELUXE -> 0.86F;
            case SUPER_DELUXE -> 0.94F;
            case GIANT -> 1F;
        };
        float phase = state.activity == PetParrotActivity.FORAGE
            ? Mth.clamp(state.activityTicks / PetParrotActivity.FORAGE.durationTicks(), 0F, 1F) : 0F;
        float reach = Mth.clamp(Math.min(phase, 1F - phase) * 5F, 0F, 1F);
        reach = reach * reach * (3F - 2F * reach);
        float idleWeight = 1F - reach;
        float idleTime = time * motion.idleRate * (0.88F + 0.12F * room);
        clips.cageIdle.apply(Math.round(idleTime * 50F), room * idleWeight);
        float glance = Mth.sin(idleTime * 0.077F) * idleWeight * room;
        tail.zRot += 0.035F * motion.tailBalance * glance;
        head.zRot += 0.04F * glance;
        if (crest != null) crest.xRot += (motion.style == CageBirdMotion.Style.CREST ? 0.095F : 0.035F) * glance;
        if (reach == 0F) return;

        // Retiming only the interior keeps the final swallow lined up with the server's meal end.
        float clipPhase = phase + (motion.idleRate - 1F) * Mth.sin(phase * Mth.TWO_PI) * 0.30F;
        float units = Math.max(.001F, state.speciesScale * state.ageScale * state.scale / 16F);
        var approach = cageFeeding.approach(state, units);
        clips.cageFeed.apply(Math.round(clipPhase * 1600F), reach * approach.weight());
        CageFeedingPose.moveFeet(approach, units, phase, bird, leftLeg, rightLeg);
        if (state.cageMeal != CageMeal.HAND) bird.z += reach * state.seatOffset / units;
        bird.yRot += approach.bodyYaw() * reach;
        head.yRot += approach.headYaw() * reach;
        // The tail stays nearer the main rail as the breast turns towards a lateral feeder.
        tail.yRot -= approach.bodyYaw() * reach * (state.cage.depth == 1 ? .76F : .51F);
        float peck = Mth.sin(clipPhase * Mth.TWO_PI * 2F) * reach;
        tail.xRot -= 0.066F * motion.tailBalance * room * peck;
        if (state.cageMeal == CageMeal.WATER) {
            head.xRot -= 0.11F * Math.max(0F, -peck);
        } else {
            switch (motion.style) {
                case SEED -> head.xRot += 0.065F * Mth.sin(clipPhase * Mth.TWO_PI * 5F) * reach;
                case CREST -> { if (crest != null) crest.xRot -= 0.13F * peck; }
                case CONURE -> head.zRot += 0.09F * peck;
                case BROAD_BILL -> head.yRot += 0.12F * peck;
                case LORY -> head.z -= 0.42F * Math.max(0F, peck);
                case LONG_TAIL -> tail.zRot += 0.09F * room * peck;
                case MACAW -> { head.zRot += 0.10F * peck; tail.yRot -= 0.05F * room * peck; }
            }
        }
    }

    private void animateOnGround(PetParrotRenderer.State state, float time, float weight) {
        if (state.sitting) {
            bird.y += weight * 1.05F;
            leftLeg.y -= weight * 1.05F;
            rightLeg.y -= weight * 1.05F;
            tail.xRot += weight * 0.17F;
        }
        long actionTime = (long) (state.activityTicks * 50F);
        switch (state.activity) {
            case NIGHT_ROOST, RUFFLE, LISTEN, CONTACT_CALL -> {
                // Keep the movement at the perch subtle, particularly while listening or sleeping.
                float idle = switch (state.activity) {
                    case NIGHT_ROOST, LISTEN -> 0.025F;
                    default -> 0.12F;
                };
                clips.idle.apply((long) (time * 50F), weight * idle);
                animatePosture(state.activity, state.activityTicks, state.individualPhase, time,
                    weight * (1F - state.outgoingWeight));
            }
            case FORAGE -> clips.forage.apply(actionTime, weight);
            case PREEN -> clips.preen.apply(actionTime, weight);
            case BATHE -> clips.bathe.apply(actionTime, weight * (1F - state.outgoingWeight));
            case DRINK -> clips.drink.apply(actionTime, weight * (1F - state.outgoingWeight));
            case STROKE -> {
                clips.stroke.apply(actionTime, weight);
                if (crest != null) {
                    float phase = Mth.clamp(state.activityTicks / state.activity.durationTicks(), 0F, 1F);
                    crest.xRot -= 0.22F * weight * Mth.sin(Mth.PI * phase);
                }
            }
            case GREET -> {
                clips.greet.apply(actionTime, weight);
                if (clips.crestDisplay != null) {
                    long crestTime = actionTime * PetParrotActivity.RAISE_CREST.durationTicks()
                        / PetParrotActivity.GREET.durationTicks();
                    clips.crestDisplay.apply(crestTime, weight);
                }
            }
            case BEG -> {
                if (clips.beg != null) {
                    clips.beg.apply(actionTime, weight);
                } else {
                    clips.idle.apply((long) (time * 50F), weight);
                }
            }
            case RAISE_CREST -> {
                clips.idle.apply((long) (time * 50F), weight);
                if (clips.crestDisplay != null) {
                    clips.crestDisplay.apply(actionTime, weight);
                }
            }
            case TOUCH_TARGET, STRETCH, HIGH_FIVE, TURN -> {
                clips.idle.apply((long) (time * 50F), weight);
                float phase = Mth.clamp(state.activityTicks / state.activity.durationTicks(), 0F, 1F);
                float reach = Mth.sin(Mth.PI * phase) * weight;
                switch (state.activity) {
                    case TOUCH_TARGET -> {
                        float tap = reach * reach;
                        bird.xRot += tap * 0.13F;
                        head.z -= tap * 0.75F;
                        head.xRot += tap * 0.52F;
                        tail.xRot -= tap * 0.12F;
                    }
                    case STRETCH -> {
                        leftWing.zRot -= reach * 1.04F;
                        rightWing.zRot += reach * 1.04F;
                        leftWing.xRot -= reach * 0.22F;
                        rightWing.xRot -= reach * 0.22F;
                        head.xRot -= reach * 0.17F;
                    }
                    case HIGH_FIVE -> {
                        bird.zRot += reach * 0.10F;
                        rightLeg.y -= reach * 1.25F;
                        rightLeg.xRot -= reach * 0.95F;
                        leftWing.zRot -= reach * 0.18F;
                        head.zRot -= reach * 0.14F;
                    }
                    case TURN -> {
                        float turn = phase * phase * (3F - 2F * phase);
                        bird.yRot += turn * Mth.TWO_PI * weight;
                        float step = reach * Mth.sin(phase * Mth.TWO_PI * 3F);
                        leftLeg.xRot += step * 0.32F;
                        rightLeg.xRot -= step * 0.32F;
                        tail.zRot -= step * 0.08F;
                    }
                    default -> {}
                }
            }
            case IDLE -> {
                float walking = state.sitting ? 0F : Mth.clamp(state.walkAnimationSpeed, 0F, 1F);
                clips.idle.apply((long) (time * 50F), weight * (1 - walking) * (1F - state.outgoingWeight));
                float step = weight * walking * Mth.sin(state.walkAnimationPos * size.strideRate);
                leftLeg.xRot += size.stepAngle * step;
                rightLeg.xRot -= size.stepAngle * step;
                head.y -= 0.26F * Math.abs(step);
                bird.zRot += size.walkSway * step;
            }
        }
    }

    private void animatePosture(PetParrotActivity activity, float ticks, float individualPhase, float time, float weight) {
        float duration = activity.durationTicks();
        float phase = Mth.clamp(ticks / duration, 0F, 1F);
        // Change the timing of interior accents, keeping both authored endpoints exactly in sync.
        float accent = 0.10F * Mth.sin(individualPhase) * phase * (1F - phase);
        long clipTime = Math.round((phase + accent) * duration * 50F);
        switch (activity) {
            case NIGHT_ROOST -> {
                clips.nightRoost.apply(clipTime, weight);
                float breath = Mth.sin(time * 0.075F * size.strideRate) * weight * phase;
                body.y -= 0.12F * breath;
                body.xRot += 0.010F * breath;
                head.y -= 0.065F * breath;
                leftWing.y -= 0.055F * breath;
                rightWing.y -= 0.055F * breath;
            }
            case RUFFLE -> clips.ruffle.apply(clipTime, weight);
            case LISTEN -> clips.listen.apply(clipTime, weight);
            case CONTACT_CALL -> clips.contactCall.apply(clipTime, weight);
            case BATHE -> clips.bathe.apply(Math.round(ticks * 50F), weight);
            case DRINK -> clips.drink.apply(Math.round(ticks * 50F), weight);
            default -> {}
        }
    }
}
