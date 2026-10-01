package com.vansqmod.compat;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cheap gates for Separated Leaves' {@code LeavesBlock.updateDistance} inject.
 * That handler structure-scans and biome-looks-up on every leaf distance update.
 */
public final class SeparatedLeavesPerf {

    private static final String RELOAD = "nl.teamdiopside.separatedleaves.Reload";
    private static final String RULE = "nl.teamdiopside.separatedleaves.Reload$LeavesRule";

    private static volatile boolean missing;
    private static Field rulesField;
    private static Method leavesMethod;
    private static volatile Set<Block> listedLeaves = Set.of();
    private static volatile int listedSize = -1;

    private SeparatedLeavesPerf() {
    }

    public static boolean shouldSkipUpdateDistance(BlockState state) {
        return !hasRuleFor(state.getBlock());
    }

    public static boolean hasRuleFor(Block block) {
        return listedLeafBlocks().contains(block);
    }

    private static Set<Block> listedLeafBlocks() {
        List<?> listed = rules();
        if (listed == null || listed.isEmpty()) {
            listedSize = 0;
            listedLeaves = Set.of();
            return listedLeaves;
        }
        int size = listed.size();
        Set<Block> cached = listedLeaves;
        if (size == listedSize && !cached.isEmpty()) {
            return cached;
        }
        Method leaves = leavesMethod;
        if (leaves == null) {
            return Set.of();
        }
        Set<Block> next = new HashSet<>();
        try {
            for (Object rule : listed) {
                @SuppressWarnings("unchecked")
                Set<Block> ruleLeaves = (Set<Block>) leaves.invoke(rule);
                if (ruleLeaves != null) {
                    next.addAll(ruleLeaves);
                }
            }
        } catch (ReflectiveOperationException ignored) {
            return Set.of();
        }
        listedLeaves = next;
        listedSize = size;
        return next;
    }

    private static List<?> rules() {
        if (missing) {
            return List.of();
        }
        try {
            Field field = rulesField;
            Method leaves = leavesMethod;
            if (field == null || leaves == null) {
                synchronized (SeparatedLeavesPerf.class) {
                    if (missing) {
                        return List.of();
                    }
                    if (rulesField == null) {
                        rulesField = Class.forName(RELOAD).getField("LEAVES_RULES");
                        leavesMethod = Class.forName(RULE).getMethod("leaves");
                    }
                    field = rulesField;
                    leaves = leavesMethod;
                }
            }
            @SuppressWarnings("unchecked")
            List<?> loaded = (List<?>) field.get(null);
            return loaded == null ? List.of() : loaded;
        } catch (ReflectiveOperationException ignored) {
            missing = true;
            return List.of();
        }
    }
}
