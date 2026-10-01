package com.vansqmod.mixin;

import com.vansqmod.entity.MobSpawnArmor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Priority 2000 so this RETURN runs after C&amp;C's TAIL chainmail→copper swap. */
@Mixin(value = Mob.class, priority = 2000)
public abstract class MobSpawnArmorMixin {

    @Inject(method = "populateDefaultEquipmentSlots", at = @At("RETURN"))
    private void vansqmod$endArmorTier(RandomSource random, DifficultyInstance difficulty, CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        MobSpawnArmor.restoreChainmailFromCcCopper(mob);
        MobSpawnArmor.endSpawnRoll();
    }

    @ModifyArg(
            method = "populateDefaultEquipmentSlots",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;getEquipmentForSlot(Lnet/minecraft/world/entity/EquipmentSlot;I)Lnet/minecraft/world/item/Item;"
            ),
            index = 1
    )
    private int vansqmod$insertArmorTiers(int vanillaQuality) {
        Mob mob = (Mob) (Object) this;
        return MobSpawnArmor.currentQuality(mob, mob.getRandom(), vanillaQuality);
    }

    @Inject(method = "getEquipmentForSlot", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$extraArmorTiers(
            EquipmentSlot slot,
            int quality,
            CallbackInfoReturnable<Item> cir
    ) {
        Item extra = MobSpawnArmor.extraTierItem(slot, quality);
        if (extra != null) {
            cir.setReturnValue(extra);
        }
    }
}
