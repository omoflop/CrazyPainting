package com.github.omoflop.crazypainting.client.models.canvas;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.client.CrazyPaintingClient;
import com.github.omoflop.crazypainting.client.models.CanvasRenderer;
import com.github.omoflop.crazypainting.items.CanvasItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;

import java.util.Optional;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class CanvasSpecialRenderer implements SpecialModelRenderer<CanvasSpecialRenderer.Data> {
    public static final Identifier SPECIAL_ID = CrazyPainting.id("canvas");


    @Override
    public void submit(Data data, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, boolean bl, int k) {
        Optional<Identifier> textureId = CanvasRenderer.tryGetCanvasId(data.canvasId);

        if (textureId.isEmpty()) {
            textureId = Optional.of(CrazyPainting.id("textures/entity/easel/canvas.png"));
        }

        poseStack.pushPose();
        boolean glow = CrazyPaintingClient.displayContext == ItemDisplayContext.GUI || data.glow();

        CanvasRenderer.prepareForItem(poseStack, CrazyPaintingClient.displayContext == ItemDisplayContext.GUI, CrazyPaintingClient.displayContext, data.width, data.height);
        submitNodeCollector.submitCustomGeometry(poseStack, CanvasRenderer.getRenderType(textureId.get(), glow), (pose, vertexConsumer) -> {
            CanvasRenderer.submitFront(vertexConsumer, pose, data.width, data.height, i);
        });

        submitNodeCollector.submitCustomGeometry(poseStack, CanvasRenderer.getBackRenderType(), (pose, vertexConsumer) -> {
            CanvasRenderer.submitBack(vertexConsumer, pose, data.width, data.height, i);
        });

        poseStack.popPose();

    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {

    }

    @Override
    public Data extractArgument(ItemStack stack) {
        if (!(stack.getItem() instanceof CanvasItem item)) return Data.EMPTY;

        return new Data(item.width, item.height, CanvasItem.getCanvasId(stack), CanvasItem.getGlow(stack));
    }

    public record Data(int width, int height, int canvasId, boolean glow) {
        public static final Data EMPTY = new Data(1, 1, -1, false);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<Data> {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new CanvasSpecialRenderer.Unbaked());


        @Override
        public SpecialModelRenderer<Data> bake(BakingContext bakingContext) {
            return new CanvasSpecialRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
