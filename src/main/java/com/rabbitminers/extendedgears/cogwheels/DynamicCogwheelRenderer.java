package com.rabbitminers.extendedgears.cogwheels;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogVisual;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.rabbitminers.extendedgears.cogwheels.materials.ClientCogwheelMaterial;
import com.rabbitminers.extendedgears.cogwheels.materials.CogwheelMaterialManager;
import com.rabbitminers.extendedgears.registry.ExtendedCogwheelsPartials;
import com.simibubi.create.foundation.model.BakedModelHelper;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import dev.engine_room.flywheel.api.instance.Instance;

import java.util.List;
import java.util.Map;
import java.util.Optional;


public class DynamicCogwheelRenderer {
    public static final ResourceLocation STRIPPED_LOG_TEMPLATE = ResourceLocation.withDefaultNamespace("block/stripped_spruce_log");
    public static final ResourceLocation STRIPPED_LOG_TOP_TEMPLATE = ResourceLocation.withDefaultNamespace("block/stripped_spruce_log_top");

    public static final String[] LOG_SUFFIXES = new String[] { "_log", "_stem" };

    public static @NotNull BakedModel generateModel(CogwheelModelKey key) {
        ClientCogwheelMaterial material = CogwheelMaterialManager.clientOf(key.material());
        PartialModel model = material != null ? material.getModel(key.large())
                : standardCogwheelModel(key.large());
        return generateModel(model.get(), key.material());
    }

    public static PartialModel standardCogwheelModel(boolean isLarge) {
        return isLarge ? ExtendedCogwheelsPartials.LARGE_COGWHEEL : ExtendedCogwheelsPartials.COGWHEEL;
    }

    public static Model createFlywheelModel(CogwheelModelKey key) {
        return new BakedModelBuilder(generateModel(key)).build();
    }

    public static void registerVisualizers() {
        var bracketedType = AllBlockEntityTypes.BRACKETED_KINETIC.get();
        VisualizerRegistry.setVisualizer(bracketedType, SimpleBlockEntityVisualizer.builder(bracketedType)
                .factory((context, blockEntity, partialTick) -> {
                    if (!(blockEntity instanceof com.rabbitminers.extendedgears.mixin_interface.IDynamicMaterialBlockEntity dynamic))
                        return BracketedKineticBlockEntityVisual.create(context, blockEntity, partialTick);
                    BlockState state = blockEntity.getBlockState();
                    if (!(state.getBlock() instanceof com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock))
                        return BracketedKineticBlockEntityVisual.create(context, blockEntity, partialTick);
                    CogwheelModelKey key = new CogwheelModelKey(
                            com.simibubi.create.content.kinetics.simpleRelays.ICogWheel.isLargeCog(state),
                            state, dynamic.getMaterial());
                    Model model = createFlywheelModel(key);
                    if (key.large())
                        return new LargeCogVisual(context, blockEntity, partialTick, model);
                    return new SingleAxisRotatingVisual<>(context, blockEntity, partialTick, model);
                }).apply());

        var smallEncasedType = AllBlockEntityTypes.ENCASED_COGWHEEL.get();
        VisualizerRegistry.setVisualizer(smallEncasedType, SimpleBlockEntityVisualizer.builder(smallEncasedType)
                .factory((context, blockEntity, partialTick) -> encasedVisual(context, blockEntity, false, partialTick))
                .apply());

        var largeEncasedType = AllBlockEntityTypes.ENCASED_LARGE_COGWHEEL.get();
        VisualizerRegistry.setVisualizer(largeEncasedType, SimpleBlockEntityVisualizer.builder(largeEncasedType)
                .factory((context, blockEntity, partialTick) -> encasedVisual(context, blockEntity, true, partialTick))
                .apply());
    }

    private static EncasedCogVisual encasedVisual(
            dev.engine_room.flywheel.api.visualization.VisualizationContext context,
            com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity blockEntity,
            boolean large, float partialTick) {
        Model model;
        if (blockEntity instanceof com.rabbitminers.extendedgears.mixin_interface.IDynamicMaterialBlockEntity dynamic) {
            model = createFlywheelModel(new CogwheelModelKey(large, blockEntity.getBlockState(), dynamic.getMaterial()));
        } else {
            model = dev.engine_room.flywheel.lib.model.Models.partial(
                    large ? com.simibubi.create.AllPartialModels.SHAFTLESS_LARGE_COGWHEEL
                            : com.simibubi.create.AllPartialModels.SHAFTLESS_COGWHEEL);
        }
        return new EncasedCogVisual(context, blockEntity, large, partialTick, model);
    }

    private static final class LargeCogVisual extends SingleAxisRotatingVisual<BracketedKineticBlockEntity> {
        private final RotatingInstance additionalShaft;

        private LargeCogVisual(
                dev.engine_room.flywheel.api.visualization.VisualizationContext context,
                BracketedKineticBlockEntity blockEntity, float partialTick, Model cogwheelModel) {
            super(context, blockEntity, partialTick, cogwheelModel);
            Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);
            additionalShaft = instancerProvider()
                    .instancer(AllInstanceTypes.ROTATING, dev.engine_room.flywheel.lib.model.Models.partial(AllPartialModels.COGWHEEL_SHAFT))
                    .createInstance();
            additionalShaft.rotateToFace(axis)
                    .setup(blockEntity)
                    .setRotationOffset(com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer
                            .getShaftAngleOffset(axis, blockEntity.getBlockPos()))
                    .setPosition(getVisualPosition())
                    .setChanged();
        }

        @Override
        public void update(float partialTick) {
            super.update(partialTick);
            Direction.Axis axis = rotationAxis();
            additionalShaft.setup(blockEntity)
                    .setRotationOffset(com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer
                            .getShaftAngleOffset(axis, blockEntity.getBlockPos()))
                    .setChanged();
        }

        @Override
        public void updateLight(float partialTick) {
            super.updateLight(partialTick);
            relight(additionalShaft);
        }

        @Override
        protected void _delete() {
            super._delete();
            additionalShaft.delete();
        }

        @Override
        public void collectCrumblingInstances(java.util.function.Consumer<Instance> consumer) {
            super.collectCrumblingInstances(consumer);
            consumer.accept(additionalShaft);
        }
    }

    public static BakedModel generateModel(BakedModel template, ResourceLocation id) {
        Map<TextureAtlasSprite, TextureAtlasSprite> map = new Reference2ReferenceOpenHashMap<>();
        String path = id.getPath();

        if (path.endsWith("_planks")) {
            String namespace = id.getNamespace();
            String wood = path.substring(0, path.length() - 7);
            BlockState logBlockState = getStrippedLogState(namespace, wood);

            map.put(getSprite(STRIPPED_LOG_TEMPLATE), getSpriteOnSide(logBlockState, Direction.SOUTH));
            map.put(getSprite(STRIPPED_LOG_TOP_TEMPLATE), getSpriteOnSide(logBlockState, Direction.UP));
        } else {
            ClientCogwheelMaterial material = CogwheelMaterialManager.clientOf(id);
            if (material == null)
                return BakedModelHelper.generateModel(template, sprite -> null);
            material.textures().forEach((old, replacement) -> map.put(getSprite(old), getSprite(replacement)));
        }

        return BakedModelHelper.generateModel(template, map::get);
    }

    public static BlockState getStrippedLogState(String namespace, String wood) {
        for (String suffix : LOG_SUFFIXES) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, "stripped_" + wood + suffix);
            Optional<BlockState> state = BuiltInRegistries.BLOCK.getOptional(id).map(Block::defaultBlockState);
            if (state.isPresent())
                return state.get();
        }
        return Blocks.OAK_LOG.defaultBlockState();
    }

    private static TextureAtlasSprite getSprite(ResourceLocation id) {
        return Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(id);
    }

    public static TextureAtlasSprite getSpriteOnSide(BlockState state, Direction side) {
        BakedModel model = Minecraft.getInstance()
                .getBlockRenderer()
                .getBlockModel(state);
        if (model == null)
            return null;

        RandomSource random = RandomSource.create();
        random.setSeed(42L);

        List<BakedQuad> quads = model.getQuads(state, side, random);
        if (!quads.isEmpty()) {
            return quads.get(0)
                    .getSprite();
        }
        random.setSeed(42L);
        quads = model.getQuads(state, null, random);
        if (!quads.isEmpty()) {
            for (BakedQuad quad : quads) {
                if (quad.getDirection() == side) {
                    return quad.getSprite();
                }
            }
        }
        return model.getParticleIcon();
    }
}
