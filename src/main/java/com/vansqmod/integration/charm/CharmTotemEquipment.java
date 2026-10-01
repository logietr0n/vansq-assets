package com.vansqmod.integration.charm;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Resolves Charm-slot stacks for totems that originally only checked hands.
 */
public final class CharmTotemEquipment {

    public static final String CHARM_SLOT = "charm";

    public static final TagKey<Item> CHARM_TAG = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("curios", "charm")
    );

    public static final ResourceLocation DEATH_TOTEM =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "death_totem");
    public static final ResourceLocation TOTEM_OF_RESPITE =
            ResourceLocation.fromNamespaceAndPath("the_beyond", "totem_of_respite");
    public static final ResourceLocation CHORUS_TOTEM =
            ResourceLocation.fromNamespaceAndPath("artifacts", "chorus_totem");

    /** Totems added to Charm that Friends &amp; Foes does not already render. */
    public static final ResourceLocation[] EXTRA_CHEST_TOTEMS = {
            DEATH_TOTEM,
            TOTEM_OF_RESPITE,
            CHORUS_TOTEM
    };

    private CharmTotemEquipment() {
    }

    public static boolean isDeathTotem(ItemStack stack) {
        return isItem(stack, DEATH_TOTEM);
    }

    public static boolean isRespite(ItemStack stack) {
        return isItem(stack, TOTEM_OF_RESPITE);
    }

    public static Optional<ItemStack> findCharmDeathTotem(LivingEntity entity) {
        return findCharm(entity, CharmTotemEquipment::isDeathTotem);
    }

    public static Optional<ItemStack> findCharmRespite(LivingEntity entity) {
        return findCharm(entity, CharmTotemEquipment::isRespite);
    }

    public static Optional<ItemStack> findCharm(LivingEntity entity, Predicate<ItemStack> predicate) {
        if (entity == null || !ModList.get().isLoaded("curios")) {
            return Optional.empty();
        }
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(inv -> inv.findFirstCurio(predicate))
                .filter(result -> CHARM_SLOT.equals(result.slotContext().identifier()))
                .map(SlotResult::stack);
    }

    private static boolean isItem(ItemStack stack, ResourceLocation id) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return BuiltInRegistries.ITEM.getOptional(id).map(stack::is).orElse(false);
    }
}
