package com.vansqmod.integration.toolbelt;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Sky Set on C&amp;C toolbelts checks {@link EquipmentSlot#LEGS}; this mirrors that logic for the Curios belt stack.
 */
public final class ToolbeltSkySetSupport {

    private static final ResourceKey<Enchantment> SKY_SET_KEY = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "sky_set"));

    private ToolbeltSkySetSupport() {
    }

    /**
     * @return true if Sky Set mid-air placement was handled (caller should cancel the original {@code use}).
     */
    public static boolean trySkySetUse(
            Item item,
            Level level,
            Player player,
            InteractionHand hand,
            CallbackResult callback
    ) {
        if (hasSkySet(player.getItemBySlot(EquipmentSlot.LEGS))) {
            return false;
        }

        ItemStack belt = ToolbeltEquipment.getEquippedToolbelt(player).orElse(ItemStack.EMPTY);
        if (!hasSkySet(belt)) {
            return false;
        }

        double reach = player.blockInteractionRange() - 2.0D;
        Vec3 eyeLoc = player.getEyePosition();
        Vec3 scaled = eyeLoc.add(player.calculateViewVector(player.getXRot(), player.getYRot())
                .scale(player.isSecondaryUseActive() ? reach / 2.0D : reach));
        BlockHitResult blockResult = level.clip(new ClipContext(
                eyeLoc, scaled, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));

        InteractionResult useOnResult = item.useOn(new UseOnContext(player, hand, blockResult));
        callback.apply(new InteractionResultHolder<>(useOnResult, player.getItemInHand(hand)));
        return true;
    }

    private static boolean hasSkySet(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        var enchants = stack.get(DataComponents.ENCHANTMENTS);
        if (enchants == null || enchants.isEmpty()) {
            return false;
        }
        for (Holder<Enchantment> enchant : enchants.keySet()) {
            if (enchant.is(SKY_SET_KEY) && enchants.getLevel(enchant) > 0) {
                return true;
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface CallbackResult {
        void apply(InteractionResultHolder<ItemStack> result);
    }
}
