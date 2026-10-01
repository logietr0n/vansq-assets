package com.vansqmod.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.monster.AbstractSkeleton;

/**
 * Synched baby fields for every {@link AbstractSkeleton}. IDs are owned by
 * AbstractSkeleton; they must still be {@link SynchedEntityData.Builder#define
 * defined} on every constructed subclass (see LivingEntitySkeletonBabyDataMixin).
 */
public final class SkeletonBabyData {

    public static final EntityDataAccessor<Boolean> BABY =
            SynchedEntityData.defineId(AbstractSkeleton.class, EntityDataSerializers.BOOLEAN);

    public static final EntityDataAccessor<String> TEXTURE =
            SynchedEntityData.defineId(AbstractSkeleton.class, EntityDataSerializers.STRING);

    private SkeletonBabyData() {
    }
}
