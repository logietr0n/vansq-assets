package com.vansqmod.mixin.leafculling;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import toni.sodiumleafculling.LeafCullingQuality;
import toni.sodiumleafculling.PerformanceSettingsAccessor;

@Mixin(
        targets = "net.caffeinemc.mods.sodium.client.gui.SodiumOptions$PerformanceSettings",
        remap = false
)
public abstract class SodiumLeafCullingPerformanceSettingsMixin implements PerformanceSettingsAccessor {

    @Unique
    private LeafCullingQuality vansqmod$leafCullingQuality = LeafCullingQuality.SOLID_AGGRESSIVE;

    @Override
    public LeafCullingQuality sodiumleafculling$getQuality() {
        return this.vansqmod$leafCullingQuality != null
                ? this.vansqmod$leafCullingQuality
                : LeafCullingQuality.SOLID_AGGRESSIVE;
    }

    @Override
    public void sodiumleafculling$setQuality(LeafCullingQuality quality) {
        this.vansqmod$leafCullingQuality = quality != null ? quality : LeafCullingQuality.SOLID_AGGRESSIVE;
    }
}
