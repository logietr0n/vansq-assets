package com.vansqmod.mixin;

import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;
import java.util.UUID;

@Mixin(PersistentEntitySectionManager.class)
public interface PersistentEntitySectionManagerAccessor {

    @Accessor("knownUuids")
    Set<UUID> vansqmod$getKnownUuids();

    @Invoker("addNewEntityWithoutEvent")
    <T extends EntityAccess> boolean vansqmod$addNewEntityWithoutEvent(T entity);
}
