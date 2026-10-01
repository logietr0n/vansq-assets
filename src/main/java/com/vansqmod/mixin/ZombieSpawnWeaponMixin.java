package com.vansqmod.mixin;

import com.vansqmod.entity.MobSpawnWeapons;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces Barched/vanilla zombie mainhand selection after it runs. Zombified piglins
 * use their own equipment method and are skipped. Gelids/thickets call this via super.
 */
@Mixin(Zombie.class)
public abstract class ZombieSpawnWeaponMixin {

    @Inject(method = "populateDefaultEquipmentSlots", at = @At("TAIL"), order = 2000)
    private void vansqmod$rollSpawnWeapon(RandomSource random, DifficultyInstance difficulty, CallbackInfo ci) {
        Zombie zombie = (Zombie) (Object) this;
        if (zombie instanceof ZombifiedPiglin) {
            return;
        }
        zombie.setItemSlot(EquipmentSlot.MAINHAND, MobSpawnWeapons.rollZombieMainhand(zombie, random, difficulty));
    }
}
