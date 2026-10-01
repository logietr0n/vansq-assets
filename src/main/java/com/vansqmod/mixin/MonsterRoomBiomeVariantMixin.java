package com.vansqmod.mixin;

import com.vansqmod.worldgen.BiomeMonsterRooms;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces vanilla cobble monster rooms in YUNG Lost/Frosted Caves, then with a
 * hanging-spawner deepslate room. Remaining vanilla rooms use Quark cobblestone
 * bricks. Priority is below Dungeons Delight's HEAD inject so this runs first and
 * those biomes keep the sandstone/permafrost rooms instead of rotten dungeons.
 *
 * Distant Horizons FEATURES generation runs this on isolated worker threads
 * that cannot load vansqmod classes; skip there so LOD gen and world save
 * are not stalled by {@link NoClassDefFoundError}.
 */
@Mixin(value = MonsterRoomFeature.class, priority = 500)
public class MonsterRoomBiomeVariantMixin {

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void vansqmod$biomeVariants(
            FeaturePlaceContext<NoneFeatureConfiguration> context,
            CallbackInfoReturnable<Boolean> cir
    ) {
        String threadName = Thread.currentThread().getName();
        if (threadName.startsWith("DH-")
                || threadName.contains("DistantHorizons")
                || threadName.contains("LOD World Gen")) {
            return;
        }
        try {
            Boolean placed = BiomeMonsterRooms.placeIfVariant(context);
            if (placed != null) {
                cir.setReturnValue(placed);
            }
        } catch (NoClassDefFoundError ignored) {
            // Isolated DH worldgen classloaders cannot see vansqmod.
        }
    }

    @Redirect(
            method = "place",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETSTATIC,
                    target = "Lnet/minecraft/world/level/block/Blocks;COBBLESTONE:Lnet/minecraft/world/level/block/Block;"
            )
    )
    private Block vansqmod$quarkCobblestoneBricks() {
        try {
            return BiomeMonsterRooms.quarkBrickOr(BiomeMonsterRooms.COBBLESTONE_BRICKS, Blocks.COBBLESTONE);
        } catch (NoClassDefFoundError ignored) {
            return Blocks.COBBLESTONE;
        }
    }

    @Redirect(
            method = "place",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETSTATIC,
                    target = "Lnet/minecraft/world/level/block/Blocks;MOSSY_COBBLESTONE:Lnet/minecraft/world/level/block/Block;"
            )
    )
    private Block vansqmod$quarkMossyCobblestoneBricks() {
        try {
            return BiomeMonsterRooms.quarkBrickOr(BiomeMonsterRooms.MOSSY_COBBLESTONE_BRICKS, Blocks.MOSSY_COBBLESTONE);
        } catch (NoClassDefFoundError ignored) {
            return Blocks.MOSSY_COBBLESTONE;
        }
    }
}
