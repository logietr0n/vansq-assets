package com.vansqmod.mixin.barched;

import com.vansqmod.client.ZombieSpearArmFix;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieVillagerModel.class)
public abstract class ZombieVillagerModelSpearMixin<T extends Zombie> extends HumanoidModel<T> {

    private ZombieVillagerModelSpearMixin(ModelPart root) {
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
