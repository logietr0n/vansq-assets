package com.vansqmod.mixin.quark;

import com.vansqmod.compat.MonsterBoxRework;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "org.violetmoon.quark.content.world.block.be.MonsterBoxBlockEntity", remap = false)
public abstract class MonsterBoxReworkMixin {

    @Inject(
            method = "tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lorg/violetmoon/quark/content/world/block/be/MonsterBoxBlockEntity;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void vansqmod$scriptedBox(
            Level level,
            BlockPos pos,
            BlockState state,
            @Coerce BlockEntity box,
            CallbackInfo ci
    ) {
        MonsterBoxRework.tick(level, pos, state, box);
        ci.cancel();
    }
}
