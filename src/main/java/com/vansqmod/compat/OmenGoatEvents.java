package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = VansqMod.MODID)
public final class OmenGoatEvents {

    private OmenGoatEvents() {
    }

    @SubscribeEvent
    public static void onWake(PlayerWakeUpEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        boolean seamlessNight = SleepCompat.shouldTreatAsActualSleep(player, event);
        if (event.wakeImmediately() && !seamlessNight) {
            return;
        }
        if (player.getSleepTimer() < 99 && !seamlessNight) {
            return;
        }
        OmenGoats.trySpawnForWake(player);
    }

    @SubscribeEvent
    public static void onGoatTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Goat goat
                && !goat.level().isClientSide()
                && OmenGoats.isFollow(goat)) {
            OmenGoats.tick(goat);
        }
    }

    @SubscribeEvent
    public static void onGoatKilled(LivingDeathEvent event) {
        if (event.getEntity() instanceof Goat dead && event.getSource().getEntity() instanceof Player killer) {
            OmenGoats.onGoatKilled(dead, killer);
        }
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk()
                && event.getEntity() instanceof Goat goat
                && OmenGoats.isOmen(goat)) {
            event.setCanceled(true);
            goat.discard();
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ChunkAccess chunk = event.getChunk();
        ChunkPos pos = chunk.getPos();
        AABB box = new AABB(
                pos.getMinBlockX(),
                level.getMinBuildHeight(),
                pos.getMinBlockZ(),
                pos.getMaxBlockX() + 1,
                level.getMaxBuildHeight(),
                pos.getMaxBlockZ() + 1
        );
        for (Goat goat : level.getEntitiesOfClass(Goat.class, box)) {
            if (OmenGoats.isOmen(goat)) {
                OmenGoats.discardWithoutFx(goat);
            }
        }
    }
}
