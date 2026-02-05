package com.github.omoflop.crazypainting.client.datagen;

import com.github.omoflop.crazypainting.client.models.canvas.CanvasSpecialRenderer;
import com.github.omoflop.crazypainting.content.CrazyComponents;
import com.github.omoflop.crazypainting.content.CrazyItems;
import com.github.omoflop.crazypainting.items.CanvasItem;
import com.github.omoflop.crazypainting.items.PaletteItem;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.*;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ConditionalItemModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.conditional.HasComponent;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;

public class CrazyModelProvider extends FabricModelProvider {

    public CrazyModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {

    }

    @Override
    public void generateItemModels(ItemModelGenerators gen) {
        registerBasic(gen, CrazyItems.EASEL_ITEM);
        registerPalette(gen, CrazyItems.PALETTE_ITEM);

        CrazyItems.allCanvases.forEach(canvas -> registerCanvas(gen, canvas));
    }

    private void registerBasic(ItemModelGenerators gen, Item item) {
        gen.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
    }

    private void registerCanvas(ItemModelGenerators gen, CanvasItem item) {
        ItemModel.Unbaked emptyModel = ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(item));
        ItemModel.Unbaked paintedModel = ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(item, "_painted"));
        ItemModel.Unbaked specialModel = ItemModelUtils.specialModel(ModelLocationUtils.getModelLocation(item, "_painted"), new CanvasSpecialRenderer.Unbaked());

        ItemModel.Unbaked paintedModelWithCondition = ItemModelUtils.select(new DisplayContext(), specialModel, ItemModelUtils.when(ItemDisplayContext.GROUND, paintedModel));

        // Create the item asset
        ConditionalItemModel.Unbaked canvasModel = new ConditionalItemModel.Unbaked(new HasComponent(CrazyComponents.CANVAS_DATA, false), paintedModelWithCondition, emptyModel);
        ClientItem asset = new ClientItem(canvasModel, new ClientItem.Properties(true, true, 1));
        gen.itemModelOutput.accept(item, asset.model());
        gen.createFlatItemModel(item, "", ModelTemplates.FLAT_ITEM);
        gen.createFlatItemModel(item, "_painted", ModelTemplates.FLAT_ITEM);
    }

    private void registerPalette(ItemModelGenerators gen, PaletteItem item) {
        var modelFilled = ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(item, "_filled"));
        var model = ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(item));
        gen.generateBooleanDispatch(item, new HasComponent(CrazyComponents.PALETTE_COLORS, true), modelFilled, model);
        gen.createFlatItemModel(item, "", ModelTemplates.FLAT_ITEM); // ???? lol
        gen.createFlatItemModel(item, "_filled", ModelTemplates.FLAT_ITEM);
    }
}