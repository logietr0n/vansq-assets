package com.vansqmod.mixin.effortless;

import com.vansqmod.integration.effortless.EffortlessReach;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Replaces the internal 1024-block trace cap used by multi-point shapes with attribute-based reach.
 */
public final class EffortlessShapeReachConstantMixin {

    private EffortlessShapeReachConstantMixin() {
    }

    @Mixin(targets = "dev.huskuraft.effortless.building.structure.builder.standard.Line", remap = false)
    public static class Line {
        @ModifyConstant(
                method = "traceLine(Ldev/huskuraft/effortless/api/core/Player;Ldev/huskuraft/effortless/api/core/BlockPosition;Ljava/util/Set;)Ldev/huskuraft/effortless/api/core/BlockInteraction;",
                constant = @Constant(intValue = 1024),
                require = 1
        )
        private static int vansqmod$reach(int original) {
            return EffortlessReach.traceReachOrDefault(original);
        }
    }

    @Mixin(targets = "dev.huskuraft.effortless.building.structure.builder.standard.Square", remap = false)
    public static class Square {
        @ModifyConstant(
                method = "traceSquare(Ldev/huskuraft/effortless/api/core/Player;Ldev/huskuraft/effortless/api/core/BlockInteraction;Ljava/util/Set;Z)Ldev/huskuraft/effortless/api/core/BlockInteraction;",
                constant = @Constant(intValue = 1024),
                require = 1
        )
        private static int vansqmod$reach(int original) {
            return EffortlessReach.traceReachOrDefault(original);
        }
    }

    @Mixin(targets = "dev.huskuraft.effortless.building.structure.builder.standard.Wall", remap = false)
    public static class Wall {
        @ModifyConstant(
                method = "traceWall(Ldev/huskuraft/effortless/api/core/Player;Ldev/huskuraft/effortless/api/core/BlockInteraction;Z)Ldev/huskuraft/effortless/api/core/BlockInteraction;",
                constant = @Constant(intValue = 1024),
                require = 1
        )
        private static int vansqmod$reach(int original) {
            return EffortlessReach.traceReachOrDefault(original);
        }
    }

    @Mixin(targets = "dev.huskuraft.effortless.building.structure.builder.standard.Floor", remap = false)
    public static class Floor {
        @ModifyConstant(
                method = "traceFloor(Ldev/huskuraft/effortless/api/core/Player;Ldev/huskuraft/effortless/api/core/BlockInteraction;Z)Ldev/huskuraft/effortless/api/core/BlockInteraction;",
                constant = @Constant(intValue = 1024),
                require = 1
        )
        private static int vansqmod$reach(int original) {
            return EffortlessReach.traceReachOrDefault(original);
        }
    }
}
