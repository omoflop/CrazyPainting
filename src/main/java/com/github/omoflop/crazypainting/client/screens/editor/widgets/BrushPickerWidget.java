package com.github.omoflop.crazypainting.client.screens.editor.widgets;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.client.CrazyPaintingClient;
import com.github.omoflop.crazypainting.client.screens.editor.BrushType;
import com.github.omoflop.crazypainting.client.screens.editor.types.EditorState;
import com.github.omoflop.crazypainting.client.screens.editor.types.EditorWidget;
import com.github.omoflop.crazypainting.client.screens.editor.types.MouseListener;
import com.github.omoflop.crazypainting.client.screens.editor.types.Renderable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class BrushPickerWidget extends EditorWidget implements Renderable, MouseListener {
    private static final Component BRUSH_SETTINGS_TEXT = Component.translatable("gui.crazypainting.painting_editor.brush_settings");

    private final EditorState state;
    private final Font textRenderer;
    private boolean leftJustDown;

    public BrushPickerWidget(EditorState state) {
        this.state = state;
        textRenderer = Minecraft.getInstance().font;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        context.drawCenteredString(textRenderer, BRUSH_SETTINGS_TEXT, centerX(), top() - textRenderer.lineHeight, CrazyPainting.YELLOW);

        int i = 0;
        for (String category : BrushType.getCategories()) {
            if (drawBrushCategoryButton(context, mouseX, mouseY, category, x, y+i*textRenderer.lineHeight) && leftJustDown && !state.brushCategory.equals(category)) {
                CrazyPaintingClient.click(2);
                state.brushCategory = category;
                leftJustDown = false;
            }

            i++;
        }

        i = 0;
        final int size = 16;
        for (BrushType type : BrushType.getBrushes(state.brushCategory)) {
            if (drawBrushSelectButton(context, mouseX, mouseY, right() - size, y+i*size, size, size, type) && leftJustDown && state.brushType != type) {
                CrazyPaintingClient.click(1.7f);
                leftJustDown = false;
                state.brushType = type;
            }
            i++;
        }

        leftJustDown = false;
    }

    private boolean drawBrushCategoryButton(GuiGraphics context, int mouseX, int mouseY, String category, int x, int y) {
        Component text = Component.literal(category);

        int width = textRenderer.width(text);
        int height = textRenderer.lineHeight;
        boolean mouseHovered = x <= mouseX && y <= mouseY && x+width > mouseX && y+height > mouseY;

        if (mouseHovered)
            context.fill(x, y, x+width, y+height, CrazyPainting.LIGHT_GRAY);

        int textColor = CrazyPainting.WHITE;
        if (state.brushCategory.equals(category)) {
            textColor = CrazyPainting.LIME;
        }

        context.drawString(textRenderer, text, x, y, textColor);

        return mouseHovered;
    }

    private boolean drawBrushSelectButton(GuiGraphics context, int mouseX, int mouseY, int x, int y, int width, int height, BrushType brush) {
        boolean mouseHovered = x <= mouseX && y <= mouseY && x+width > mouseX && y+height > mouseY;

        boolean selected = state.brushType == brush;

        int borderColor = selected ? CrazyPainting.WHITE : CrazyPainting.BLACK;
        int innerColor = selected ? CrazyPainting.LIME : CrazyPainting.WHITE;

        if (!selected && mouseHovered) {
            innerColor = CrazyPainting.LIGHT_GRAY;
        }

        Renderable.drawBorder(context, x, y, width, height, borderColor);
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, innerColor);

        brush.iteratePatternCentered(width/2, height/2, (px, py, ignored) -> {
            context.fill(x + px, y + py, x + px + 1, y + py + 1, selected ? CrazyPainting.WHITE : CrazyPainting.BLACK);
        });

        return mouseHovered;
    }

    @Override
    public boolean onMousePressed(MouseButtonEvent mouseButtonEvent, boolean bl) {
        if (mouseButtonEvent.button() == 0) leftJustDown = true;
        return false;
    }

    @Override
    public boolean onMouseReleased(MouseButtonEvent mouseButtonEvent) {
        return false;
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return false;
    }
}
