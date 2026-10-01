package com.vansqmod.mixin;

import com.vansqmod.compat.MonsterBoxRework;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public abstract class MonsterBoxNbtMixin {

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void vansqmod$saveMonsterBox(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (MonsterBoxRework.isMonsterBoxEntity(self)) {
            MonsterBoxRework.save(self, tag);
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void vansqmod$loadMonsterBox(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (MonsterBoxRework.isMonsterBoxEntity(self)) {
            MonsterBoxRework.load(self, tag);
        }
    }

    @Inject(method = "getUpdatePacket", at = @At("HEAD"), cancellable = true)
    private void vansqmod$syncMonsterBox(CallbackInfoReturnable<Packet<ClientGamePacketListener>> cir) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (MonsterBoxRework.isMonsterBoxEntity(self)) {
            cir.setReturnValue(ClientboundBlockEntityDataPacket.create(self, (be, provider) -> {
                CompoundTag tag = be.getUpdateTag(provider);
                MonsterBoxRework.save(be, tag);
                return tag;
            }));
        }
    }
}
