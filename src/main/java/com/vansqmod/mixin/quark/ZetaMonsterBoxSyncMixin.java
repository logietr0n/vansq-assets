package com.vansqmod.mixin.quark;

import com.vansqmod.compat.MonsterBoxRework;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "org.violetmoon.zeta.block.be.ZetaBlockEntity", remap = false)
public abstract class ZetaMonsterBoxSyncMixin {

    @Inject(method = "writeSharedNBT", at = @At("TAIL"))
    private void vansqmod$writeMonsterBox(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (MonsterBoxRework.isMonsterBoxEntity(self)) {
            MonsterBoxRework.save(self, tag);
        }
    }

    @Inject(method = "readSharedNBT", at = @At("TAIL"))
    private void vansqmod$readMonsterBox(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (MonsterBoxRework.isMonsterBoxEntity(self)) {
            MonsterBoxRework.load(self, tag);
        }
    }
}
