package com.vansqmod.boss;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Entity types that vansqmod attaches a boss health bar to. Expand this when adding
 * more bars or per-boss music so every bar shares one lookup.
 */
public final class BossBarRegistry {

    public static final ResourceLocation MISSIONER =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "missioner");
    public static final ResourceLocation LIFESTEALER_TRUE_FORM =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "lifestealer_true_form");
    public static final ResourceLocation BERSERKER =
            ResourceLocation.fromNamespaceAndPath("galosphere", "berserker");
    public static final ResourceLocation MUTANT_ENDERMAN =
            ResourceLocation.fromNamespaceAndPath("mutantmonsters", "mutant_enderman");

    private static final Map<ResourceLocation, BossBarStyle> STYLES = Map.ofEntries(
            Map.entry(MISSIONER, new BossBarStyle(0x9CB3BF, BossEvent.BossBarOverlay.PROGRESS)),
            Map.entry(LIFESTEALER_TRUE_FORM, new BossBarStyle(0xAD0000, BossEvent.BossBarOverlay.PROGRESS)),
            Map.entry(BERSERKER, new BossBarStyle(0xFF8178, BossEvent.BossBarOverlay.PROGRESS)),
            Map.entry(MUTANT_ENDERMAN, new BossBarStyle(0xDD9EFF, BossEvent.BossBarOverlay.PROGRESS))
    );

    private BossBarRegistry() {
    }

    public static @Nullable BossBarStyle get(Entity entity) {
        if (entity == null) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return id == null ? null : STYLES.get(id);
    }
}
