package com.vansqmod.mixin.borninchaos;

import com.nyfaria.awcapi.entity.IAdvancedClimber;
import com.nyfaria.awcapi.entity.movement.ClimberPathNavigator;
import com.vansqmod.compat.NyfSpiderAi;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Baby Spider and Mother Spider extend vanilla {@link Spider}, so they should pick up Nyf's
 * climber navigator from {@code Spider.createNavigation}. Reinstall it when that did not happen,
 * and make sure the leap goal is Nyf's BetterLeap rather than the vanilla pounce.
 */
@Mixin(
        targets = {
                "net.mcreator.borninchaosv.entity.BabySpiderEntity",
                "net.mcreator.borninchaosv.entity.MotherSpiderEntity"
        },
        remap = false
)
public abstract class BornSpiderNyfMixin extends Spider {

    protected BornSpiderNyfMixin(EntityType<? extends Spider> type, Level level) {
        super(type, level);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void vansqmod$nyfSpiderAi(CallbackInfo ci) {
        if (!(this.navigation instanceof ClimberPathNavigator)) {
            var navigator = new ClimberPathNavigator<>((Spider & IAdvancedClimber) (Object) this, this.level(), false);
            navigator.setCanFloat(true);
            this.navigation = navigator;
        }
        NyfSpiderAi.applyFollowRangeBonus(this);
        NyfSpiderAi.ensureBetterLeap(this);
    }
}
