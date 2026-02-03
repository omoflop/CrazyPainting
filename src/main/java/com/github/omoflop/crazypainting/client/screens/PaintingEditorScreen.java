package com.github.omoflop.crazypainting.client.screens;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.client.screens.editor.widgets.*;
import com.github.omoflop.crazypainting.client.screens.editor.NetSync;
import com.github.omoflop.crazypainting.client.screens.editor.types.EditorState;
import com.github.omoflop.crazypainting.client.texture.CanvasTexture;
import com.github.omoflop.crazypainting.items.PaletteItem;
import com.github.omoflop.crazypainting.network.event.PaintingChangeEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Environment(EnvType.CLIENT)
public class PaintingEditorScreen extends AbstractPaintingScreen {
    public static final Identifier EDITOR_TEXTURE_ID = CrazyPainting.id("textures/gui/editor.png");

    private static final Component TITLE_PLACEHOLDER_TEXT = Component.translatable("gui.crazypainting.painting_editor.title");

    private final CanvasWidget canvasWidget;
    private final ColorPickerWidget colorPicker;
    private final BrushPickerWidget brushPicker;
    private final HelpWidget help;
    private final SignWidget sign;

    private final NetSync netSync;

    public PaintingEditorScreen(PaintingChangeEvent event, CanvasTexture texture, LocalPlayer player, int easelEntityId) {
        super(TITLE_PLACEHOLDER_TEXT);

        AtomicBoolean hasChanges = new AtomicBoolean(false);
        netSync = new NetSync(texture, event.change().orElseThrow(), this::onClose, hasChanges, easelEntityId);

        ItemStack paletteStack = player.getItemBySlot(EquipmentSlot.MAINHAND);
        Collection<Integer> colors;
        if (paletteStack.isEmpty() || !(paletteStack.getItem() instanceof PaletteItem)) colors = List.of();
        else colors = PaletteItem.getColors(paletteStack);

        EditorState state = new EditorState(colors);
        canvasWidget = new CanvasWidget(state, texture, hasChanges);
        colorPicker  = new ColorPickerWidget(state, colors);
        brushPicker  = new BrushPickerWidget(state);
        sign = new SignWidget(state, netSync);
        help  = new HelpWidget();
        drawDebugBoundaries = false;
    }

    @Override
    protected void init() {
        super.init();

        addWidget(canvasWidget);
        addWidget(colorPicker);
        addWidget(brushPicker);
        addWidget(help);
        addWidget(sign);
    }

    @Override
    protected void recalculateWidgetPositions(int width, int height) {
        super.recalculateWidgetPositions(width, height);

        canvasWidget.alignXCenter(width);
        canvasWidget.alignYCenter(height);

        final int sideWidth = width / 10;
        final int sideHeight = height / 2;

        colorPicker.alignXBetween(0, canvasWidget.x);
        colorPicker.x = Math.max(colorPicker.x, 0);
        colorPicker.alignYCenter(height);
        colorPicker.width = sideWidth;
        colorPicker.height = sideHeight;

        brushPicker.alignXBetween(canvasWidget.right(), width);
        brushPicker.x = Math.min(brushPicker.x, width - brushPicker.width);
        brushPicker.alignYCenter(height);
        brushPicker.width = sideWidth;
        brushPicker.height = sideHeight;

        help.x = 2;
        help.y = 2;
        help.width = 12;
        help.height = 12;

        sign.width = canvasWidget.width;
        sign.height = 30;
        sign.alignXCenter(width);
        sign.y = canvasWidget.bottom() + 2;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void tick() {
        netSync.tick();
        super.tick();
    }

    @Override
    public void onClose() {
        super.onClose();
        netSync.close();
        Minecraft.getInstance().mouseHandler.grabMouse();
    }
}
