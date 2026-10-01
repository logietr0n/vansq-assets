package com.vansqmod.mixin.barched;

import com.vansqmod.client.ZombieSpearArmFix;
import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Monster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * After Barched overwrites zombie arm poses (spear aim on the empty hand while
 * idle), put the SPEAR pose back on the hand that actually holds the spear.
 */
@Mixin(AbstractZombieModel.class)
public abstract class AbstractZombieModelSpearMixin<T extends Monster> extends HumanoidModel<T> {

    private AbstractZombieModelSpearMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void vansqmod$fixSpearHands(
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        ZombieSpearArmFix.fixSpearHands(this, entity, limbSwing, limbSwingAmount, ageInTicks);
    }
}
