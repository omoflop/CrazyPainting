package com.github.omoflop.crazypainting.client.models.canvas;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.client.models.CanvasRenderer;
import com.github.omoflop.crazypainting.components.CanvasDataComponent;
import com.github.omoflop.crazypainting.content.CrazyComponents;
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
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3fc;

import java.util.Optional;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class CanvasSpecialRenderer implements SpecialModelRenderer<CanvasSpecialRenderer.Data> {
    public static final Identifier SPECIAL_ID = CrazyPainting.id("canvas");


    @Override
    public void submit(@NotNull Data data, ItemDisplayContext displayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, boolean bl, int k) {

        Optional<Identifier> textureId = CanvasRenderer.tryGetCanvasId(data.canvasId);
        if (textureId.isEmpty()) return;

        poseStack.pushPose();
        boolean glow = displayContext == ItemDisplayContext.GUI || data.glow();

        submitNodeCollector.submitCustomGeometry(poseStack, CanvasRenderer.getRenderType(textureId.get(), glow), (pose, vertexConsumer) -> {
            poseStack.pushPose();
            CanvasRenderer.prepareForItem(poseStack, displayContext == ItemDisplayContext.GUI, displayContext, data.width, data.height);
            CanvasRenderer.submitFront(vertexConsumer, pose, data.width, data.height, i);
            poseStack.popPose();
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

    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<CanvasSpecialRenderer.Unbaked> CODEC = MapCodec.unit(new CanvasSpecialRenderer.Unbaked());


        @Override
        public SpecialModelRenderer<?> bake(BakingContext bakingContext) {
            return new CanvasSpecialRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
            return CODEC;
        }
    }
}
