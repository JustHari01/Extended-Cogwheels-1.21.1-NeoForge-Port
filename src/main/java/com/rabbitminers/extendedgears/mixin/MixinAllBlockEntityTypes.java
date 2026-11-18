package com.rabbitminers.extendedgears.mixin;

import com.rabbitminers.extendedgears.ExtendedCogwheels;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsBlocks;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.foundation.data.CreateBlockEntityBuilder;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.Arrays;

@Mixin(value = AllBlockEntityTypes.class, remap = false)
public class MixinAllBlockEntityTypes {
    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/data/CreateBlockEntityBuilder;validBlocks([Lcom/tterrag/registrate/util/nullness/NonNullSupplier;)Lcom/tterrag/registrate/builders/BlockEntityBuilder;",
                    ordinal = 1,
                    remap = false
            ),
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=schematic_table"),
                    to = @At(value = "CONSTANT", args = "stringValue=motor")
            ),
            remap = false
    )
    @SuppressWarnings("unchecked")
    private static <T extends Block, E extends BlockEntity> BlockEntityBuilder<E, ?> addCustomCogwheelsToBlockEntity(
            CreateBlockEntityBuilder<E, ?> instance,
            NonNullSupplier<T>[] blocks
    ) {
        NonNullSupplier<T>[] combined = Arrays.copyOf(blocks, blocks.length + 4);
        combined[blocks.length] = (NonNullSupplier<T>) (Object) ExtendedCogwheelsBlocks.SHAFTLESS_COGWHEEL;
        combined[blocks.length + 1] = (NonNullSupplier<T>) (Object) ExtendedCogwheelsBlocks.LARGE_SHAFTLESS_COGWHEEL;
        combined[blocks.length + 2] = (NonNullSupplier<T>) (Object) ExtendedCogwheelsBlocks.HALF_SHAFT_COGWHEEL;
        combined[blocks.length + 3] = (NonNullSupplier<T>) (Object) ExtendedCogwheelsBlocks.LARGE_HALF_SHAFT_COGWHEEL;
        return instance.validBlocks(combined);
    }
}
