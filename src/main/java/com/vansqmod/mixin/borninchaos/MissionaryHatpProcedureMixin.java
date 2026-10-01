package com.vansqmod.mixin.borninchaos;

import com.vansqmod.integration.missionaryhat.MissionaryHatEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Hat-on-hit summon (controlled baby skeleton) also counts the Curios {@code head} slot.
 * Must target the private Event overload; the public {@code execute} only forwards and has no slot check.
 */
@Mixin(
        targets = "net.mcreator.borninchaosv.procedures.MissionaryHatpProcedure",
        remap = false
)
public abstract class MissionaryHatpProcedureMixin {

    private static final String EXECUTE =
            "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V";

    @Redirect(
            method = EXECUTE,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;",
                    remap = true
            ),
            remap = false
    )
    private static ItemStack vansqmod$includeCuriosHat(LivingEntity entity, EquipmentSlot slot) {
        ItemStack vanilla = entity.getItemBySlot(slot);
        if (slot == EquipmentSlot.HEAD && !MissionaryHatEquipment.isHat(vanilla)) {
            return MissionaryHatEquipment.getCuriosHat(entity).orElse(vanilla);
        }
        return vanilla;
    }
}
