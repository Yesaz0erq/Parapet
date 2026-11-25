package dev.parapet.cage;

import dev.parapet.block.ParrotCageBlock.Size;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Coordinates in blocks from the north-facing cage base. Local +X runs along the main perch. */
public final class CageLayout {
    public static final double FLOOR = 2.02 / 16.0;
    private CageLayout() {}

    public static Vec3 perch(Size size) {
        return new Vec3(.64 * size.width, size.perchHeight, .5 * size.depth);
    }

    /** Centreline of the short feeding branch, perpendicular to the main perch. */
    public static double feedingBranchX(Size size) {
        return perch(size).x + (size == Size.STANDARD ? .10 : size == Size.LARGE ? .20 : .29);
    }

    /** Distance from the main perch to either end, in blocks along local Z. */
    public static double feedingBranchSpan(Size size) {
        return size == Size.STANDARD ? .16 : size == Size.LARGE ? .22 : .35;
    }

    /** Square cross-section shared by the visual branch and its collision shape. */
    public static double feedingBranchThickness(Size size) {
        return size == Size.STANDARD ? .06 : size == Size.LARGE ? .08 : .10;
    }

    public static float dishScale(Size size) {
        return size == Size.STANDARD ? .245F : size == Size.LARGE ? .32F : .56F;
    }

    public static float toyScale(Size size) {
        return size == Size.STANDARD ? .265F : size == Size.LARGE ? .39F : .68F;
    }

    /** Top of the shelf holding the porcelain foot, measured from the cage floor origin. */
    public static double shelfHeight(Size size) {
        return size.perchHeight + dishScale(size) * .22;
    }

    /** Surface the bill can touch, with the water directly in front and the two feeds on its sides. */
    public static Vec3 mealPosition(Size size, CageMeal meal) {
        Vec3 perch = perch(size);
        double forward = size == Size.STANDARD ? .184 : size == Size.LARGE ? .40 : .52;
        double spacing = size == Size.STANDARD ? .225 : size == Size.LARGE ? .28 : .47;
        double side = meal == CageMeal.SEED ? -spacing : meal == CageMeal.PREMIUM ? spacing : 0.0;
        double surface = meal == CageMeal.WATER ? 3.33 / 16.0 : .17;
        return new Vec3(perch.x + forward, shelfHeight(size) + surface * dishScale(size), perch.z + side);
    }

    /** Model anchor relative to an item model's centre, in blocks, before uniform scaling. */
    public enum ToyMount {
        FLOOR(new Vec3(0, -.5, 0)),
        SIDE(new Vec3(0, 4.4 / 16, 6.0 / 16)),
        ROOF(new Vec3(0, 7.0 / 16, 0));

        public final Vec3 modelAnchor;
        ToyMount(Vec3 modelAnchor) { this.modelAnchor = modelAnchor; }
    }

    public record ToyPlacement(Vec3 anchor, ToyMount mount, float yaw, float scale) {}

    /** All three overhead hooks encircle a roof rod. Rear and roof rods use the same X spacing. */
    private static double barX(Size size, double fraction) {
        double first = switch (size) {
            case STANDARD -> .1855;
            case LARGE -> .183625;
            case GIANT -> .206875;
            default -> .193125;
        };
        double step = switch (size) {
            case STANDARD -> .131667;
            case LARGE -> .127842;
            case GIANT -> .174375;
            default -> .146841;
        };
        return first + step * Math.round((size.width * fraction - first) / step);
    }

    private static double sideBarZ(Size size) {
        return switch (size) {
            case STANDARD, LARGE -> .3190625;
            case DELUXE -> .64334375;
            case SUPER_DELUXE -> .928875;
            case GIANT -> .906;
        };
    }

    public static ToyPlacement toyPlacement(Size size, CageToy toy) {
        float scale = toyScale(size);
        double rear = size.depth - .86 / 16;
        // The lower face of the hook's top bar rests on the upper face of the cage rod.
        double roof = size.height - (.43 + 1.2 * scale) / 16;
        return switch (toy) {
            case FORAGING_BALL -> new ToyPlacement(
                new Vec3(size.width * .49, FLOOR, size.depth * .20), ToyMount.FLOOR, 0, scale);
            case BEAK_RUBBING_PERCH -> new ToyPlacement(
                new Vec3(.9 / 16, size.perchHeight + .48 * scale, sideBarZ(size)), ToyMount.SIDE, -90, scale);
            case CHEW_BLOCKS -> new ToyPlacement(
                new Vec3(barX(size, .59), size.perchHeight + .71 * scale, rear), ToyMount.SIDE, 0, scale);
            case MINERAL_CHEW_CHARM -> new ToyPlacement(
                new Vec3(barX(size, .29), size.perchHeight + .88 * scale, rear), ToyMount.SIDE, 0, scale);
            case ROPE_SWING -> new ToyPlacement(
                new Vec3(barX(size, .23), roof, size.depth * .38), ToyMount.ROOF, 0, scale);
            case DOUBLE_RING_SWING -> new ToyPlacement(
                new Vec3(barX(size, .53), roof, size.depth * .72), ToyMount.ROOF, 0, scale);
            case LARGE_CHEW_MOBILE -> new ToyPlacement(
                new Vec3(barX(size, .82), roof, size.depth * .56), ToyMount.ROOF, 0, scale);
        };
    }

    public static Vec3 toWorld(BlockPos base, Direction facing, Vec3 local) {
        return Vec3.atBottomCenterOf(base).relative(facing.getClockWise(), local.x - .5)
            .relative(facing.getOpposite(), local.z - .5).add(0, local.y, 0);
    }
}
