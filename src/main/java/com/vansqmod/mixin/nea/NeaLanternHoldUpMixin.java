package com.vansqmod.mixin.nea;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LanternBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

/**
 * Not Enough Animations only raises the arm for items in {@code holdingItems}.
 * That list has vanilla lanterns (and 1.21.2+ {@code minecraft:copper_lantern}
 * IDs) but not Caverns and Chasms / other mod lanterns, so those hang from a
 * lowered fist. Treat every hanging lantern block like the default lantern.
 */
@Mixin(targets = "dev.tr7zw.notenoughanimations.animations.hands.LookAtItemAnimation", remap = false)
public abstract class NeaLanternHoldUpMixin {

    @Redirect(
            method = "isValid",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"),
            remap = false
    )
    private boolean vansqmod$holdLanternsLikeVanilla(Set<?> holdingItems, Object candidate) {
        if (holdingItems.contains(candidate)) {
            return true;
        }
        return candidate instanceof Item item && vansqmod$isLantern(item);
    }

    @Unique
    private static boolean vansqmod$isLantern(Item item) {
        return item instanceof BlockItem blockItem
                && blockItem.getBlock().defaultBlockState().hasProperty(LanternBlock.HANGING);
    }
}
