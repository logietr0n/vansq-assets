package com.vansqmod.entity;

import com.vansqmod.VansqMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.neoforged.fml.ModList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Applies Vanilla Backport biome pig variants after a fresh-spawn force-add only.
 * Must not run on world/chunk load — that overwrites the pig's persisted variant.
 */
public final class VanillaBackportPigVariants {

    private static final String VB_MOD_ID = "vanillabackport";
    private static Boolean available;
    private static Method spawnContextCreate;
    private static Method selectVariantToSpawn;
    private static Method getHolder;
    private static Method setVariantData;
    private static Method getVariantData;
    private static Method trySetOffspringVariant;
    private static Object pigVariantsRegistry;
    private static Object farmAnimalsSpawner;
    private static boolean reflectFailed;

    private VanillaBackportPigVariants() {
    }

    /**
     * Assigns a parent trait to a breeding child. When both parents have variants,
     * Vanilla Backport picks either parent with equal probability.
     */
    public static void applyOffspringVariant(LivingEntity child, LivingEntity parentA, LivingEntity parentB) {
        if (!isAvailable() || child == null || parentA == null || parentB == null) {
            return;
        }
        if (child.level().isClientSide()) {
            return;
        }
        try {
            ensureReflect();
            if (reflectFailed) {
                return;
            }
            if (trySetOffspringVariant != null) {
                trySetOffspringVariant.invoke(null, child, parentA, parentB);
                return;
            }
            // Fallback: 50/50 copy of whichever parent still exposes variant data.
            Object holderA = getHolder.invoke(null, parentA);
            Object holderB = getHolder.invoke(null, parentB);
            if (holderA == null) {
                holderA = parentA;
            }
            if (holderB == null) {
                holderB = parentB;
            }
            @SuppressWarnings("unchecked")
            Optional<Object> varA = (Optional<Object>) getVariantData.invoke(holderA);
            @SuppressWarnings("unchecked")
            Optional<Object> varB = (Optional<Object>) getVariantData.invoke(holderB);
            Optional<Object> chosen;
            if (varA != null && varA.isPresent() && varB != null && varB.isPresent()) {
                chosen = child.getRandom().nextBoolean() ? varA : varB;
            } else if (varA != null && varA.isPresent()) {
                chosen = varA;
            } else if (varB != null && varB.isPresent()) {
                chosen = varB;
            } else {
                return;
            }
            Object childHolder = getHolder.invoke(null, child);
            if (childHolder == null) {
                childHolder = child;
            }
            setVariantData.invoke(childHolder, chosen.get());
        } catch (ReflectiveOperationException e) {
            VansqMod.LOGGER.warn("[PigLitters] failed to apply offspring pig variant", e);
            reflectFailed = true;
        }
    }

    public static void applyAfterForceAdd(ServerLevel level, Pig pig) {
        if (!isAvailable() || pig == null || level.isClientSide()) {
            return;
        }
        try {
            ensureReflect();
            if (reflectFailed) {
                return;
            }
            Object ctx = spawnContextCreate.invoke(null, level, pig.blockPosition());
            @SuppressWarnings("unchecked")
            Optional<Object> variant = (Optional<Object>) selectVariantToSpawn.invoke(
                    null, ctx, pigVariantsRegistry, farmAnimalsSpawner);
            if (variant == null || variant.isEmpty()) {
                VansqMod.LOGGER.debug(
                        "[PigForcePersist] VB variant select empty at {} (config off or no match)",
                        pig.blockPosition());
                return;
            }
            Object holder = getHolder.invoke(null, (LivingEntity) pig);
            if (holder == null) {
                // PigMixin implements VariantDataHolder directly.
                holder = pig;
            }
            setVariantData.invoke(holder, variant.get());
            VansqMod.LOGGER.info(
                    "[PigForcePersist] applied VB pig variant {} at {}",
                    variant.get(),
                    pig.blockPosition());
        } catch (ReflectiveOperationException e) {
            VansqMod.LOGGER.warn("[PigForcePersist] failed to apply VB pig variant", e);
            reflectFailed = true;
        }
    }

    private static boolean isAvailable() {
        if (available == null) {
            available = ModList.get().isLoaded(VB_MOD_ID);
        }
        return available;
    }

    private static void ensureReflect() throws ReflectiveOperationException {
        if (spawnContextCreate != null || reflectFailed) {
            return;
        }
        Class<?> spawnContext = Class.forName(
                "com.blackgear.vanillabackport.common.api.variant.spawn.SpawnContext");
        Class<?> variantUtils = Class.forName(
                "com.blackgear.vanillabackport.common.api.variant.VariantUtils");
        Class<?> variantDataHolder = Class.forName(
                "com.blackgear.vanillabackport.common.api.variant.VariantDataHolder");
        Class<?> registries = Class.forName(
                "com.blackgear.vanillabackport.core.registries.ModBuiltinRegistries");
        Class<?> variantSpawner = Class.forName(
                "com.blackgear.vanillabackport.common.api.variant.VariantSpawner");
        Class<?> builtInRegistry = Class.forName(
                "com.blackgear.platform.core.BuiltInCoreRegistry");

        spawnContextCreate = spawnContext.getMethod(
                "create",
                net.minecraft.world.level.ServerLevelAccessor.class,
                BlockPos.class);
        selectVariantToSpawn = variantUtils.getMethod(
                "selectVariantToSpawn",
                spawnContext,
                builtInRegistry,
                variantSpawner);
        getHolder = variantDataHolder.getMethod("getHolder", LivingEntity.class);
        setVariantData = variantDataHolder.getMethod("setVariantData", Object.class);
        getVariantData = variantDataHolder.getMethod("getVariantData");
        trySetOffspringVariant = variantDataHolder.getMethod(
                "trySetOffspringVariant",
                LivingEntity.class,
                LivingEntity.class,
                LivingEntity.class);

        Field pigVariants = registries.getField("PIG_VARIANTS");
        pigVariantsRegistry = pigVariants.get(null);
        Field farmAnimals = variantSpawner.getField("FARM_ANIMALS");
        farmAnimalsSpawner = farmAnimals.get(null);
    }
}
