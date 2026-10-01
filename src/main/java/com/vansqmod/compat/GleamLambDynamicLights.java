package com.vansqmod.compat;

import com.vansqmod.integration.beltborne.BeltborneLanternEquipment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.thatmaidenjaden.gleam.client.lighting.GleamEmitterRegistry;
import net.thatmaidenjaden.gleam.client.lighting.GleamLight;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gleam only scans placed blocks during chunk rebuilds. LambDynamicLights already
 * raises vanilla block light for handheld and entity sources; this feeds those
 * same emitters into Gleam's GPU light list so the color overlay matches.
 */
public final class GleamLambDynamicLights {

    /** Match Gleam's default lightRenderDistance so entity lights are not dropped early. */
    private static final double MAX_DISTANCE_SQ = 192.0 * 192.0;
    /** Mint cyan from vansqresources glow-ink / glow-squid Gleam providers. */
    private static final float GLOW_INK_R = 0xA7 / 255.0f;
    private static final float GLOW_INK_G = 0xFF / 255.0f;
    private static final float GLOW_INK_B = 0xD5 / 255.0f;
    private static final ResourceLocation GLOW_INK_CLUMPS =
            ResourceLocation.fromNamespaceAndPath("galosphere", "glow_ink_clumps");
    /** Gleam jar fallbacks if the live block provider is missing. */
    private static final int GLEAM_FIRE = 0xFF4C00;
    private static final int GLEAM_GLOWSTONE = 0xF2FF82;
    private static final int GLEAM_SOUL_FIRE = 0x4CCFFF;
    private static final int GLEAM_TORCH = 0xFFA814;
    private static final float SPAWNER_GLEAM_R = 0xE8 / 255.0f;
    private static final float SPAWNER_GLEAM_G = 0xFF / 255.0f;
    private static final float SPAWNER_GLEAM_B = 0x65 / 255.0f;
    private static final float SPAWNER_GLEAM_RADIUS = 1.5f;
    private static final float SPAWNER_GLEAM_INTENSITY = 1.75f;

    private GleamLambDynamicLights() {
    }

    public static List<GleamLight> merge(List<GleamLight> lights) {
        try {
            return mergeUnsafe(lights);
        } catch (Throwable ignored) {
            return lights;
        }
    }

    private static List<GleamLight> mergeUnsafe(List<GleamLight> lights) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.gameRenderer == null) {
            return lights;
        }
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        float partialTick = minecraft.getTimer().getGameTimeDeltaPartialTick(false);
        Entity cameraEntity = minecraft.getCameraEntity();
        List<GleamLight> extra = new ArrayList<>();
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity == null || entity.isSpectator() || entity.isRemoved()) {
                continue;
            }
            int luminance = LambDynamicLightsCompat.getEntityLuminance(entity);
            if (luminance <= 0) {
                continue;
            }
            Vec3 pos = lightPosition(entity, partialTick);
            if (pos.distanceToSqr(camera) > MAX_DISTANCE_SQ) {
                continue;
            }
            GleamLight light = lightFor(entity, pos, entity == cameraEntity);
            if (light != null) {
                extra.add(light);
            }
        }
        List<GleamLight> merged = applyMonsterBoxLights(lights, minecraft.level, camera);
        if (extra.isEmpty()) {
            return merged;
        }
        List<GleamLight> withEntities = new ArrayList<>(merged.size() + extra.size());
        withEntities.addAll(merged);
        withEntities.addAll(extra);
        return withEntities;
    }

    private static List<GleamLight> applyMonsterBoxLights(List<GleamLight> lights, Level level, Vec3 camera) {
        Set<BlockPos> boxes = new HashSet<>();
        List<GleamLight> kept = new ArrayList<>(lights.size());
        for (GleamLight light : lights) {
            BlockPos pos = BlockPos.containing(light.x(), light.y(), light.z());
            if (MonsterBoxRework.isMonsterBox(level.getBlockState(pos))) {
                boxes.add(pos.immutable());
                continue;
            }
            kept.add(light);
        }
        MonsterBoxRework.forEachTracked(box -> {
            if (box.getLevel() == level) {
                boxes.add(box.getBlockPos().immutable());
            }
        });
        for (BlockPos pos : boxes) {
            if (pos.distToCenterSqr(camera) > MAX_DISTANCE_SQ) {
                continue;
            }
            BlockEntity box = level.getBlockEntity(pos);
            if (!MonsterBoxRework.isMonsterBoxEntity(box)) {
                continue;
            }
            GleamLight light = monsterBoxLight(pos, box);
            if (light != null) {
                kept.add(light);
            }
        }
        return kept;
    }

    private static GleamLight monsterBoxLight(BlockPos pos, BlockEntity box) {
        int stage = MonsterBoxRework.stageOf(box);
        if (stage == MonsterBoxRework.STAGE_SPENT) {
            return null;
        }
        float intensity = stage == MonsterBoxRework.STAGE_IDLE
                ? SPAWNER_GLEAM_INTENSITY * 3.0f / 15.0f
                : SPAWNER_GLEAM_INTENSITY;
        return GleamLight.create(
                pos.getX() + 0.5f,
                pos.getY() + 0.5f,
                pos.getZ() + 0.5f,
                SPAWNER_GLEAM_R,
                SPAWNER_GLEAM_G,
                SPAWNER_GLEAM_B,
                SPAWNER_GLEAM_RADIUS,
                intensity,
                false,
                false
        );
    }

    private static GleamLight lightFor(Entity entity, Vec3 pos, boolean handheld) {
        GleamLight template = templateFor(entity);
        if (template == null) {
            return null;
        }
        return GleamLight.create(
                (float) pos.x,
                (float) pos.y,
                (float) pos.z,
                template.r(),
                template.g(),
                template.b(),
                template.radius(),
                handheld ? Math.max(template.intensity(), 1.0f) : template.intensity(),
                template.blacklight(),
                template.occludeToBlocklight()
        );
    }

    private static Vec3 lightPosition(Entity entity, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (entity == minecraft.getCameraEntity()
                && minecraft.options.getCameraType().isFirstPerson()
                && minecraft.gameRenderer != null) {
            Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
            return new Vec3(camera.x, camera.y - 0.25, camera.z);
        }
        if (!(entity instanceof LivingEntity)) {
            return entity.getLightProbePosition(partialTick);
        }
        double x = Mth.lerp(partialTick, entity.xo, entity.getX());
        double y = Mth.lerp(partialTick, entity.yo, entity.getY()) + entity.getEyeHeight() * 0.55;
        double z = Mth.lerp(partialTick, entity.zo, entity.getZ());
        return new Vec3(x, y, z);
    }

    private static GleamLight templateFor(Entity entity) {
        GleamLight best = null;
        if (entity instanceof ItemEntity itemEntity) {
            return usable(templateFromStack(itemEntity.getItem()));
        }
        if (entity instanceof FallingBlockEntity falling) {
            best = brighter(best, templateFromState(falling.getBlockState()));
        }
        if (entity instanceof LivingEntity living) {
            boolean hideOffhandLight = living instanceof Player player && DoubucklerHands.holdingInMain(player);
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (hideOffhandLight && slot == EquipmentSlot.OFFHAND) {
                    continue;
                }
                best = brighter(best, templateFromStack(living.getItemBySlot(slot)));
            }
            if (ModList.get().isLoaded("beltborne_lanterns")) {
                try {
                    best = brighter(best, BeltborneLanternEquipment.getBeltLamp(living)
                            .map(GleamLambDynamicLights::templateFromStack)
                            .orElse(null));
                } catch (Throwable ignored) {
                }
            }
        }
        if (entity.isOnFire() || isFireLitEntity(entity)) {
            best = brighter(best, fireLight());
        }
        if (best == null) {
            best = templateFromEntity(entity);
        }
        return usable(best);
    }

    private static boolean isFireLitEntity(Entity entity) {
        EntityType<?> type = entity.getType();
        return type == EntityType.BLAZE || type == EntityType.MAGMA_CUBE;
    }

    private static GleamLight templateFromEntity(Entity entity) {
        String path = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        if (path.contains("soul")) {
            return soulFireLight();
        }
        if (path.contains("glow")) {
            return glowInkLight();
        }
        return null;
    }

    private static GleamLight templateFromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        if (stack.is(Items.GLOWSTONE_DUST)) {
            return glowstoneLight();
        }
        if (stack.is(Items.BLAZE_ROD) || stack.is(Items.BLAZE_POWDER)) {
            return fireLight();
        }
        if (stack.is(Items.GLOW_INK_SAC)) {
            return glowInkLight();
        }
        if (stack.getItem() instanceof BlockItem blockItem) {
            GleamLight fromBlock = templateFromState(emittingState(blockItem.getBlock()));
            if (fromBlock != null) {
                return fromBlock;
            }
        }
        return templateFromName(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath());
    }

    private static GleamLight templateFromState(BlockState state) {
        if (state == null || state.isAir()) {
            return null;
        }
        return usable(GleamEmitterRegistry.createLight(emittingState(state.getBlock(), state), 0, 0, 0));
    }

    private static GleamLight usable(GleamLight light) {
        if (light == null) {
            return null;
        }
        if (light.radius() <= 0.0f || light.intensity() <= 0.0f) {
            return null;
        }
        if (light.r() <= 0.0f && light.g() <= 0.0f && light.b() <= 0.0f) {
            return null;
        }
        return light;
    }

    private static BlockState emittingState(Block block) {
        return emittingState(block, block.defaultBlockState());
    }

    private static BlockState emittingState(Block block, BlockState state) {
        if (state.hasProperty(BlockStateProperties.LIT) && !state.getValue(BlockStateProperties.LIT)) {
            return state.setValue(BlockStateProperties.LIT, true);
        }
        if (block == Blocks.REDSTONE_LAMP) {
            return Blocks.REDSTONE_LAMP.defaultBlockState().setValue(BlockStateProperties.LIT, true);
        }
        return state;
    }

    private static GleamLight templateFromName(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        if (path.contains("soul_torch") || path.contains("soul_lantern") || path.contains("soul_campfire")
                || path.contains("soul_fire")) {
            return soulFireLight();
        }
        if (path.contains("glow_ink")) {
            return glowInkLight();
        }
        if (path.contains("glowstone")) {
            return glowstoneLight();
        }
        if (path.contains("blaze_rod") || path.contains("blaze_powder")) {
            return fireLight();
        }
        if (path.contains("torch") || path.contains("lantern") || path.contains("campfire")) {
            return torchLight();
        }
        return null;
    }

    private static GleamLight fireLight() {
        return templateOrFallback(Blocks.FIRE.defaultBlockState(), GLEAM_FIRE, 4.5f, 1.0f);
    }

    private static GleamLight glowstoneLight() {
        return templateOrFallback(Blocks.GLOWSTONE.defaultBlockState(), GLEAM_GLOWSTONE, 4.0f, 1.0f);
    }

    private static GleamLight soulFireLight() {
        return templateOrFallback(Blocks.SOUL_FIRE.defaultBlockState(), GLEAM_SOUL_FIRE, 8.0f, 1.0f);
    }

    private static GleamLight torchLight() {
        return templateOrFallback(Blocks.TORCH.defaultBlockState(), GLEAM_TORCH, 3.5f, 0.8f);
    }

    private static GleamLight glowInkLight() {
        Block clumps = BuiltInRegistries.BLOCK.getOptional(GLOW_INK_CLUMPS).orElse(Blocks.AIR);
        if (clumps != Blocks.AIR) {
            GleamLight fromClumps = templateFromState(clumps.defaultBlockState());
            if (fromClumps != null) {
                return fromClumps;
            }
        }
        return recolor(glowstoneLight(), GLOW_INK_R, GLOW_INK_G, GLOW_INK_B);
    }

    private static GleamLight templateOrFallback(BlockState state, int rgb, float radius, float intensity) {
        GleamLight light = templateFromState(state);
        if (light != null) {
            return light;
        }
        return GleamLight.create(
                0.0f,
                0.0f,
                0.0f,
                ((rgb >> 16) & 0xFF) / 255.0f,
                ((rgb >> 8) & 0xFF) / 255.0f,
                (rgb & 0xFF) / 255.0f,
                radius,
                intensity,
                false,
                false
        );
    }

    private static GleamLight recolor(GleamLight template, float r, float g, float b) {
        if (template == null) {
            return GleamLight.create(0.0f, 0.0f, 0.0f, r, g, b, 4.0f, 1.0f, false, false);
        }
        return GleamLight.create(
                0.0f,
                0.0f,
                0.0f,
                r,
                g,
                b,
                template.radius(),
                template.intensity(),
                template.blacklight(),
                template.occludeToBlocklight()
        );
    }

    private static GleamLight brighter(GleamLight current, GleamLight candidate) {
        if (candidate == null) {
            return current;
        }
        if (current == null) {
            return candidate;
        }
        return luma(candidate) > luma(current) ? candidate : current;
    }

    private static float luma(GleamLight light) {
        return 0.2126f * light.r() + 0.7152f * light.g() + 0.0722f * light.b();
    }
}
