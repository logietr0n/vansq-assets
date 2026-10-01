package com.vansqmod.mixin;

import com.vansqmod.compat.TweedDrops;
import com.vansqmod.registry.ModItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When Farmer's Delight's {@code add_item} loot modifier would add straw
 * (grass, wheat, rice, sandy shrub, vansqtweaks extras), 1/8196 of those
 * harvests drop tweed instead.
 */
@Mixin(targets = "vectorwing.farmersdelight.common.loot.modifier.AddItemModifier", remap = false)
public class FarmersDelightTweedAddItemMixin {

    @Shadow
    @Final
    private Item addedItem;

    @Shadow
    @Final
    private int count;

    @Inject(method = "doApply", at = @At("HEAD"), cancellable = true)
    private void vansqmod$maybeTweed(
            ObjectArrayList<ItemStack> generatedLoot,
            LootContext context,
            CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        if (!TweedDrops.isStraw(addedItem) || !TweedDrops.shouldReplace(context.getRandom())) {
            return;
        }
        generatedLoot.add(new ItemStack(ModItems.TWEED.get(), Math.max(1, count)));
        cir.setReturnValue(generatedLoot);
    }
}
