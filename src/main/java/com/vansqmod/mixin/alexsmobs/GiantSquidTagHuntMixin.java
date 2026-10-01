package com.vansqmod.mixin.alexsmobs;

import com.github.alexthe666.alexsmobs.entity.EntityGiantSquid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * {@code EntityGiantSquid$2} is the {@code #alexsmobs:giant_squid_targets} hunt.
 * Alex's Mobs only starts it when {@code !isInWaterOrBubble()}, so tag prey is
 * ignored in the ocean. Invert that check.
 */
@Mixin(targets = "com.github.alexthe666.alexsmobs.entity.EntityGiantSquid$2", remap = false)
public abstract class GiantSquidTagHuntMixin {

    @Redirect(
            method = "canUse",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/entity/EntityGiantSquid;isInWaterOrBubble()Z",
                    remap = false
            )
    )
    private boolean vansqmod$huntInWater(EntityGiantSquid squid) {
        return !squid.isInWaterOrBubble();
    }
}
