package com.vansqmod.mixin.borninchaos;

import com.vansqmod.integration.charm.CharmTotemEquipment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Totem of Spite (death totem) also consumes a Charm-slot stack, not only hands.
 */
@Mixin(
        targets = "net.mcreator.borninchaosv.procedures.DeathTotemKoghdaPriedmietNakhoditsiaVRukieProcedure",
        remap = false
)
public abstract class DeathTotemCharmSlotMixin {

    @Redirect(
            method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getOffhandItem()Lnet/minecraft/world/item/ItemStack;",
                    remap = true
            ),
            remap = false
    )
    private static ItemStack vansqmod$offhandOrCharm(LivingEntity entity) {
        ItemStack offhand = entity.getOffhandItem();
        if (CharmTotemEquipment.isDeathTotem(offhand) || CharmTotemEquipment.isDeathTotem(entity.getMainHandItem())) {
            return offhand;
        }
        return CharmTotemEquipment.findCharmDeathTotem(entity).orElse(offhand);
    }
}
