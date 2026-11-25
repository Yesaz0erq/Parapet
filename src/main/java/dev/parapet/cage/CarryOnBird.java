package dev.parapet.cage;

import dev.parapet.block.ParrotCageBlock;
import dev.parapet.entity.ParrotEcology;
import dev.parapet.entity.PetParrotEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

/** Loaded only when Carry On is installed. */
final class CarryOnBird {
    static boolean isHeld(Player player) {
        var data = CarryOnDataManager.getCarryData(player);
        if (!data.isCarrying(CarryOnData.CarryType.ENTITY)) return false;
        var entity = data.getContentNbt();
        return entity != null && entity.getString("id").map(Identifier::tryParse)
            .flatMap(ParrotEcology::forEntityId).isPresent();
    }

    static boolean deposit(ServerLevel level, Player player, ParrotCageBlock cage, BlockPos root, Direction facing) {
        var data = CarryOnDataManager.getCarryData(player);
        if (!(data.getEntity(level) instanceof PetParrotEntity bird) || !bird.isAlive() || !bird.isOwnedBy(player)
            || level.getEntityInAnyDimension(bird.getUUID()) != null) return false;
        int needed = CageRoost.units(ParrotEcology.forBird(bird).size());
        for (var resident : CageRoost.residents(level, root)) {
            if (!resident.isOwnedBy(player)) return false;
            needed += CageRoost.units(ParrotEcology.forBird(resident).size());
        }
        if (needed > CageRoost.units(cage.size().maxBirdSize)) return false;
        bird.getCageRoost().settle(level, bird, cage, root, facing);
        if (!level.addFreshEntity(bird)) return false;
        data.clear();
        CarryOnDataManager.setCarryData(player, data);
        player.removeEffect(MobEffects.SLOWNESS);
        return true;
    }
}
