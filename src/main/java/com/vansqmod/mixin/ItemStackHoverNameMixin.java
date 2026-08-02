package com.vansqmod.mixin;

import com.vansqmod.config.ItemNameColorConfig;
import com.vansqmod.config.ItemNameStyleOverride;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies {@code vansq_rarities.json} overrides to hover-name color and {@link ItemStack#getRarity()},
 * and neutralizes Dungeons Delight custom rarities (e.g. {@code MONSTER}) unless overridden.
 * <p>
 * Hover names are wrapped so ImmersiveUI (which reads sibling styles, not the root style) picks up
 * the same color as the item name.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackHoverNameMixin {

    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void vansq$applyHoverNameStyleOverrides(CallbackInfoReturnable<Component> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        ItemNameStyleOverride override = resolveNameOverride(stack);
        if (override == null) {
            return;
        }
        cir.setReturnValue(styledHoverName(stack, cir.getReturnValue(), override));
    }

    @Inject(method = "getRarity", at = @At("RETURN"), cancellable = true)
    private void vansq$applyRarityOverrides(CallbackInfoReturnable<Rarity> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.isEmpty()) {
            return;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) {
            return;
        }

        ItemNameStyleOverride configOverride = ItemNameColorConfig.overrideFor(id);
        if (configOverride != null) {
            if (configOverride.rarity() != null) {
                cir.setReturnValue(configOverride.rarity());
            }
            return;
        }

        if (!"dungeonsdelight".equals(id.getNamespace())) {
            return;
        }
        Rarity stored = componentRarity(stack);
        if (!isVanillaRarity(stored)) {
            cir.setReturnValue(Rarity.COMMON);
        }
    }

    private static @Nullable ItemNameStyleOverride resolveNameOverride(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) {
            return null;
        }

        ItemNameStyleOverride configOverride = ItemNameColorConfig.overrideFor(id);
        if (configOverride != null) {
            return configOverride;
        }

        if (!"dungeonsdelight".equals(id.getNamespace())) {
            return null;
        }
        // Read stored component rarity (not getRarity()) so DD custom tiers are still visible here.
        Rarity stored = componentRarity(stack);
        if (isVanillaRarity(stored)) {
            return null;
        }
        return ItemNameStyleOverride.fromRarity(Rarity.COMMON);
    }

    /** Rarity from item/stack components — bypasses this mixin's {@code getRarity} override. */
    private static Rarity componentRarity(ItemStack stack) {
        Rarity rarity = stack.get(DataComponents.RARITY);
        if (rarity != null) {
            return rarity;
        }
        return stack.getItem().components().getOrDefault(DataComponents.RARITY, Rarity.COMMON);
    }

    /**
     * ImmersiveUI samples {@code getHoverName().getSiblings()} colors (not the root style).
     * Wrapping the styled name as a sibling keeps tooltip text identical while feeding that API.
     */
    private static Component styledHoverName(ItemStack stack, Component fallback, ItemNameStyleOverride override) {
        Component base = stack.has(DataComponents.CUSTOM_NAME)
                ? stack.get(DataComponents.CUSTOM_NAME)
                : fallback;
        // If fallback was already wrapped by a prior inject, unwrap to avoid nested empties.
        Component styled = base.copy().withStyle(override.styleModifier());
        return Component.empty().append(styled);
    }

    private static boolean isVanillaRarity(Rarity rarity) {
        return rarity == Rarity.COMMON
                || rarity == Rarity.UNCOMMON
                || rarity == Rarity.RARE
                || rarity == Rarity.EPIC;
    }
}
