package com.vansqmod.mixin;

import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

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
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith(".FarmersDelightKnifeStatsMixin")) {
            return isModLoaded("farmersdelight");
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
        if (mixinClassName.contains(".caverns.")) {
            return isModLoaded("caverns_and_chasms");
        }
        if (mixinClassName.contains(".effortless.")) {
            return isModLoaded("effortless");
        }
        if (mixinClassName.contains(".galosphere.")) {
            return isModLoaded("galosphere");
        }
        if (mixinClassName.contains(".beltborne.")) {
            return isModLoaded("beltborne_lanterns") && isModLoaded("curios");
        }
        if (mixinClassName.contains(".spyglass.")) {
            return isModLoaded("spyglass_astronomy") && isModLoaded("spyglass_improvements");
        }
        if (mixinClassName.contains(".combatnouveau.")) {
            return isModLoaded("combatnouveau");
        }
        if (mixinClassName.contains(".mapatlases.")) {
            return isModLoaded("map_atlases");
        }
        if (mixinClassName.contains(".copperage.")) {
            return isModLoaded("copperagebackport");
        }
        if (mixinClassName.endsWith(".MonsterBoxLivingFlameMixin")
                || mixinClassName.endsWith(".MonsterBoxSoundTypeMixin")) {
            return isModLoaded("quark") && isModLoaded("dungeonsdelight");
        }
        if (mixinClassName.contains(".quark.") || mixinClassName.endsWith(".QuarkAttributeTooltipsMixin")) {
            return isModLoaded("quark");
        }
        if (mixinClassName.contains(".yungscavebiomes.")) {
            return isModLoaded("yungscavebiomes");
        }
        if (mixinClassName.contains(".distanthorizons.")) {
            return isModLoaded("distanthorizons");
        }
        if (mixinClassName.contains(".peaceless.")) {
            return isModLoaded("peaceless");
        }
        if (mixinClassName.contains(".subtleeffects.")) {
            return isModLoaded("subtle_effects") && isModLoaded("endrem");
        }
        if (mixinClassName.contains(".essential.")) {
            return isModLoaded("essential");
        }
        if (mixinClassName.contains(".heartstone.")) {
            return isModLoaded("heartstone");
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
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
