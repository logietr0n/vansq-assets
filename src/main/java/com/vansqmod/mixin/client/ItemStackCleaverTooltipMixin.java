package com.vansqmod.mixin.client;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Locale;

/**
 * Dungeons Delight adds the cleaver throw-range line from a client-only mixin at {@link At#TAIL} on
 * {@link ItemStack#getTooltipLines}. NeoForge's {@code ItemTooltipEvent} often sees the tooltip list
 * before that tail injection runs, so stripping there misses it. This mixin runs at {@link At#RETURN},
 * after all tail injections, and removes that line.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackCleaverTooltipMixin {

    private static final String THROW_RANGE_KEY = "dungeonsdelight.tooltip.attribute.range";

    @Inject(method = "getTooltipLines", at = @At("RETURN"))
    private void vansq$stripDungeonsDelightThrowRange(
            Item.TooltipContext tooltipContext,
            Player player,
            TooltipFlag tooltipFlag,
            CallbackInfoReturnable<List<Component>> cir
    ) {
        List<Component> tip = cir.getReturnValue();
        if (tip == null || tip.size() <= 1) {
            return;
        }
        for (int i = tip.size() - 1; i >= 1; i--) {
            if (shouldStripThrowRangeLine(tip.get(i))) {
                tip.remove(i);
            }
        }
    }

    private static boolean shouldStripThrowRangeLine(Component line) {
        if (containsTranslatableKey(line, THROW_RANGE_KEY)) {
            return true;
        }
        String visual = line.getString().trim().toLowerCase(Locale.ROOT);
        return visual.contains("throw range") && visual.matches(".*\\d+\\.?\\d*x.*");
    }

    private static boolean containsTranslatableKey(Component line, String key) {
        ComponentContents contents = line.getContents();
        if (contents instanceof TranslatableContents tc && key.equals(tc.getKey())) {
            return true;
        }
        for (Component sibling : line.getSiblings()) {
            if (containsTranslatableKey(sibling, key)) {
                return true;
            }
        }
        return false;
    }
}
