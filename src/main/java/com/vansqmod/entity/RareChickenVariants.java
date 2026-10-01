package com.vansqmod.entity;

import com.blackgear.vanillabackport.common.api.variant.ModelAndTexture;
import com.blackgear.vanillabackport.common.api.variant.VariantDataHolder;
import com.blackgear.vanillabackport.common.api.variant.spawn.SpawnPrioritySelectors;
import com.blackgear.vanillabackport.common.level.entities.animal.ChickenVariant;
import com.blackgear.vanillabackport.common.level.entities.animal.ChickenVariants;
import com.blackgear.vanillabackport.common.registries.ModDataComponents;
import com.blackgear.vanillabackport.core.registries.ModRegistries;
import com.vansqmod.VansqMod;
import com.vansqmod.debug.VansqDebugState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;

/**
 * Breeding-only chicken variant, using the Blue Axolotl rate (1/1200). It never
 * matches a biome spawn condition. Two rare parents always produce a rare chick.
 */
public final class RareChickenVariants {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "rare");
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "entity/chicken/rare_chicken");
    public static final float BREED_CHANCE = 1.0F / 1200.0F;

    private static ResourceKey<ChickenVariant> key;
    private static boolean registered;

    private RareChickenVariants() {
    }

    public static void bootstrap() {
        if (registered || !ModList.get().isLoaded("vanillabackport")) {
            return;
        }
        ChickenVariant variant = new ChickenVariant(
                new ModelAndTexture<>(ChickenVariant.ModelType.NORMAL, TEXTURE),
                SpawnPrioritySelectors.EMPTY);
        ChickenVariants.REGISTRY.register(ID, variant);
        key = ResourceKey.create(ModRegistries.CHICKEN_VARIANT_KEY, ID);
        registered = true;
        VansqMod.LOGGER.info("Registered rare chicken variant {}", ID);
    }

    public static ResourceKey<ChickenVariant> key() {
        bootstrap();
        return key;
    }

    public static Item createEgg() {
        bootstrap();
        return new EggItem(new Item.Properties()
                .stacksTo(16)
                .component(ModDataComponents.CHICKEN_VARIANT.get(), key()));
    }

    public static boolean isRare(LivingEntity entity) {
        if (!registered || !(entity instanceof Chicken)) {
            return false;
        }
        VariantDataHolder<ChickenVariant> holder = VariantDataHolder.getHolder(entity);
        if (holder == null) {
            return false;
        }
        return holder.getVariantData()
                .filter(variant -> ID.equals(ChickenVariants.REGISTRY.getKey(variant)))
                .isPresent();
    }

    public static void apply(LivingEntity entity) {
        if (!registered) {
            return;
        }
        VariantDataHolder<ChickenVariant> holder = VariantDataHolder.getHolder(entity);
        ChickenVariant variant = ChickenVariants.REGISTRY.get(ID);
        if (holder == null || variant == null) {
            return;
        }
        holder.setVariantData(variant);
    }

    public static void applyBreeding(Chicken child, Chicken parentA, Chicken parentB) {
        if (!registered || child == null) {
            return;
        }
        boolean bothRare = isRare(parentA) && isRare(parentB);
        if (bothRare) {
            apply(child);
            return;
        }
        if (VansqDebugState.rareEventSucceeds(child.getRandom(), BREED_CHANCE)) {
            apply(child);
            return;
        }
        if (isRare(child)) {
            Chicken keep = isRare(parentA) ? parentB : parentA;
            VariantDataHolder<ChickenVariant> from = VariantDataHolder.getHolder(keep);
            VariantDataHolder<ChickenVariant> to = VariantDataHolder.getHolder(child);
            if (from != null && to != null) {
                from.getVariantData().ifPresent(to::setVariantData);
            }
        }
    }
}
