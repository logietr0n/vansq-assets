package com.vansqmod.mixin.vanillabackport;

import com.vansqmod.entity.RareChickenVariants;
import com.vansqmod.registry.ModItems;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Chicken.class)
public abstract class RareChickenEggDropMixin {

    @ModifyArg(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/Chicken;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"
            )
    )
    private ItemLike vansqmod$rareEgg(ItemLike original) {
        if (original == Items.AIR) {
            return original;
        }
        if (RareChickenVariants.isRare((Chicken) (Object) this)) {
            return ModItems.RARE_EGG.get();
        }
        return original;
    }
}
