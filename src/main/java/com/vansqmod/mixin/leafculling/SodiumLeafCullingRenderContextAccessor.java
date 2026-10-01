package com.vansqmod.mixin.leafculling;

import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
        targets = "net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext",
        remap = false
)
public interface SodiumLeafCullingRenderContextAccessor {

    @Accessor("state")
    BlockState vansqmod$getState();

    @Accessor("slice")
    LevelSlice vansqmod$getSlice();

    @Accessor("pos")
    BlockPos vansqmod$getPos();
}
