package com.vansqmod.mixin.variantsandventures;

import com.vansqmod.compat.FrostedCavesGelidConversion;
import com.vansqmod.compat.GelidSnowballs;
import com.vansqmod.debug.VansqDebugState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gelids always get an offhand snowball after zombie equipment. If the zombie weapon
 * roll hit, drop the snowball so they melee instead of throwing. Unarmed Gelids keep
 * the snowball, with a chance to hold an ice snowball instead.
 */
@Mixin(targets = "com.faboslav.variantsandventures.common.entity.mob.GelidEntity")
public abstract class GelidSnowballWeaponMixin {

    @Inject(method = "populateDefaultEquipmentSlots", at = @At("TAIL"))
    private void vansqmod$weaponOverridesSnowball(RandomSource random, DifficultyInstance difficulty, CallbackInfo ci) {
        Mob gelid = (Mob) (Object) this;
        if (!gelid.getMainHandItem().isEmpty()) {
            gelid.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            return;
        }
        if (!gelid.getOffhandItem().is(Items.SNOWBALL)) {
            return;
        }
        float chance = gelid instanceof Zombie zombie && FrostedCavesGelidConversion.inFrostedCaves(zombie)
                ? GelidSnowballs.ICE_HOLD_CHANCE_FROSTED_CAVES
                : GelidSnowballs.ICE_HOLD_CHANCE;
        if (!VansqDebugState.rareEventSucceeds(random, chance)) {
            return;
        }
        ItemStack ice = GelidSnowballs.iceSnowballStack();
        if (!ice.isEmpty()) {
            gelid.setItemSlot(EquipmentSlot.OFFHAND, ice);
        }
    }
}
