package com.vansqmod.mixin.borninchaos;

import com.vansqmod.client.BabySkeletonEnchantedEyesLayer;
import com.vansqmod.client.GeoEnchantedEyesLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * EnchantWithMob only attaches EnchantedEyesLayer to LivingEntityRenderer.
 * Born in Chaos uses GeckoLib, so the overlay is added here. Datapack
 * enchantwithmob:mob_enchant_eye entries decide which mobs actually draw eyes.
 */
@Mixin(targets = {
        "net.mcreator.borninchaosv.client.renderer.BabySkeletonMinionRenderer",
        "net.mcreator.borninchaosv.client.renderer.BabySkeletonRenderer",
        "net.mcreator.borninchaosv.client.renderer.BabySpiderControlledRenderer",
        "net.mcreator.borninchaosv.client.renderer.BabySpiderRenderer",
        "net.mcreator.borninchaosv.client.renderer.BarrelZombieRenderer",
        "net.mcreator.borninchaosv.client.renderer.BloodyGadflyRenderer",
        "net.mcreator.borninchaosv.client.renderer.BoneImpMinionRenderer",
        "net.mcreator.borninchaosv.client.renderer.BoneImpRenderer",
        "net.mcreator.borninchaosv.client.renderer.BonescallerNotDespawnRenderer",
        "net.mcreator.borninchaosv.client.renderer.BonescallerRenderer",
        "net.mcreator.borninchaosv.client.renderer.ControlledBabySkeletonRenderer",
        "net.mcreator.borninchaosv.client.renderer.ControlledSpiritualAssistantRenderer",
        "net.mcreator.borninchaosv.client.renderer.CorpseFishRenderer",
        "net.mcreator.borninchaosv.client.renderer.CorpseFlyRenderer",
        "net.mcreator.borninchaosv.client.renderer.DarkVortexRenderer",
        "net.mcreator.borninchaosv.client.renderer.DecayingZombieNotDespawnRenderer",
        "net.mcreator.borninchaosv.client.renderer.DecayingZombieRenderer",
        "net.mcreator.borninchaosv.client.renderer.DecrepitSkeletonRenderer",
        "net.mcreator.borninchaosv.client.renderer.DiamondThermiteRenderer",
        "net.mcreator.borninchaosv.client.renderer.DireHoundLeaderRenderer",
        "net.mcreator.borninchaosv.client.renderer.DoorKnightNotDespawnRenderer",
        "net.mcreator.borninchaosv.client.renderer.DoorKnightRenderer",
        "net.mcreator.borninchaosv.client.renderer.DreadHoundNotDespawnRenderer",
        "net.mcreator.borninchaosv.client.renderer.DreadHoundRenderer",
        "net.mcreator.borninchaosv.client.renderer.FallenChaosKnightRenderer",
        "net.mcreator.borninchaosv.client.renderer.FelsteedRenderer",
        "net.mcreator.borninchaosv.client.renderer.FirelightNotDespawnRenderer",
        "net.mcreator.borninchaosv.client.renderer.FirelightRenderer",
        "net.mcreator.borninchaosv.client.renderer.GluttonFishRenderer",
        "net.mcreator.borninchaosv.client.renderer.InfernalSpiritRenderer",
        "net.mcreator.borninchaosv.client.renderer.KrampusHenchmanRenderer",
        "net.mcreator.borninchaosv.client.renderer.KrampusRenderer",
        "net.mcreator.borninchaosv.client.renderer.LifestealerRenderer",
        "net.mcreator.borninchaosv.client.renderer.LifestealerTrueFormRenderer",
        "net.mcreator.borninchaosv.client.renderer.LordPumpkinheadHeadRenderer",
        "net.mcreator.borninchaosv.client.renderer.LordPumpkinheadRenderer",
        "net.mcreator.borninchaosv.client.renderer.LordPumpkinheadWithoutaHorseRenderer",
        "net.mcreator.borninchaosv.client.renderer.LordTheHeadlessRenderer",
        "net.mcreator.borninchaosv.client.renderer.LordsFelsteedRenderer",
        "net.mcreator.borninchaosv.client.renderer.MaggotRenderer",
        "net.mcreator.borninchaosv.client.renderer.MissionaryRaiderRenderer",
        "net.mcreator.borninchaosv.client.renderer.MissionerRenderer",
        "net.mcreator.borninchaosv.client.renderer.MotherSpiderRenderer",
        "net.mcreator.borninchaosv.client.renderer.MrPumpkinControlledRenderer",
        "net.mcreator.borninchaosv.client.renderer.MrPumpkinRenderer",
        "net.mcreator.borninchaosv.client.renderer.MrsPumpkinRenderer",
        "net.mcreator.borninchaosv.client.renderer.NightmareStalkerRenderer",
        "net.mcreator.borninchaosv.client.renderer.PhantomCreeperCopyRenderer",
        "net.mcreator.borninchaosv.client.renderer.PhantomCreeperRenderer",
        "net.mcreator.borninchaosv.client.renderer.PumpkinBruiserRenderer",
        "net.mcreator.borninchaosv.client.renderer.PumpkinDunceRenderer",
        "net.mcreator.borninchaosv.client.renderer.PumpkinSpiritRenderer",
        "net.mcreator.borninchaosv.client.renderer.PumpkinheadRenderer",
        "net.mcreator.borninchaosv.client.renderer.RestlessSpiritRenderer",
        "net.mcreator.borninchaosv.client.renderer.RidingFelsteedRenderer",
        "net.mcreator.borninchaosv.client.renderer.RidingLordsFelsteedRenderer",
        "net.mcreator.borninchaosv.client.renderer.ScarletPersecutorRenderer",
        "net.mcreator.borninchaosv.client.renderer.SearedSpiritNotDespawnRenderer",
        "net.mcreator.borninchaosv.client.renderer.SearedSpiritRenderer",
        "net.mcreator.borninchaosv.client.renderer.SenorPumpkinRenderer",
        "net.mcreator.borninchaosv.client.renderer.SiameseSkeletonsRenderer",
        "net.mcreator.borninchaosv.client.renderer.SiameseSkeletonsleftRenderer",
        "net.mcreator.borninchaosv.client.renderer.SiameseSkeletonsrightRenderer",
        "net.mcreator.borninchaosv.client.renderer.SirPumpkinheadRenderer",
        "net.mcreator.borninchaosv.client.renderer.SirPumpkinheadWithoutHorseRenderer",
        "net.mcreator.borninchaosv.client.renderer.SirTheHeadlessRenderer",
        "net.mcreator.borninchaosv.client.renderer.SkeletonDemomanRenderer",
        "net.mcreator.borninchaosv.client.renderer.SkeletonThrasherNotDespawnRenderer",
        "net.mcreator.borninchaosv.client.renderer.SkeletonThrasherRenderer",
        "net.mcreator.borninchaosv.client.renderer.SpiritGuideAssistantRenderer",
        "net.mcreator.borninchaosv.client.renderer.SpiritGuideRenderer",
        "net.mcreator.borninchaosv.client.renderer.SpiritofChaosRenderer",
        "net.mcreator.borninchaosv.client.renderer.SupremeBonescallerNotDespawnRenderer",
        "net.mcreator.borninchaosv.client.renderer.SupremeBonescallerRenderer",
        "net.mcreator.borninchaosv.client.renderer.SupremeBonescallerStage2Renderer",
        "net.mcreator.borninchaosv.client.renderer.SwarmerRenderer",
        "net.mcreator.borninchaosv.client.renderer.ThornshellCrabRenderer",
        "net.mcreator.borninchaosv.client.renderer.ZombieBruiserRenderer",
        "net.mcreator.borninchaosv.client.renderer.ZombieClownNotDespawnRenderer",
        "net.mcreator.borninchaosv.client.renderer.ZombieClownRenderer",
        "net.mcreator.borninchaosv.client.renderer.ZombieFishermanRenderer",
        "net.mcreator.borninchaosv.client.renderer.ZombieLumberjackRenderer"
}, remap = false)
public abstract class BornInChaosEnchantedEyesMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void vansqmod$addEnchantedEyes(EntityRendererProvider.Context context, CallbackInfo ci) {
        GeoEntityRenderer renderer = (GeoEntityRenderer) (Object) this;
        if (this.getClass().getName().contains("BabySkeleton")) {
            renderer.addRenderLayer(new BabySkeletonEnchantedEyesLayer(renderer));
        } else {
            renderer.addRenderLayer(new GeoEnchantedEyesLayer(renderer));
        }
    }
}
