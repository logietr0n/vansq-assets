package com.vansqmod.mixin.quark;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.yirmiri.dungeonsdelight.core.registry.DDSoundTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Quark Monster Box uses stained-scrap block sounds, matching Dungeons Delight's spawner treatment.
 * Activation growl ({@code quark:block.monster_box.growl}) is unchanged.
 */
@Mixin(BlockBehaviour.class)
public abstract class MonsterBoxSoundTypeMixin {

    private static final ResourceLocation MONSTER_BOX =
            ResourceLocation.fromNamespaceAndPath("quark", "monster_box");

    @Inject(method = "getSoundType", at = @At("HEAD"), cancellable = true)
    private void vansqmod$monsterBoxStainedScrap(BlockState state, CallbackInfoReturnable<SoundType> cir) {
        if (MONSTER_BOX.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()))) {
            cir.setReturnValue(DDSoundTypes.STAINED_SCRAP);
        }
    }
}
