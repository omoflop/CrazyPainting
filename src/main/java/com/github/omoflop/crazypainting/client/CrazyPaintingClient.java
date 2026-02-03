package com.github.omoflop.crazypainting.client;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.client.models.canvas.CanvasEntityRenderer;
import com.github.omoflop.crazypainting.client.models.canvas.CanvasSpecialRenderer;
import com.github.omoflop.crazypainting.client.models.easel.EaselEntityModel;
import com.github.omoflop.crazypainting.client.models.easel.EaselEntityRenderer;
import com.github.omoflop.crazypainting.client.network.ClientPaintingChangeHandler;
import com.github.omoflop.crazypainting.client.network.PaintingCanUpdateHandler;
import com.github.omoflop.crazypainting.client.network.PaintingUpdateHandler;
import com.github.omoflop.crazypainting.client.network.UpdateEaselCanvasIdHandler;
import com.github.omoflop.crazypainting.client.resources.BrushReloadListener;
import com.github.omoflop.crazypainting.content.CrazyEntities;
import com.github.omoflop.crazypainting.network.event.PaintingChangeEvent;
import com.github.omoflop.crazypainting.network.s2c.PaintingCanUpdateS2C;
import com.github.omoflop.crazypainting.network.s2c.PaintingUpdateS2C;
import com.github.omoflop.crazypainting.network.s2c.UpdateEaselCanvasIdS2C;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.server.packs.PackType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class CrazyPaintingClient implements ClientModInitializer {

    public static final ModelLayerLocation CANVAS_MODEL_LAYER = new ModelLayerLocation(CrazyPainting.id("canvas"), "main");
    public static final ModelLayerLocation EASEL_MODEL_LAYER = new ModelLayerLocation(CrazyPainting.id("easel"), "main");


    @Override
    public void onInitializeClient() {

        // Register special model type for canvas rendering
        SpecialModelRenderers.ID_MAPPER.put(CanvasSpecialRenderer.SPECIAL_ID, CanvasSpecialRenderer.Unbaked.CODEC);

        // Register easel model
        EntityModelLayerRegistry.registerModelLayer(EASEL_MODEL_LAYER, EaselEntityModel::getTexturedModelData);

        // Register easel and canvas entity renderers
        EntityRendererRegistry.register(CrazyEntities.EASEL_ENTITY_TYPE, (EaselEntityRenderer::new));
        EntityRendererRegistry.register(CrazyEntities.CANVAS_ENTITY_TYPE, (CanvasEntityRenderer::new));

        // Packet receivers
        ClientPlayNetworking.registerGlobalReceiver(PaintingChangeEvent.ID, new ClientPaintingChangeHandler());
        ClientPlayNetworking.registerGlobalReceiver(PaintingUpdateS2C.ID, new PaintingUpdateHandler());
        ClientPlayNetworking.registerGlobalReceiver(PaintingCanUpdateS2C.ID, new PaintingCanUpdateHandler());
        ClientPlayNetworking.registerGlobalReceiver(UpdateEaselCanvasIdS2C.ID, new UpdateEaselCanvasIdHandler());

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new BrushReloadListener());
    }

    public static void play(SoundEvent sound, float pitch) {
        play(sound, 0.5f, pitch);
    }
    public static void play(SoundEvent sound, float volume, float pitch) {
        Minecraft.getInstance().player.playSound(sound, volume, pitch);
    }

    public static void click(float pitch) {
        play(SoundEvents.UI_BUTTON_CLICK.value(), pitch);
    }

}
