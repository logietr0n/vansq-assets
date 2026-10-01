package com.vansqmod.mixin.caverns;

import com.vansqmod.integration.caverns.LostCavesTinMonoliths;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Lets Lost Caves tin monoliths actually generate: biome check at cave height, no spawn square ban.
 */
@Mixin(targets = "com.teamabnormals.caverns_and_chasms.common.levelgen.structure.TinMonolithStructure", remap = false)
public abstract class TinMonolithStructureMixin extends Structure {

    private TinMonolithStructureMixin(Structure.StructureSettings settings) {
        super(settings);
    }

    @Invoker("generatePieces")
    protected abstract void vansqmod$generatePieces(StructurePiecesBuilder builder, Structure.GenerationContext context);

    @Inject(method = "findGenerationPoint", at = @At("HEAD"), cancellable = true)
    private void vansqmod$lostCavesMonolith(
            Structure.GenerationContext context,
            CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir
    ) {
        if (!LostCavesTinMonoliths.isLostCavesOnlyStructure(this)) {
            return;
        }
        int y = LostCavesTinMonoliths.findLostCavesY(context);
        if (y == Integer.MIN_VALUE) {
            return;
        }
        ChunkPos chunk = context.chunkPos();
        BlockPos origin = new BlockPos(chunk.getMinBlockX(), y, chunk.getMinBlockZ());
        cir.setReturnValue(Optional.of(new Structure.GenerationStub(origin, builder -> vansqmod$generatePieces(builder, context))));
    }
}
