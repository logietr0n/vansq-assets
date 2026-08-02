package com.vansqmod.mixin;

import com.vansqmod.entity.PigLitters;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Track golden-carrot feeding for litter size boost (Quark Pig Litters parity). */
@Mixin(Animal.class)
public class AnimalGoldenCarrotEatMixin {

    @Inject(method = "usePlayerItem", at = @At("HEAD"))
    private void vansqmod$markGoldenCarrotEat(Player player, InteractionHand hand, ItemStack stack, CallbackInfo ci) {
        Animal self = (Animal) (Object) this;
        if (self instanceof Pig && !stack.isEmpty() && stack.is(Items.GOLDEN_CARROT)) {
            PigLitters.markAteGoldenCarrot(self);
        }
    }
}
