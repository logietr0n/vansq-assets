package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Natural EnchantWithMob yes/no chance. Replaces the two toml percents.
 *
 * <p>Before anyone enters the Nether: Easy 0%, Normal 1%, Hard 2.5%.
 * After a player has entered the Nether (world-wide): Easy 1%, Normal 2%, Hard 5%.
 * Long-inhabited chunks can scale that up to 1.5× using vanilla effective difficulty.</p>
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class EnchantedSpawnRates {

    private static final ResourceLocation ENTER_NETHER =
            ResourceLocation.withDefaultNamespace("story/enter_the_nether");
    private static final float INHABIT_BONUS = 0.5F;

    private EnchantedSpawnRates() {
    }

    public static float chance(ServerLevel level, net.minecraft.core.BlockPos pos) {
        Difficulty difficulty = level.getDifficulty();
        float base = baseChance(difficulty, NetherEnchantUnlock.isUnlocked(level.getServer()));
        if (base <= 0.0F) {
            return 0.0F;
        }
        float effective = level.getCurrentDifficultyAt(pos).getEffectiveDifficulty();
        float floor = difficulty.getId() * 0.75F;
        float span = Math.max(maxEffective(difficulty) - floor, 0.001F);
        float inhabit = Mth.clamp((effective - floor) / span, 0.0F, 1.0F);
        return base * (1.0F + INHABIT_BONUS * inhabit);
    }

    private static float baseChance(Difficulty difficulty, boolean netherVisited) {
        return switch (difficulty) {
            case EASY -> netherVisited ? 0.01F : 0.0F;
            case NORMAL -> netherVisited ? 0.02F : 0.01F;
            case HARD -> netherVisited ? 0.05F : 0.025F;
            default -> 0.0F;
        };
    }

    private static float maxEffective(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 1.5F;
            case NORMAL -> 4.0F;
            case HARD -> 6.75F;
            default -> 0.0F;
        };
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (Level.NETHER.equals(event.getTo())) {
            unlockFrom(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && hasEnteredNetherAdvancement(player)) {
            NetherEnchantUnlock.unlock(player.server);
        }
    }

    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getLevel().dimension() == Level.NETHER) {
            NetherEnchantUnlock.unlock(player.server);
        }
    }

    private static void unlockFrom(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetherEnchantUnlock.unlock(serverPlayer.server);
        }
    }

    private static boolean hasEnteredNetherAdvancement(ServerPlayer player) {
        var holder = player.server.getAdvancements().get(ENTER_NETHER);
        return holder != null && player.getAdvancements().getOrStartProgress(holder).isDone();
    }
}
