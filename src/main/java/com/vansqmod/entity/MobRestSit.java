package com.vansqmod.entity;

import com.vansqmod.mixin.AreaEffectCloudAccessor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

/**
 * Mob AI Tweaks rest seats are radius-0 {@link AreaEffectCloud}s the mob rides.
 * Vanilla passenger attachment uses the cloud's 0.5-block height, so sitters float.
 *
 * <p>The mob keeps its normal boat/minecart leg-fold pose while resting (same
 * {@code rotX = 1.4137167F} fold {@link net.minecraft.client.model.HumanoidModel}
 * uses for any passenger) - that pose is correct as-is and is not touched here.
 * The problem this attachment point fixes is purely positional: that fold rotates
 * the foot about the hip pivot, and at this rig's proportions (12-unit hip height,
 * 12-unit leg length) an ~81-degree fold only drops the foot {@code 12*cos(81deg)
 * =~1.9} units below the hip - leaving the whole seated body hovering roughly
 * {@code 12 - 1.9 =~10.1} raw units (~0.63 blocks, at this rig's 16-units-per-block
 * scale) above the ground it should be sitting on. Lowering the whole entity by
 * that same ~0.63 blocks (below) brings the seated foot back down to floor level
 * without touching the pose's rotation at all.
 */
public final class MobRestSit {

    /**
     * Desired rider Y relative to the rest cloud <em>after</em>
     * {@code Entity#positionRider} subtracts the passenger's vehicle attachment.
     * Vanilla zombies (and Putrid, which now matches them) use
     * {@code ridingOffset(-0.7F)}, which stores a +0.7 vehicle attachment;
     * returning {@link #SEAT_ATTACHMENT_Y} as the raw passenger attachment
     * therefore seats them almost a full block into the floor. Callers must add
     * the passenger's vehicle-attachment Y (see {@link #attachmentForPassenger})
     * so every adult humanoid sits at this height.
     *
     * <p>Tiny Takeover babies, Putrid, and Bouldering Zombie use unique true-size
     * meshes (and skip vanilla's baby shrink), so they need a separate, higher
     * seat or they sit in the floor.
     */
    public static final double SEAT_ATTACHMENT_Y = -0.23D;

    public static final double BABY_UNIQUE_MESH_SEAT_Y = 0.20D;

    private MobRestSit() {
    }

    /**
     * Rest seats are empty radius-0 clouds. Shrinking lingering/poison clouds also
     * hit that radius, so potion contents must be empty. Owner is not required:
     * {@link AreaEffectCloud#getOwner()} only resolves on the server.
     */
    public static boolean isRestSeatCloud(AreaEffectCloud cloud) {
        if (cloud.getRadius() > 0.05F) {
            return false;
        }
        return !hasPotionEffects(cloud);
    }

    private static boolean hasPotionEffects(AreaEffectCloud cloud) {
        PotionContents contents;
        try {
            contents = ((AreaEffectCloudAccessor) cloud).vansqmod$getPotionContents();
        } catch (Throwable ignored) {
            return true;
        }
        if (contents == null) {
            return false;
        }
        for (MobEffectInstance effect : contents.getAllEffects()) {
            if (effect != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRestSeat(Entity vehicle, Entity passenger) {
        if (!(vehicle instanceof AreaEffectCloud cloud) || !isRestSeatCloud(cloud)) {
            return false;
        }
        LivingEntity owner = cloud.getOwner();
        if (owner != null) {
            return owner == passenger;
        }
        // Owner UUID is only resolved on the server, so client rest-sit detection
        // falls back to "empty radius-0 cloud this mob is actually riding".
        return cloud.getPassengers().contains(passenger);
    }

    public static boolean isRestingOnSeat(LivingEntity entity) {
        Entity vehicle = entity.getVehicle();
        return vehicle != null && isRestSeat(vehicle, entity);
    }

    /**
     * Passenger attachment that lands the rider at the mesh-appropriate seat Y
     * relative to the cloud, regardless of that rider's {@code ridingOffset}.
     */
    public static Vec3 attachmentForPassenger(Entity vehicle, Entity passenger) {
        double seatY = SEAT_ATTACHMENT_Y;
        if (passenger instanceof LivingEntity living && living.isBaby()) {
            if (usesUniqueBabyMesh(living)) {
                seatY = BABY_UNIQUE_MESH_SEAT_Y;
            } else {
                seatY *= living.getAgeScale();
            }
        }
        return new Vec3(0.0D, seatY + passenger.getVehicleAttachmentPoint(vehicle).y, 0.0D);
    }

    /**
     * Tiny Takeover replaces baby zombie/husk/drowned/piglin meshes with true-size
     * models and disables vanilla's 0.5 age scale. Putrid and Bouldering Zombie
     * use the same true-size baby geos.
     */
    private static boolean usesUniqueBabyMesh(LivingEntity living) {
        if (living instanceof Putrid || living instanceof BoulderingZombie) {
            return true;
        }
        if (!(living instanceof Zombie) && !(living instanceof AbstractPiglin)) {
            return false;
        }
        return ModList.get().isLoaded("tiny_takeover_backport");
    }
}
