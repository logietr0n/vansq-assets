package com.vansqmod.integration.curios;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;

/**
 * Equips a single copy of the held stack into a valid Curios slot, swapping with an occupant
 * when every matching slot is filled. Curios' own right-click path copies the entire stack.
 */
public final class CuriosHotbarEquip {

    private CuriosHotbarEquip() {
    }

    public static boolean tryEquipOne(Player player, InteractionHand hand) {
        if (player == null || player.isSpectator()) {
            return false;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            return false;
        }
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(player).orElse(null);
        if (handler == null) {
            return false;
        }

        SlotTarget empty = null;
        SlotTarget swap = null;
        for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
            ICurioStacksHandler stacksHandler = entry.getValue();
            IDynamicStackHandler stacks = stacksHandler.getStacks();
            NonNullList<Boolean> renderStates = stacksHandler.getRenders();
            int slotCount = stacks.getSlots();
            for (int i = 0; i < slotCount; i++) {
                boolean visible = renderStates.size() <= i || Boolean.TRUE.equals(renderStates.get(i));
                SlotContext ctx = new SlotContext(entry.getKey(), player, i, false, visible);
                if (!stacks.isItemValid(i, held)) {
                    continue;
                }
                ItemStack present = stacks.getStackInSlot(i);
                if (present.isEmpty()) {
                    if (empty == null) {
                        empty = new SlotTarget(entry.getKey(), i, ctx);
                    }
                    continue;
                }
                if (swap != null || ItemStack.isSameItemSameComponents(present, held)) {
                    continue;
                }
                if (!canUnequip(ctx, present)) {
                    continue;
                }
                swap = new SlotTarget(entry.getKey(), i, ctx);
            }
        }
        SlotTarget target = empty != null ? empty : swap;
        if (target == null) {
            return false;
        }
        ItemStack previous = empty != null
                ? ItemStack.EMPTY
                : handler.getCurios().get(target.id()).getStacks().getStackInSlot(target.index()).copy();
        ItemStack one = held.copyWithCount(1);
        handler.setEquippedCurio(target.id(), target.index(), one);
        playEquipFromUse(target.ctx(), one);
        giveBack(player, hand, held, previous);
        return true;
    }

    /**
     * Atlas and spyglass crash or open unwanted UIs on use; if they could not go into a slot,
     * still swallow the vanilla right-click.
     */
    public static boolean shouldSuppressVanillaUse(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(Items.SPYGLASS)) {
            return true;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return "map_atlases".equals(id.getNamespace()) && id.getPath().startsWith("atlas");
    }

    private static boolean canUnequip(SlotContext ctx, ItemStack present) {
        return CuriosApi.getCurio(present).map(curio -> curio.canUnequip(ctx)).orElse(true);
    }

    private static void playEquipFromUse(SlotContext ctx, ItemStack stack) {
        ICurio curio = CuriosApi.getCurio(stack).orElse(null);
        if (curio != null) {
            curio.onEquipFromUse(ctx);
            return;
        }
        LivingEntity entity = ctx.entity();
        entity.level().playSound(
                null,
                entity.blockPosition(),
                SoundEvents.ARMOR_EQUIP_GENERIC.value(),
                entity.getSoundSource(),
                1.0f,
                1.0f
        );
    }

    private static void giveBack(Player player, InteractionHand hand, ItemStack held, ItemStack previous) {
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        if (previous.isEmpty()) {
            return;
        }
        if (held.isEmpty()) {
            player.setItemInHand(hand, previous);
            return;
        }
        if (!player.getInventory().add(previous)) {
            player.drop(previous, false);
        }
    }

    private record SlotTarget(String id, int index, SlotContext ctx) {
    }
}
