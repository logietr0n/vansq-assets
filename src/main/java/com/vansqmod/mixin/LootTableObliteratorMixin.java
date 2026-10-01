package com.vansqmod.mixin;

import com.vansqmod.config.ObliteratorItemConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

/**
 * Drops Item Obliterator-blacklisted stacks from every loot table roll (chests, mobs,
 * archaeology, fishing, vaults, trial spawners, piglin bartering, etc.).
 */
@Mixin(LootTable.class)
public abstract class LootTableObliteratorMixin {

    @ModifyVariable(
            method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Consumer<ItemStack> vansqmod$filterBlacklistedLoot(Consumer<ItemStack> output, LootContext context) {
        return stack -> {
            if (!ObliteratorItemConfig.isBlocked(stack)) {
                output.accept(stack);
            }
        };
    }
}
