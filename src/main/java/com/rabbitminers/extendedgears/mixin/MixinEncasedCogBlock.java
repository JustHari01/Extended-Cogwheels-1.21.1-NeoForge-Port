package com.rabbitminers.extendedgears.mixin;

import com.rabbitminers.extendedgears.mixin_interface.IDynamicMaterialBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EncasedCogwheelBlock.class, remap = false)
public class MixinEncasedCogBlock extends Block {
    public MixinEncasedCogBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                       Player player, net.minecraft.world.InteractionHand hand,
                                                       BlockHitResult ray) {
        ItemInteractionResult resp = super.useItemOn(stack, state, level, pos, player, hand, ray);
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IDynamicMaterialBlockEntity dyn) || level.isClientSide)
            return resp;
        InteractionResult result = dyn.applyMaterialIfValid(stack);
        if (result == InteractionResult.PASS)
            return resp;
        if (result == InteractionResult.FAIL)
            return ItemInteractionResult.FAIL;
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Redirect(
            method = "handleEncasing",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;switchToBlockState(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"
                    ,remap = false
            )
    )
    public void transferMaterial(Level level, BlockPos pos, BlockState state) {
        if (!(level.getBlockEntity(pos) instanceof IDynamicMaterialBlockEntity cogWheelBlockEntity))
            return;
        ResourceLocation material = cogWheelBlockEntity.getMaterial();
        KineticBlockEntity.switchToBlockState(level, pos, state);
        if (!(level.getBlockEntity(pos) instanceof IDynamicMaterialBlockEntity encasedBlockEntity))
            return;
        encasedBlockEntity.applyMaterial(material);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}
