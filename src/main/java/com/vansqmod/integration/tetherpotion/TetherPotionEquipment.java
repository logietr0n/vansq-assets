package com.vansqmod.integration.tetherpotion;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves Caverns &amp; Chasms wearable potions from Curios {@code head}, not the vanilla helmet slot.
 */
public final class TetherPotionEquipment {

    public static final String HEAD_SLOT = "head";
    public static final String CNC_MODID = "caverns_and_chasms";
    public static final ResourceLocation TETHER_POTION_ITEM =
            ResourceLocation.fromNamespaceAndPath(CNC_MODID, "tether_potion");
    public static final ResourceLocation IMPACT_POTION_ITEM =
            ResourceLocation.fromNamespaceAndPath(CNC_MODID, "impact_potion");
    public static final ResourceLocation TRAIL_POTION_ITEM =
            ResourceLocation.fromNamespaceAndPath(CNC_MODID, "trail_potion");
    public static final List<ResourceLocation> POTION_ITEMS = List.of(
            TETHER_POTION_ITEM,
            IMPACT_POTION_ITEM,
            TRAIL_POTION_ITEM
    );
    private static final ResourceLocation TETHER_COOLDOWN =
            ResourceLocation.fromNamespaceAndPath(CNC_MODID, "tether_cooldown");
    private static final String TETHER_ITEM_CLASS =
            "com.teamabnormals.caverns_and_chasms.common.item.TetherPotionItem";
    private static final String SUBTLE_POTION_CLASS =
            "com.teamabnormals.caverns_and_chasms.common.item.SubtlePotion";

    private static Class<?> tetherPotionClass;
    private static Method updateEffectsMethod;
    private static Method isSubtleMethod;
    private static boolean cncResolved;
    private static Set<Item> cachedPotionItems;

    private TetherPotionEquipment() {
    }

    public static boolean isPotion(ItemStack stack) {
        return !stack.isEmpty() && isPotionItem(stack.getItem());
    }

    public static boolean isPotionItem(Item item) {
        if (item == null) {
            return false;
        }
        resolveCnc();
        if (tetherPotionClass != null && tetherPotionClass.isInstance(item)) {
            return true;
        }
        return potionItems().contains(item);
    }

    public static boolean isTetherPotion(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return TETHER_POTION_ITEM.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static boolean isSubtle(ItemStack stack) {
        resolveCnc();
        if (isSubtleMethod == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(isSubtleMethod.invoke(null, stack));
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    public static void updateTetherPotionEffects(LivingEntity entity, ItemStack stack, boolean applying) {
        if (entity == null || stack.isEmpty()) {
            return;
        }
        resolveCnc();
        if (updateEffectsMethod == null) {
            return;
        }
        try {
            updateEffectsMethod.invoke(null, entity, stack, applying);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    public static void setTetherCooldown(ItemStack stack, int ticks) {
        if (stack.isEmpty()) {
            return;
        }
        DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(TETHER_COOLDOWN);
        if (type == null) {
            return;
        }
        stack.set((DataComponentType<Integer>) type, ticks);
    }

    public static ItemStack resolveWornPotion(LivingEntity entity, EquipmentSlot slot) {
        ItemStack vanilla = entity.getItemBySlot(slot);
        if (slot != EquipmentSlot.HEAD) {
            return vanilla;
        }
        if (isPotion(vanilla)) {
            return vanilla;
        }
        return getCuriosPotion(entity).orElse(vanilla);
    }

    public static Optional<ItemStack> getCuriosPotion(LivingEntity entity) {
        return findPotionSlot(entity).map(SlotResult::stack);
    }

    public static Optional<SlotResult> findPotionSlot(LivingEntity entity) {
        if (entity == null || !ModList.get().isLoaded("curios")) {
            return Optional.empty();
        }
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return Optional.empty();
        }
        return handler.getStacksHandler(HEAD_SLOT).flatMap(headHandler -> {
            IDynamicStackHandler stacks = headHandler.getStacks();
            int slots = stacks.getSlots();
            for (int i = 0; i < slots; i++) {
                ItemStack stack = stacks.getStackInSlot(i);
                if (isPotion(stack)) {
                    return Optional.of(new SlotResult(
                            new SlotContext(HEAD_SLOT, entity, i, false, true),
                            stack
                    ));
                }
            }
            return Optional.empty();
        });
    }

    /**
     * Puts the potion into an empty Curios {@code head} slot. Does not overwrite another head curio.
     *
     * @return {@code true} if the potion is now in Curios head
     */
    public static boolean tryEquipPotion(LivingEntity entity, ItemStack stack) {
        if (entity == null || stack.isEmpty() || !isPotion(stack)) {
            return false;
        }
        if (getCuriosPotion(entity).isPresent()) {
            return false;
        }
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return false;
        }
        return handler.getStacksHandler(HEAD_SLOT).map(headHandler -> {
            IDynamicStackHandler stacks = headHandler.getStacks();
            int slots = stacks.getSlots();
            for (int i = 0; i < slots; i++) {
                if (stacks.getStackInSlot(i).isEmpty() && stacks.isItemValid(i, stack)) {
                    handler.setEquippedCurio(HEAD_SLOT, i, stack);
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }

    public static void setEquippedPotion(LivingEntity entity, ItemStack stack) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return;
        }

        Optional<SlotResult> existing = findPotionSlot(entity);
        if (existing.isPresent()) {
            SlotContext ctx = existing.get().slotContext();
            handler.setEquippedCurio(ctx.identifier(), ctx.index(), stack);
            return;
        }

        if (stack.isEmpty()) {
            return;
        }

        handler.getStacksHandler(HEAD_SLOT).ifPresent(headHandler -> {
            IDynamicStackHandler stacks = headHandler.getStacks();
            int slots = stacks.getSlots();
            for (int i = 0; i < slots; i++) {
                if (stacks.getStackInSlot(i).isEmpty() && stacks.isItemValid(i, stack)) {
                    handler.setEquippedCurio(HEAD_SLOT, i, stack);
                    return;
                }
            }
        });
    }

    private static Set<Item> potionItems() {
        if (cachedPotionItems == null || cachedPotionItems.isEmpty()) {
            Set<Item> items = new HashSet<>();
            for (ResourceLocation id : POTION_ITEMS) {
                Item item = BuiltInRegistries.ITEM.get(id);
                if (item != null && item != Items.AIR) {
                    items.add(item);
                }
            }
            cachedPotionItems = items;
        }
        return cachedPotionItems;
    }

    private static void resolveCnc() {
        if (cncResolved) {
            return;
        }
        cncResolved = true;
        if (!ModList.get().isLoaded(CNC_MODID)) {
            return;
        }
        try {
            tetherPotionClass = Class.forName(TETHER_ITEM_CLASS);
            updateEffectsMethod = tetherPotionClass.getMethod(
                    "updateTetherPotionEffects",
                    LivingEntity.class,
                    ItemStack.class,
                    boolean.class
            );
            Class<?> subtle = Class.forName(SUBTLE_POTION_CLASS);
            isSubtleMethod = subtle.getMethod("isSubtle", ItemStack.class);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
