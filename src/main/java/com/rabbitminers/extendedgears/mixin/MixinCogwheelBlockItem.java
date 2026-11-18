package com.rabbitminers.extendedgears.mixin;

import com.simibubi.create.content.kinetics.simpleRelays.CogwheelBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = CogwheelBlockItem.class, remap = false)
public class MixinCogwheelBlockItem extends BlockItem {
    public MixinCogwheelBlockItem(Block block, Properties properties) {
        super(block, properties);
    }
}
