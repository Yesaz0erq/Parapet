package dev.parapet.entity;

public enum PetParrotActivity {
    IDLE(0), FORAGE(32), PREEN(56), GREET(30), BEG(32), RAISE_CREST(36), BATHE(72),
    STROKE(30), TOUCH_TARGET(32), STRETCH(46), HIGH_FIVE(34), TURN(42),
    NIGHT_ROOST(40), RUFFLE(28), LISTEN(32), CONTACT_CALL(24);

    private static final PetParrotActivity[] ACTIVITIES = values();
    private final int duration;

    PetParrotActivity(int duration) {
        this.duration = duration;
    }

    public boolean isOwnerTrick() {
        return switch (this) {
            case STROKE, TOUCH_TARGET, STRETCH, HIGH_FIVE, TURN -> true;
            default -> false;
        };
    }

    public int durationTicks() {
        return duration;
    }

    public boolean isNaturalDetail() {
        return this == NIGHT_ROOST || this == RUFFLE || this == LISTEN || this == CONTACT_CALL;
    }

    static PetParrotActivity byIndex(int index) {
        return index >= 0 && index < ACTIVITIES.length ? ACTIVITIES[index] : IDLE;
    }
}
