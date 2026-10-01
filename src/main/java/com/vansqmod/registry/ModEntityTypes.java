package com.vansqmod.registry;

import com.vansqmod.VansqMod;
import com.vansqmod.entity.BoulderingZombie;
import com.vansqmod.entity.IceSnowballProjectile;
import com.vansqmod.entity.IcicleProjectile;
import com.vansqmod.entity.Mellowed;
import com.vansqmod.entity.Putrid;
import com.vansqmod.entity.SoulFireballProjectile;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = VansqMod.MODID)
public final class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, VansqMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<Mellowed>> MELLOWED =
            ENTITY_TYPES.register("mellowed", () -> EntityType.Builder.of(Mellowed::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.99F)
                    .eyeHeight(1.74F)
                    .clientTrackingRange(8)
                    .build("mellowed"));

    public static final DeferredHolder<EntityType<?>, EntityType<Putrid>> PUTRID =
            ENTITY_TYPES.register("putrid", () -> EntityType.Builder.of(Putrid::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .eyeHeight(1.74F)
                    .passengerAttachments(2.0125F)
                    .ridingOffset(-0.7F)
                    .clientTrackingRange(8)
                    .build("putrid"));

    public static final DeferredHolder<EntityType<?>, EntityType<BoulderingZombie>> BOULDERING_ZOMBIE =
            ENTITY_TYPES.register("bouldering_zombie", () -> EntityType.Builder.of(BoulderingZombie::new, MobCategory.MONSTER)
                    .sized(0.6F, 2.0F)
                    .eyeHeight(1.78F)
                    .passengerAttachments(2.0625F)
                    .ridingOffset(-0.7F)
                    .clientTrackingRange(8)
                    .build("bouldering_zombie"));

    public static final DeferredHolder<EntityType<?>, EntityType<IcicleProjectile>> ICICLE =
            ENTITY_TYPES.register("icicle", () -> EntityType.Builder.<IcicleProjectile>of(IcicleProjectile::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("icicle"));

    public static final DeferredHolder<EntityType<?>, EntityType<IceSnowballProjectile>> ICE_SNOWBALL =
            ENTITY_TYPES.register("ice_snowball", () -> EntityType.Builder.<IceSnowballProjectile>of(IceSnowballProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("ice_snowball"));

    public static final DeferredHolder<EntityType<?>, EntityType<SoulFireballProjectile>> SOUL_FIRE_CHARGE =
            ENTITY_TYPES.register("soul_fire_charge", () -> EntityType.Builder.<SoulFireballProjectile>of(SoulFireballProjectile::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .fireImmune()
                    .build("soul_fire_charge"));

    private ModEntityTypes() {
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(MELLOWED.get(), Mellowed.createAttributes().build());
        event.put(PUTRID.get(), Putrid.createAttributes().build());
        event.put(BOULDERING_ZOMBIE.get(), BoulderingZombie.createAttributes().build());
    }

    @SubscribeEvent
    public static void modifyAttributes(EntityAttributeModificationEvent event) {
        event.add(PUTRID.get(), Attributes.SCALE, Putrid.MODEL_SCALE);
        event.add(BOULDERING_ZOMBIE.get(), Attributes.SCALE, BoulderingZombie.MODEL_SCALE);
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                MELLOWED.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mellowed::checkSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
                PUTRID.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
        event.register(
                BOULDERING_ZOMBIE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }
}
