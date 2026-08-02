package com.vansqmod.block;

import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.util.valueproviders.UniformInt;

public class GildedAncientSandstone extends DropExperienceBlock {

    public GildedAncientSandstone(BlockBehaviour.Properties properties) {
        super(UniformInt.of(0, 1), properties);
    }

}