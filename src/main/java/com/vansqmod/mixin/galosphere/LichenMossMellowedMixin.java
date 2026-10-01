package com.vansqmod.mixin.galosphere;

import com.vansqmod.block.LichenMossLighting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.orcinus.galosphere.blocks.LichenMossBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = LichenMossBlock.class, remap = false)
public class LichenMossMellowedMixin {

    @Inject(method = "stepOn", at = @At("HEAD"), cancellable = true)
    private void vansqmod$mellowedDoesNotLight(
            Level level,
            BlockPos pos,
            BlockState state,
            Entity entity,
            CallbackInfo ci
    ) {
        if (!LichenMossLighting.canLightFrom(entity)) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
            )
    )
    private List<Entity> vansqmod$ignoreMellowedOccupants(ServerLevel level, Class<Entity> type, AABB box) {
        return level.getEntitiesOfClass(type, box, LichenMossLighting::keepsLit);
    }
}
