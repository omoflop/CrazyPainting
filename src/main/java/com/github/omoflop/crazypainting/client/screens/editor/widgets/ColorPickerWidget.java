package com.github.omoflop.crazypainting.client.screens.editor.widgets;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.client.ColorHelper;
import com.github.omoflop.crazypainting.client.CrazyPaintingClient;
import com.github.omoflop.crazypainting.client.screens.PaintingEditorScreen;
import com.github.omoflop.crazypainting.client.screens.editor.types.EditorState;
import com.github.omoflop.crazypainting.client.screens.editor.types.EditorWidget;
import com.github.omoflop.crazypainting.client.screens.editor.types.MouseListener;
import com.github.omoflop.crazypainting.client.screens.editor.types.Renderable;
import java.util.Collection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

import static com.github.omoflop.crazypainting.client.CrazyPaintingClient.click;

public class ColorPickerWidget extends EditorWidget implements Renderable, MouseListener {
    private static final Component PALETTE_TEXT = Component.translatable("gui.crazypainting.palette");
    private static final Component PALETTE_EMPTY_TEXT = Component.translatable("gui.crazypainting.palette.empty");

    private final EditorState state;
    private final Collection<Integer> colors;
    private final Font textRenderer;

    private boolean leftJustPressed = false;
    private boolean rightJustPressed = false;

    public ColorPickerWidget(EditorState state, Collection<Integer> colors) {
        this.state = state;
        this.colors = colors;

        textRenderer = Minecraft.getInstance().font;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        context.drawCenteredString(textRenderer, PALETTE_TEXT, centerX(), top() - textRenderer.lineHeight, CrazyPainting.YELLOW);
        if (colors.isEmpty()) {
            context.drawWordWrap(textRenderer, PALETTE_EMPTY_TEXT, x, y, width, CrazyPainting.RED, false);
            return;
        }

        drawPalette(context, mouseX, mouseY);
        drawBottomArea(context);

        leftJustPressed = false;
        rightJustPressed = false;
    }

    private void drawBottomArea(GuiGraphics context) {
        final int size = (int) (textRenderer.lineHeight * 1.5f);
        int y2 = bottom() - size * 2;

        if (colors.contains(state.primaryColor) || CrazyPainting.isNotVanillaColor(state.primaryColor)) {
            Component text = Component.literal(ColorHelper.hexString(state.primaryColor));
            drawColorBox(context, x, y2, width, size, CrazyPainting.LIGHT_GRAY, state.primaryColor);
            context.drawString(textRenderer, text, x + width/2 - textRenderer.width(text) / 2, y2 + 3, ColorHelper.contrast(state.primaryColor), false);

        }

        if (colors.contains(state.secondaryColor) || CrazyPainting.isNotVanillaColor(state.secondaryColor)) {
            y2 += size;
            Component text = Component.literal(ColorHelper.hexString(state.secondaryColor));
            drawColorBox(context, x, y2, width, size, CrazyPainting.GRAY, state.secondaryColor);
            context.drawString(textRenderer, text, x + width/2 - textRenderer.width(text)/2, y2+3, ColorHelper.contrast(state.secondaryColor), false);
        }

    }

    private void drawPalette(GuiGraphics context, int mouseX, int mouseY) {
        final int size = 12;

        int xx = x + 2;
        int yy = y + 2;
        for (int color : colors) {
            if (drawPaletteBox(context, mouseX, mouseY, xx, yy, size, size, color)) {
                if (color != state.primaryColor && leftJustPressed) {
                    state.primaryColor = color;
                    click(2);
                }
                if (color != state.secondaryColor && rightJustPressed) {
                    state.secondaryColor = color;
                    click(1.7f);
                }
            }

            xx += size;
            if (xx+size >= right()) {
                xx = x + 2;
                yy += size;
            }
        }

        for (int i = 4; i > 0; i--) {
            int ii = 4 - i;
            float opacity = (i*25)/100f;

            if (drawOpacityBox(context, mouseX, mouseY, x + size*ii + 2, yy + size*2, size, size, opacity) && leftJustPressed && state.opacity != opacity) {
                leftJustPressed = false;
                CrazyPaintingClient.click(opacity+.5f);
                state.opacity = opacity;
            }
        }


        if (drawToolBox(context, mouseX, mouseY, x + 2, (int) (yy + size*4), size+1, size+1, state.colorPickerActive, 16, 16, 7, 7) && leftJustPressed) {
            state.setColorPicker(!state.colorPickerActive);
        }
    }

    private boolean drawOpacityBox(GuiGraphics context, int mouseX, int mouseY, int x, int y, int width, int height, float opacity) {

        boolean isSelected = state.opacity == opacity;

        boolean isMouseOver = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

        int borderColor = CrazyPainting.BLACK;
        if (isSelected) borderColor = CrazyPainting.WHITE;
        int backgroundColor = isMouseOver ? CrazyPainting.WHITE : CrazyPainting.TRANSPARENT;

        if (opacity != 1f) {
            context.fill(x + 2, y + 2, x + width - 2, y + height - 2, CrazyPainting.WHITE);
        }
        drawColorBox(context, x, y, width, height, borderColor, ColorHelper.setOpacity(CrazyPainting.BLACK, opacity), backgroundColor);

        if (isMouseOver) {
            context.setTooltipForNextFrame(textRenderer, Component.translatable("gui.crazypainting.painting_editor.opacity", (int)(Math.floor(opacity*100)) + "%"), mouseX, mouseY);
        }

        return isMouseOver;
    }

    private boolean drawPaletteBox(GuiGraphics context, int mouseX, int mouseY, int x, int y, int width, int height, int color) {
        boolean isPrimary = color == state.primaryColor;
        boolean isSecondary = color == state.secondaryColor;
        boolean isMouseOver = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

        int borderColor = CrazyPainting.BLACK;
        if (isPrimary) borderColor = CrazyPainting.WHITE;
        if (isSecondary) borderColor = CrazyPainting.LIGHT_GRAY;
        int backgroundColor = color == CrazyPainting.WHITE ? CrazyPainting.LIGHT_GRAY : CrazyPainting.WHITE;
        if (!isMouseOver) backgroundColor = CrazyPainting.TRANSPARENT;

        drawColorBox(context, x, y, width, height, borderColor, color, backgroundColor);

        return isMouseOver;
    }

    private boolean drawToolBox(GuiGraphics context, int mouseX, int mouseY, int x, int y, int width, int height, boolean selected, int u, int v, int w, int h) {
        boolean isMouseOver = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

        int borderColor = selected ? CrazyPainting.WHITE : CrazyPainting.BLACK;
        int backgroundColor = CrazyPainting.LIGHT_GRAY;
        if (!isMouseOver) backgroundColor = CrazyPainting.TRANSPARENT;

        drawColorBox(context, x, y, width, height, borderColor, CrazyPainting.WHITE, backgroundColor);

        context.blit(RenderPipelines.GUI_TEXTURED, PaintingEditorScreen.EDITOR_TEXTURE_ID, x + width/2 - w/2, y + height/2 - h/2, u, v, w, h, 64, 64);

        return isMouseOver;
    }

    private void drawColorBox(GuiGraphics context, int x, int y, int width, int height, int borderColor, int innerColor, int backgroundColor) {
        Renderable.drawBorder(context, x, y, width, height, borderColor);

        if (backgroundColor != CrazyPainting.TRANSPARENT) {
            context.fill(x + 1, y + 1, x + width - 1, y + height - 1, backgroundColor);
        }

        if (innerColor == CrazyPainting.TRANSPARENT) {
            context.fill(x + 2, y + 2, x + width - 2, y + height - 2, CrazyPainting.GRAY);
            context.fill(x + 2, y + 2, x + width/2, y + height/2, CrazyPainting.LIGHT_GRAY);
            context.fill(x + width/2, y + height/2, x + width - 2, y + height - 2, CrazyPainting.LIGHT_GRAY);
        } else {
            context.fill(x + 2, y + 2, x + width - 2, y + height - 2, innerColor);
        }
    }

    private void drawColorBox(GuiGraphics context, int x, int y, int width, int height, int borderColor, int innerColor) {
        drawColorBox(context, x, y, width, height, borderColor, innerColor, CrazyPainting.TRANSPARENT);
    }

    @Override
    public boolean onMousePressed(MouseButtonEvent mouseButtonEvent, boolean bl) {
        int button = mouseButtonEvent.button();
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        if (!containsPoint(mouseX, mouseY)) return false;

        if (button == 0) leftJustPressed = true;
        if (button == 1) rightJustPressed = true;

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
