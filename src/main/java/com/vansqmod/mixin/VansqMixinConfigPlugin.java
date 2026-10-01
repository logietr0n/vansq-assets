package com.vansqmod.mixin;

import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;

/**
 * Conditionally applies integration mixins based on loaded mods.
 * Replaces {@code @IfModLoaded} from mixinconstraints to avoid conflicts with
 * backpacks' bundled mixinconstraints plugin on dedicated servers.
 */
public class VansqMixinConfigPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        registerMixinCancellers();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith(".FarmersDelightKnifeStatsMixin")
                || mixinClassName.endsWith(".FarmersDelightKnifeDurabilityMixin")
                || mixinClassName.endsWith(".FarmersDelightTweedAddItemMixin")
                || mixinClassName.endsWith(".FarmersDelightTweedCuttingMixin")) {
            return isModLoaded("farmersdelight");
        }
        if (mixinClassName.endsWith(".KineticWeaponMixin")) {
            // KineticWeapon is 1.21.2+; the missing target shows up as a mixin error
            // on Drippy's loading overlay in this 1.21.1 pack.
            return false;
        }
        if (mixinClassName.contains(".backpacks.")) {
            if (mixinClassName.endsWith(".BackpackSpyglassMixin")) {
                return isModLoaded("backpacks") && isModLoaded("curios") && isModLoaded("spyglass_improvements");
            }
            if (mixinClassName.endsWith(".MinecraftItemUseCooldownInvoker")) {
                return true;
            }
            return isModLoaded("backpacks") && isModLoaded("curios");
        }
        if (mixinClassName.contains(".toolbelt.")) {
            return isModLoaded("caverns_and_chasms") && isModLoaded("curios");
        }
        if (mixinClassName.contains(".caverns.") && mixinClassName.contains("TetherPotion")) {
            return isModLoaded("caverns_and_chasms") && isModLoaded("curios");
        }
        if (mixinClassName.contains(".caverns.")) {
            return isModLoaded("caverns_and_chasms");
        }
        if (mixinClassName.contains(".galosphere.")) {
            return isModLoaded("galosphere");
        }
        if (mixinClassName.contains(".nea.")) {
            return isModLoaded("notenoughanimations");
        }
        if (mixinClassName.contains(".barched.")) {
            return isModLoaded("barched");
        }
        if (mixinClassName.contains(".mait.")) {
            return isModLoaded("mob-ai-tweaks");
        }
        if (mixinClassName.contains(".beltborne.")) {
            return isModLoaded("beltborne_lanterns") && isModLoaded("curios");
        }
        if (mixinClassName.contains(".astronomy.")) {
            return isModLoaded("spyglass_astronomy");
        }
        if (mixinClassName.contains(".spyglass.")) {
            return isModLoaded("spyglass_astronomy") && isModLoaded("spyglass_improvements");
        }
        if (mixinClassName.contains(".combatnouveau.")) {
            if (mixinClassName.endsWith(".CombatNouveauBucklerDelayMixin")) {
                return isModLoaded("combatnouveau") && isModLoaded("piglinproliferation");
            }
            return isModLoaded("combatnouveau");
        }
        if (mixinClassName.endsWith(".BucklerItemUseMixin")
                || mixinClassName.endsWith(".BucklerDashMomentumMixin")
                || mixinClassName.endsWith(".DoubucklerOffhandSlotMixin")
                || mixinClassName.endsWith(".DoubucklerSwapOffhandMixin")) {
            return isModLoaded("piglinproliferation");
        }
        if (mixinClassName.contains(".mapatlases.")) {
            return isModLoaded("map_atlases");
        }
        if (mixinClassName.contains(".moonlight.")) {
            if (mixinClassName.endsWith(".MoonlightMapAtlasCompatMixin")) {
                return isModLoaded("moonlight") && isModLoaded("map_atlases");
            }
            return isModLoaded("moonlight");
        }
        if (mixinClassName.contains(".particlerain.")) {
            return isModLoaded("particlerain");
        }
        if (mixinClassName.contains(".lootr.")) {
            return isModLoaded("lootr");
        }
        if (mixinClassName.contains(".copperage.")) {
            return isModLoaded("copperagebackport");
        }
        if (mixinClassName.contains(".incontrol.")) {
            return isModLoaded("incontrol");
        }
        if (mixinClassName.endsWith(".MonsterBoxLivingFlameMixin")
                || mixinClassName.endsWith(".MonsterBoxSoundTypeMixin")) {
            return isModLoaded("quark") && isModLoaded("dungeonsdelight");
        }
        if (mixinClassName.endsWith(".MonsterBoxReworkMixin")
                || mixinClassName.endsWith(".MonsterBoxHardnessMixin")
                || mixinClassName.endsWith(".ZetaMonsterBoxSyncMixin")) {
            return isModLoaded("quark");
        }
        if (mixinClassName.contains(".quark.") || mixinClassName.endsWith(".QuarkAttributeTooltipsMixin")) {
            return isModLoaded("quark");
        }
        if (mixinClassName.endsWith(".CuriosAttributeSlotHeaderMixin")) {
            return isModLoaded("curios");
        }
        if (mixinClassName.contains(".yungscavebiomes.")) {
            if (mixinClassName.endsWith(".ZombieFrostedCavesGelidMixin")
                    || mixinClassName.endsWith(".ZombieGelidConversionShakeMixin")) {
                return isModLoaded("yungscavebiomes") && isModLoaded("variantsandventures");
            }
            return isModLoaded("yungscavebiomes");
        }
        if (mixinClassName.contains(".distanthorizons.")) {
            return isModLoaded("distanthorizons");
        }
        if (mixinClassName.contains(".emf.")) {
            return isModLoaded("entity_model_features") && isModLoaded("supplementaries");
        }
        if (mixinClassName.contains(".hominid.")) {
            return isModLoaded("hominid");
        }
        if (mixinClassName.contains(".goatman.")) {
            return isModLoaded("goat_man");
        }
        if (mixinClassName.contains(".seamlesssleep.")) {
            return isModLoaded("seamlesssleep");
        }
        if (mixinClassName.contains(".sleeptight.")) {
            return isModLoaded("sleep_tight");
        }
        if (mixinClassName.contains(".sleepquality.")) {
            return isModLoaded("sleepquality");
        }
        if (mixinClassName.contains(".reactivemusic.")) {
            return isModLoaded("reactivemusic");
        }
        if (mixinClassName.contains(".peaceless.")) {
            return isModLoaded("peaceless");
        }
        if (mixinClassName.contains(".variantsandventures.")) {
            return isModLoaded("variantsandventures");
        }
        if (mixinClassName.contains(".tinytakeover.")) {
            return isModLoaded("tiny_takeover_backport") && isModLoaded("variantsandventures");
        }
        if (mixinClassName.endsWith(".SubtleEffectsEndRemasteredCompatMixin")) {
            return isModLoaded("subtle_effects") && isModLoaded("endrem");
        }
        if (mixinClassName.contains(".subtleeffects.")) {
            return isModLoaded("subtle_effects");
        }
        if (mixinClassName.contains(".gleam.")) {
            if (mixinClassName.contains("SodiumPatcher")) {
                return isModLoaded("gleam") && isModLoaded("sodium");
            }
            return isModLoaded("gleam");
        }
        if (mixinClassName.contains(".leafculling.")) {
            return isModLoaded("sodium") && isModLoaded("sodiumleafculling");
        }
        if (mixinClassName.contains(".sodium.")) {
            return isModLoaded("sodium") && isModLoaded("lambdynlights");
        }
        if (mixinClassName.contains(".soundphysics.")) {
            return isModLoaded("sound_physics_remastered");
        }
        if (mixinClassName.contains(".essential.")) {
            return isModLoaded("essential");
        }
        if (mixinClassName.contains(".fancymenu.")) {
            return isModLoaded("fancymenu") && isModLoaded("essential");
        }
        if (mixinClassName.contains(".heartstone.")) {
            return isModLoaded("heartstone");
        }
        if (mixinClassName.contains(".netherexp.")) {
            return isModLoaded("netherexp");
        }
        if (mixinClassName.contains(".refraction.")) {
            return isModLoaded("refraction");
        }
        if (mixinClassName.contains(".artifacts.")) {
            return isModLoaded("artifacts");
        }
        if (mixinClassName.contains(".incubation.")) {
            return isModLoaded("incubation")
                    && isModLoaded("vanillabackport")
                    && isModLoaded("vbincubationcompat");
        }
        if (mixinClassName.contains(".vanillabackport.")) {
            if (mixinClassName.endsWith(".FireflyBushSubtleEffectsMixin")) {
                return isModLoaded("vanillabackport") && isModLoaded("subtle_effects");
            }
            if (mixinClassName.endsWith(".RareChickenBabyRendererMixin")) {
                return isModLoaded("vanillabackport") && isModLoaded("tiny_takeover_backport");
            }
            return isModLoaded("vanillabackport");
        }
        if (mixinClassName.contains(".artistry.")) {
            return isModLoaded("artistry");
        }
        if (mixinClassName.contains(".dappledup.")) {
            return isModLoaded("dappled_up");
        }
        if (mixinClassName.contains(".separatedleaves.")) {
            return isModLoaded("separatedleaves");
        }
        if (mixinClassName.contains(".spideroverhaul.")) {
            if (mixinClassName.endsWith(".ClimberHelperOceanPounceMixin")) {
                return isModLoaded("spider_overhaul") && isModLoaded("nyfsspiders");
            }
            return isModLoaded("spider_overhaul");
        }
        if (mixinClassName.contains(".creeperoverhaul.")) {
            if (mixinClassName.endsWith(".CreeperOverhaulEnchantedEyesMixin")) {
                return isModLoaded("creeperoverhaul") && isModLoaded("enchantwithmob");
            }
            return isModLoaded("creeperoverhaul");
        }
        if (mixinClassName.contains(".shieldexp.")) {
            return isModLoaded("shieldexp") && isModLoaded("piglinproliferation");
        }
        if (mixinClassName.contains(".enchantwithmob.")) {
            return isModLoaded("enchantwithmob");
        }
        if (mixinClassName.contains(".multiplayerbosses.")) {
            return isModLoaded("multiplayerbosses");
        }
        if (mixinClassName.contains(".jade.")) {
            return isModLoaded("jade");
        }
        if (mixinClassName.contains(".alexsmobs.")) {
            return isModLoaded("alexsmobs");
        }
        if (mixinClassName.contains(".geckolib.")) {
            if (mixinClassName.endsWith(".GeoClimberRenderMixin")) {
                return isModLoaded("geckolib") && isModLoaded("nyfsspiders");
            }
            return isModLoaded("geckolib");
        }
        if (mixinClassName.contains(".borninchaos.")) {
            if (mixinClassName.endsWith(".BornInChaosEnchantedEyesMixin")) {
                return isModLoaded("born_in_chaos_v1") && isModLoaded("enchantwithmob");
            }
            if (mixinClassName.endsWith(".MissionaryHatpProcedureMixin")
                    || mixinClassName.endsWith(".DeathTotemCharmSlotMixin")) {
                return isModLoaded("born_in_chaos_v1") && isModLoaded("curios");
            }
            if (mixinClassName.endsWith(".BabySpiderControlledNyfMixin")
                    || mixinClassName.endsWith(".BornSpiderNyfMixin")) {
                return isModLoaded("born_in_chaos_v1") && isModLoaded("nyfsspiders");
            }
            return isModLoaded("born_in_chaos_v1");
        }
        if (mixinClassName.contains(".thebeyond.")) {
            return isModLoaded("the_beyond") && isModLoaded("curios");
        }
        if (mixinClassName.contains(".friendsandfoes.")) {
            return isModLoaded("friendsandfoes") && isModLoaded("curios");
        }
        if (mixinClassName.endsWith(".XaeroHudRendererDisableMixin")
                || mixinClassName.endsWith(".XaeroMinimapControlsDisableMixin")) {
            return isModLoaded("xaerominimap");
        }
        if (mixinClassName.endsWith(".XaeroWorldMapControlsDisableMixin")) {
            return isModLoaded("xaeroworldmap");
        }
        return true;
    }

    private static boolean isModLoaded(String modId) {
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (mixinClassName.endsWith("EmfModelPartCustomTextureSizeMixin")
                || mixinClassName.contains(".leafculling.")) {
            registerMixinCancellers();
        }
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (isSeparatedLeavesPatchMixin(mixinClassName, targetClassName)) {
            patchSeparatedLeavesHandler(targetClass);
        }
    }

    /**
     * Sodium Leaf Culling 1.0.1 still targets Sodium 0.6 {@code SodiumGameOptions} /
     * {@code SodiumGameOptionPages} and the old options-backed renderer hooks. Cancel
     * those mixins; vansq retargets the leaf-cull pipeline at Sodium 0.8.
     * <p>
     * Do not cancel Supplementaries {@code CompatEMFMixin} (3.9.9 already injects EMF
     * 3.3's 5-arg constructor) or their {@code AbstractBlockRenderContext} accessor
     * (valid on Sodium 0.8). MixinSquared logs a WARN on every cancel, and Drippy
     * pauses the loading overlay on that warning.
     */
    private static final Set<String> CANCELLED_MIXINS = Set.of(
            "toni.sodiumleafculling.mixins.BlockOcclusionCacheMixin",
            "toni.sodiumleafculling.mixins.BlockRendererMixin",
            "toni.sodiumleafculling.mixins.PerformanceSettingsMixin",
            "toni.sodiumleafculling.mixins.SodiumGameOptionPagesMixin"
    );
    private static boolean mixinCancellerRegistered;

    private static void registerMixinCancellers() {
        if (mixinCancellerRegistered) {
            return;
        }
        try {
            Class<?> api = Class.forName("com.bawnorton.mixinsquared.api.MixinCanceller");
            Class<?> registrar = Class.forName("com.bawnorton.mixinsquared.canceller.MixinCancellerRegistrar");
            Object canceller = Proxy.newProxyInstance(api.getClassLoader(), new Class<?>[] { api }, (proxy, method, args) -> {
                if (method.getDeclaringClass() == Object.class) {
                    return switch (method.getName()) {
                        case "equals" -> proxy == args[0];
                        case "hashCode" -> System.identityHashCode(proxy);
                        default -> "vansqmod mixin canceller";
                    };
                }
                if (args == null) {
                    return false;
                }
                for (Object arg : args) {
                    String name = String.valueOf(arg);
                    if (CANCELLED_MIXINS.contains(name)) {
                        return true;
                    }
                }
                return false;
            });
            registrar.getMethod("register", api).invoke(null, canceller);
            mixinCancellerRegistered = true;
        } catch (Throwable ignored) {
            // MixinSquared is jar-in-jar on Supplementaries; retry from preApply.
        }
    }

    private static boolean isSeparatedLeavesPatchMixin(String mixinClassName, String targetClassName) {
        return mixinClassName.endsWith("separatedleaves.SeparatedLeavesUpdateDistanceMixin")
                && "net.minecraft.world.level.block.LeavesBlock".equals(targetClassName);
    }

    /**
     * Separated Leaves injects at HEAD of {@code LeavesBlock.updateDistance} and always
     * structure-scans + biome-looks-up. Mixin cannot target that mixin class, so after
     * their handler is merged we return immediately for leaf types with no pairing rule.
     * <p>
     * The skip call must stay on {@code LeavesBlock} itself. A raw invoke of
     * {@code com.vansqmod.compat.SeparatedLeavesPerf} from Minecraft bytecode fails
     * JPMS ({@code ClassNotFoundException} while ticking leaves).
     */
    private static final String SKIP_OWNER = "net/minecraft/world/level/block/LeavesBlock";
    private static final String SKIP_DESC = "(Lnet/minecraft/world/level/block/state/BlockState;)Z";

    private static void patchSeparatedLeavesHandler(ClassNode targetClass) {
        MethodNode skip = findSkipMethod(targetClass);
        if (skip == null) {
            return;
        }
        for (MethodNode method : targetClass.methods) {
            if (!isSeparatedLeavesUpdateDistanceHandler(method) || alreadyPatched(method, skip.name)) {
                continue;
            }
            LabelNode cont = new LabelNode();
            InsnList prefix = new InsnList();
            prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
            prefix.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    SKIP_OWNER,
                    skip.name,
                    SKIP_DESC,
                    false
            ));
            prefix.add(new JumpInsnNode(Opcodes.IFEQ, cont));
            prefix.add(new InsnNode(Opcodes.RETURN));
            prefix.add(cont);
            method.instructions.insert(prefix);
        }
    }

    private static MethodNode findSkipMethod(ClassNode targetClass) {
        for (MethodNode method : targetClass.methods) {
            if (method.name.contains("shouldSkipSeparatedLeavesScan") && SKIP_DESC.equals(method.desc)) {
                return method;
            }
        }
        return null;
    }

    private static boolean isSeparatedLeavesUpdateDistanceHandler(MethodNode method) {
        return method.name.startsWith("handler$")
                && method.name.contains("updateDistance")
                && method.desc.contains("CallbackInfoReturnable")
                && (method.name.contains("separatedleaves") || method.name.contains("LeavesBlockMixin"));
    }

    private static boolean alreadyPatched(MethodNode method, String skipName) {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode call
                    && SKIP_OWNER.equals(call.owner)
                    && skipName.equals(call.name)) {
                return true;
            }
        }
        return false;
    }
}
