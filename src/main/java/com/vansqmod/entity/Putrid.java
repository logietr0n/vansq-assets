package com.vansqmod.entity;

import com.vansqmod.debug.VansqDebugState;
import com.vansqmod.mixin.AreaEffectCloudAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Swamp zombie variant. Thicket sounds and drowned texture; vents a poison lingering cloud
 * for {@link #CLOUD_DURATION_TICKS}. Walking {@link #CLOUD_RELOCATE_DISTANCE} from that
 * cloud's center discards it and starts a fresh one, so Putrids do not leave a trail.
 * Immune to Poison (does not receive the undead invert that heals other undead).
 *
 * <p>Rendered with GeckoLib ({@code assets/vansqmod/geo/entity/putrid.geo.json},
 * BlockBench-sculpted geometry only) instead of vanilla's {@code ZombieModel}, but
 * behaves like a plain zombie: all bone motion (walk swing, arm reach pose, idle
 * bob, head-look) is computed procedurally in {@link PutridModel}, porting vanilla's
 * own {@code HumanoidModel}/{@code AbstractZombieModel} animation code instead of
 * using BlockBench-authored clips.</p>
 *
 * <p>Shearable, same as vanilla's {@code Bogged}: the {@code body_shroom}/
 * {@code head_shroom} geo bones (hidden client-side in {@link PutridModel} once
 * {@link #isSheared()}) drop as Dappled Up shelf mushrooms instead of staying on
 * the model. {@code body_outer}/{@code head_outer}/{@code right_arm_outer}/
 * {@code left_arm_outer} are a separate, permanent moss layer, not mushrooms, and
 * stay visible regardless of shearing.</p>
 *
 * <p>Spawn-equipment mixins ({@code MobSpawnArmorMixin} on {@code Mob},
 * {@code ZombieSpawnWeaponMixin} on {@code Zombie}) weave into the vanilla bytecode
 * Putrid inherits {@code populateDefaultEquipmentSlots} from, so armor tiers and the
 * spawn-weapon roll already apply here with no extra wiring. Curios is a different
 * story: Curios only grants slot access to entity types explicitly listed in a
 * {@code curios/entities/*.json} data file (bytecode inheritance from {@code Zombie}
 * doesn't carry over, since Curios matches by registered entity id, not Java class),
 * so {@code data/vansqmod/curios/entities/putrid.json} grants Putrid the same slot
 * set vanilla zombies get from Artifacts' own compat data. Every Putrid spawns with
 * an Artifacts {@code charm_of_sinking} equipped in the "necklace" slot (see
 * {@link #equipCharmOfSinking()}) -- invisibly, since GeckoLib's {@code GeoEntityRenderer}
 * isn't a {@code RenderLayerParent<EntityModel>} and Curios' curio-render hook can't
 * attach to it at all, unlike vanilla armor (visible via {@code PutridArmorLayer}).
 * That equipped copy is destroyed on death so Curios cannot always-drop it; the only
 * charm that can appear is the entity loot table's player-kill roll.</p>
 *
     * <p>3% spawn chance of a Caverns and Chasms oxidized copper lantern in the
     * offhand (20% of those rolls are weathered instead — see
     * {@link #populateDefaultEquipmentSlots}), rendered in-hand via
     * {@code PutridItemLayer}. Its 50%-plus-Looting drop chance is entirely vanilla's own
     * per-slot equipment drop mechanic ({@link #setDropChance}) -- the Looting boost on
     * top of that base 50% comes from the Looting enchantment's own data-driven
     * {@code EquipmentDrops} effect, not from any custom chance/looting math here.
     * Charm of Sinking drops from the entity loot table (0.75% + 0.25% per Looting)
     * when killed by a player, not from the curio slot itself.</p>
 */
public class Putrid extends Zombie implements GeoEntity, Shearable {

    private static final EntityDataAccessor<Boolean> DATA_SHEARED =
            SynchedEntityData.defineId(Putrid.class, EntityDataSerializers.BOOLEAN);
    private static final ResourceLocation SHELF_MUSHROOM_ID =
            ResourceLocation.fromNamespaceAndPath("dappled_up", "shelf_mushroom");
    private static final int SHEARED_MUSHROOM_COUNT = 2;
    private static final ResourceLocation CHARM_OF_SINKING_ID =
            ResourceLocation.fromNamespaceAndPath("artifacts", "charm_of_sinking");
    private static final String CURIO_NECKLACE_SLOT = "necklace";
    private static final String COPPER_LANTERN_NAMESPACE = "caverns_and_chasms";
    private static final ResourceLocation OXIDIZED_COPPER_LANTERN_ID =
            ResourceLocation.fromNamespaceAndPath(COPPER_LANTERN_NAMESPACE, "oxidized_copper_lantern");
    private static final ResourceLocation WEATHERED_COPPER_LANTERN_ID =
            ResourceLocation.fromNamespaceAndPath(COPPER_LANTERN_NAMESPACE, "weathered_copper_lantern");
    private static final float COPPER_LANTERN_CHANCE = 0.03F;
    /** Among Putrids that spawn with a lantern, chance the lantern is weathered. */
    private static final float WEATHERED_LANTERN_CHANCE = 0.20F;
    /**
     * Base per-slot equipment drop chance (vanilla {@link #getEquipmentDropChance}/
     * {@link #dropCustomDeathLoot} mechanic, inherited unmodified) -- the Looting
     * boost on top of this is entirely vanilla's own data-driven enchantment effect
     * ({@code EnchantmentEffectComponents.EQUIPMENT_DROPS}, defined in the Looting
     * enchantment's own data file), so no custom drop-chance/looting math is written
     * here at all.
     */
    private static final float COPPER_LANTERN_DROP_CHANCE = 0.5F;

    /** 11 seconds. */
    private static final int POISON_DURATION_TICKS = 220;
    /** 10 seconds. */
    private static final int CLOUD_DURATION_TICKS = 200;
    /** Lingering potion radius plus one block. */
    private static final float CLOUD_RADIUS = 4.0F;
    /** Baby Putrids are half-scale; their cloud matches that. */
    private static final float BABY_CLOUD_RADIUS_SCALE = 0.5F;
    /**
     * If this Putrid walks this far (horizontal) from the center of the cloud it
     * created, that cloud is discarded and a new 10-second cloud is spawned here.
     */
    private static final double CLOUD_RELOCATE_DISTANCE = 2.0D;
    private static final double CLOUD_RELOCATE_DISTANCE_SQR =
            CLOUD_RELOCATE_DISTANCE * CLOUD_RELOCATE_DISTANCE;
    /** Do not spawn another cloud while this close horizontally to an existing poison cloud. */
    private static final double CLOUD_NEAR_CENTER = 3.0D;
    /** Same as a Husk: type size 0.6×1.95 with no extra {@link Attributes#SCALE}. */
    public static final double MODEL_SCALE = 1.0D;
    /** Vanilla zombie {@link Attributes#JUMP_STRENGTH} default. */
    public static final double JUMP_STRENGTH = 0.42D;
    /**
     * Chance to spawn holding a fishing rod when the mainhand is otherwise empty,
     * used like Mob AI Tweaks fishermen. Independent of the zombie tool roll so it
     * never replaces a spawned weapon.
     */
    private static final float FISHING_ROD_CHANCE = 0.36F;

    private static final ResourceLocation THICKET_AMBIENT =
            ResourceLocation.fromNamespaceAndPath("variantsandventures", "entity.thicket.ambient");
    private static final ResourceLocation THICKET_HURT =
            ResourceLocation.fromNamespaceAndPath("variantsandventures", "entity.thicket.hurt");
    private static final ResourceLocation THICKET_DEATH =
            ResourceLocation.fromNamespaceAndPath("variantsandventures", "entity.thicket.death");
    private static final ResourceLocation THICKET_STEP =
            ResourceLocation.fromNamespaceAndPath("variantsandventures", "entity.thicket.step");
    private static final ResourceLocation THICKET_ATTACK =
            ResourceLocation.fromNamespaceAndPath("variantsandventures", "entity.thicket.attack");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    /** Own last cloud, so a just-spawned cloud is seen before the world entity list updates. */
    private AreaEffectCloud lastCloud;
    /** Prevents a new cloud every tick if the near-check flickers (water bob, slopes). */
    private int cloudSpawnCooldown;
    /**
     * Charm of Sinking should skip water/lava travel physics, but vanilla also
     * gates {@code jumpFromGround()} on {@link #isAffectedByFluids()}. Set only
     * around {@link #travel} so land (and seafloor) jumps still fire.
     */
    private boolean ignoreFluidsForTravel;
    private boolean charmOfSinkingCached;
    private int charmOfSinkingCacheTick = Integer.MIN_VALUE;

    public Putrid(EntityType<? extends Putrid> type, Level level) {
        super(type, level);
        applyModelScale();
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new PutridNavigation(this, level);
    }

    private void applyModelScale() {
        AttributeInstance scale = this.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.setBaseValue(MODEL_SCALE);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.175D)
                .add(Attributes.ARMOR, 5.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 3.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.JUMP_STRENGTH, JUMP_STRENGTH);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new PutridFishingRodGoal(this));
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.getAvailableGoals().removeIf(
                wrapped -> wrapped.getGoal() instanceof WaterAvoidingRandomStrollGoal);
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0D));
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);
        boolean forceEquip = VansqDebugState.isForceEquipmentEnabled();
        if (forceEquip || (this.getMainHandItem().isEmpty() && random.nextFloat() < FISHING_ROD_CHANCE)) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.FISHING_ROD));
        }
        if (forceEquip || random.nextFloat() < COPPER_LANTERN_CHANCE) {
            ResourceLocation lanternId = random.nextFloat() < WEATHERED_LANTERN_CHANCE
                    ? WEATHERED_COPPER_LANTERN_ID
                    : OXIDIZED_COPPER_LANTERN_ID;
            BuiltInRegistries.ITEM.getOptional(lanternId)
                    .or(() -> BuiltInRegistries.ITEM.getOptional(OXIDIZED_COPPER_LANTERN_ID))
                    .ifPresent(lantern -> {
                        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(lantern));
                        // The 50%-plus-Looting drop roll itself is entirely vanilla's own
                        // per-slot equipment drop chance + Looting's data-driven equipment-drop
                        // enchantment effect -- setting this is the only line needed for it.
                        this.setDropChance(EquipmentSlot.OFFHAND, COPPER_LANTERN_DROP_CHANCE);
                    });
        }
    }

    /** Used by {@link PutridModel} to raise whichever arm bone renders as the offhand. */
    public boolean isHoldingCopperLantern() {
        ItemStack offhand = this.getOffhandItem();
        if (offhand.isEmpty()) {
            return false;
        }
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(offhand.getItem());
        return COPPER_LANTERN_NAMESPACE.equals(key.getNamespace())
                && key.getPath().endsWith("copper_lantern");
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnType,
            @Nullable SpawnGroupData spawnGroupData
    ) {
        // Runs Zombie's own finalizeSpawn chain first (armor tiers, spawn weapon,
        // etc. via the existing spawn-equipment mixins) before the Curios equip.
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        equipCharmOfSinking();
        return spawnGroupData;
    }

    /**
     * Soft dependency on Artifacts/Curios, same pattern as {@link #spawnShearedMushrooms()}:
     * no-ops if the item isn't registered (Artifacts absent) or if this entity has no
     * "necklace" curio slot (Curios absent, or {@code data/vansqmod/curios/entities/putrid.json}
     * missing). Never renders -- see the class Javadoc.
     */
    private void equipCharmOfSinking() {
        BuiltInRegistries.ITEM.getOptional(CHARM_OF_SINKING_ID).ifPresent(charm ->
                CuriosApi.getCuriosInventory(this).ifPresent(inventory ->
                        inventory.setEquippedCurio(CURIO_NECKLACE_SLOT, 0, new ItemStack(charm))));
    }

    /**
     * Curios treats {@code DEFAULT} drop rules as always-drop for mobs, and tosses
     * those item entities farther than loot-table {@code spawnAtLocation}. The necklace
     * charm is only equipped for {@link #isAffectedByFluids()}; strip it before death
     * loot so the datapack table is the only drop path.
     */
    private void destroyEquippedCharmOfSinking() {
        BuiltInRegistries.ITEM.getOptional(CHARM_OF_SINKING_ID).ifPresent(charm ->
                CuriosApi.getCuriosInventory(this).ifPresent(inventory -> {
                    if (inventory.isEquipped(charm)) {
                        inventory.setEquippedCurio(CURIO_NECKLACE_SLOT, 0, ItemStack.EMPTY);
                    }
                }));
    }

    /**
     * Whether this Putrid currently has its (normally-equipped, see
     * {@link #equipCharmOfSinking()}) charm_of_sinking curio present -- used by
     * {@link #isAffectedByFluids()} below. Cached for the current tick because
     * travel/jump/fluid checks can hit this several times per tick.
     */
    private boolean isHoldingCharmOfSinking() {
        int tick = this.tickCount;
        if (tick == this.charmOfSinkingCacheTick) {
            return this.charmOfSinkingCached;
        }
        this.charmOfSinkingCacheTick = tick;
        this.charmOfSinkingCached = BuiltInRegistries.ITEM.getOptional(CHARM_OF_SINKING_ID)
                .map(charm -> CuriosApi.getCuriosInventory(this)
                        .map(inventory -> inventory.isEquipped(charm))
                        .orElse(false))
                .orElse(false);
        return this.charmOfSinkingCached;
    }

    /**
     * Ports charm_of_sinking's "moves through water like on land" effect directly,
     * rather than relying on Artifacts' own implementation of it: that turned out
     * (see {@code artifacts.neoforge.mixin.ability.sinking.PlayerMixin}, decompiled
     * during investigation) to be limited to a single {@code Player}-only dig-speed
     * tweak, plus the actual water-push/oxygen mechanics gated behind a separate
     * "expandability" library mod that isn't part of this pack -- meaning the
     * "ignore water" movement behavior described in Artifacts' own charm_of_sinking
     * config ("removes the wearer's collision with water") doesn't currently fire
     * for anyone, players included, in this install.
     *
     * <p>{@code LivingEntity#travel} uses {@link #isAffectedByFluids()} to pick the
     * water/lava movement branches, but {@code LivingEntity#aiStep} also refuses to
     * call {@code jumpFromGround()} unless this returns true. Returning false always
     * (the first sinking port) therefore left Putrids unable to clear 1-block gaps
     * on land, unlike Mellowed. Fluid physics are skipped only during
     * {@link #travel}; current push is separately disabled via {@link #isPushedByFluid()}.
     * Pathfinding ignores water the same way: {@link PutridNavigation} accepts water
     * nodes, and {@link PathType#WATER}/{@link PathType#WATER_BORDER} malus is 0.
     *
     * <p>Vanilla {@code LivingEntity#aiStep} still treats standing in water deeper than
     * {@link #getFluidJumpThreshold()} as a swim hop ({@code jumpInLiquid}, +0.3) instead
     * of {@code jumpFromGround()} (0.42). That hop cannot clear a 1-block shore, so
     * Putrids would jump in place at the water's edge. With the charm, the fluid-jump
     * threshold is ignored and swim hops are skipped so seafloor jumps match land.
     */
    @Override
    public boolean isAffectedByFluids() {
        return !ignoreFluidsForTravel && super.isAffectedByFluids();
    }

    @Override
    public boolean isPushedByFluid() {
        return !isHoldingCharmOfSinking() && super.isPushedByFluid();
    }

    @Override
    public void travel(Vec3 travelVector) {
        ignoreFluidsForTravel = isHoldingCharmOfSinking();
        try {
            super.travel(travelVector);
        } finally {
            ignoreFluidsForTravel = false;
        }
    }

    @Override
    public double getFluidJumpThreshold() {
        return isHoldingCharmOfSinking() ? Double.POSITIVE_INFINITY : super.getFluidJumpThreshold();
    }

    @Override
    protected void jumpInLiquid(TagKey<Fluid> fluid) {
        if (isHoldingCharmOfSinking()) {
            this.jumpFromGround();
            return;
        }
        super.jumpInLiquid(fluid);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide || !this.isAlive() || this.isNoAi()) {
            return;
        }
        if (this.cloudSpawnCooldown > 0) {
            this.cloudSpawnCooldown--;
        }
        if (hasActiveOwnCloud()) {
            if (horizontalDistanceSqr(this.lastCloud) > CLOUD_RELOCATE_DISTANCE_SQR) {
                discardOwnCloud();
                spawnPoisonCloud(true);
            }
            return;
        }
        if (this.cloudSpawnCooldown > 0) {
            return;
        }
        if (!isNearPoisonCloud()) {
            spawnPoisonCloud(false);
        }
    }

    private boolean isNearPoisonCloud() {
        AABB search = this.getBoundingBox().inflate(CLOUD_NEAR_CENTER, 16.0D, CLOUD_NEAR_CENTER);
        for (AreaEffectCloud cloud : this.level().getEntitiesOfClass(
                AreaEffectCloud.class, search, Putrid::isPoisonCloud)) {
            if (horizontalDistanceSqr(cloud) <= CLOUD_NEAR_CENTER * CLOUD_NEAR_CENTER) {
                return true;
            }
        }
        return false;
    }

    private boolean hasActiveOwnCloud() {
        return this.lastCloud != null
                && this.lastCloud.isAlive()
                && !this.lastCloud.isRemoved()
                && isPoisonCloud(this.lastCloud);
    }

    private void discardOwnCloud() {
        if (this.lastCloud != null && !this.lastCloud.isRemoved()) {
            this.lastCloud.discard();
        }
        this.lastCloud = null;
    }

    private double horizontalDistanceSqr(Entity other) {
        double dx = this.getX() - other.getX();
        double dz = this.getZ() - other.getZ();
        return dx * dx + dz * dz;
    }

    private static boolean isPoisonCloud(AreaEffectCloud cloud) {
        if (cloud.getRadius() <= 0.05F) {
            return false;
        }
        PotionContents contents;
        try {
            contents = ((AreaEffectCloudAccessor) cloud).vansqmod$getPotionContents();
        } catch (Throwable ignored) {
            return false;
        }
        if (contents == null) {
            return false;
        }
        for (MobEffectInstance effect : contents.getAllEffects()) {
            if (effect != null && effect.getEffect().is(MobEffects.POISON)) {
                return true;
            }
        }
        return false;
    }

    private void spawnPoisonCloud() {
        spawnPoisonCloud(false);
    }

    private void spawnPoisonCloud(boolean force) {
        if (!force && isNearPoisonCloud()) {
            return;
        }
        float radius = poisonCloudRadius();
        AreaEffectCloud cloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
        cloud.setOwner(this);
        cloud.setRadius(radius);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setDuration(CLOUD_DURATION_TICKS);
        cloud.setRadiusPerTick(-radius / (float) CLOUD_DURATION_TICKS);
        cloud.setPotionContents(new PotionContents(
                Optional.of(Potions.WATER),
                Optional.of(MobEffects.POISON.value().getColor()),
                List.of(new MobEffectInstance(MobEffects.POISON, POISON_DURATION_TICKS, 0))
        ));
        this.lastCloud = cloud;
        this.cloudSpawnCooldown = 20;
        this.level().addFreshEntity(cloud);
    }

    private float poisonCloudRadius() {
        return this.isBaby() ? CLOUD_RADIUS * BABY_CLOUD_RADIUS_SCALE : CLOUD_RADIUS;
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide && !this.isRemoved()) {
            destroyEquippedCharmOfSinking();
            discardOwnCloud();
            spawnPoisonCloud(true);
        }
        super.die(source);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (effect.getEffect().is(MobEffects.POISON)) {
            return false;
        }
        return super.canBeAffected(effect);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypes.DROWN) || super.isInvulnerableTo(source);
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        SoundEvent attack = thicketSound(THICKET_ATTACK, null);
        if (attack != null) {
            this.playSound(attack, this.getSoundVolume(), this.getVoicePitch());
        }
        return super.doHurtTarget(target);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return thicketSound(THICKET_AMBIENT, SoundEvents.ZOMBIE_AMBIENT);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return thicketSound(THICKET_HURT, SoundEvents.ZOMBIE_HURT);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return thicketSound(THICKET_DEATH, SoundEvents.ZOMBIE_DEATH);
    }

    @Override
    protected SoundEvent getStepSound() {
        return thicketSound(THICKET_STEP, SoundEvents.ZOMBIE_STEP);
    }

    private static SoundEvent thicketSound(ResourceLocation id, SoundEvent fallback) {
        return BuiltInRegistries.SOUND_EVENT.getOptional(id).orElse(fallback);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SHEARED, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("sheared", this.isSheared());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        applyModelScale();
        this.setSheared(tag.getBoolean("sheared"));
    }

    public boolean isSheared() {
        return this.entityData.get(DATA_SHEARED);
    }

    public void setSheared(boolean sheared) {
        this.entityData.set(DATA_SHEARED, sheared);
    }

    @Override
    public boolean readyForShearing() {
        return !this.isSheared() && this.isAlive();
    }

    /**
     * Same shape as vanilla's {@code Bogged#shear}: play the snip, drop the
     * vegetation as items, and flag the model to hide the outer growth bones.
     */
    @Override
    public void shear(SoundSource soundSource) {
        this.level().playSound(null, this, SoundEvents.MOOSHROOM_SHEAR, soundSource, 1.0F, 1.0F);
        this.spawnShearedMushrooms();
        this.setSheared(true);
    }

    private void spawnShearedMushrooms() {
        Item shelfMushroom = BuiltInRegistries.ITEM.getOptional(SHELF_MUSHROOM_ID).orElse(Items.RED_MUSHROOM);
        for (int i = 0; i < SHEARED_MUSHROOM_COUNT; i++) {
            this.spawnAtLocation(new ItemStack(shelfMushroom), this.getBbHeight());
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (itemInHand.is(Items.SHEARS) && this.readyForShearing()) {
            this.shear(SoundSource.PLAYERS);
            this.gameEvent(GameEvent.SHEAR, player);
            if (!this.level().isClientSide) {
                itemInHand.hurtAndBreak(1, player, getSlotForHand(hand));
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    /**
     * No clips to register -- {@link PutridModel#setCustomAnimations} computes every
     * bone rotation procedurally each frame, same as vanilla's {@code ZombieModel}.
     */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Intentionally empty.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
