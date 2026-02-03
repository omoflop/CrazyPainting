package com.github.omoflop.crazypainting.client.screens.editor.widgets;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.client.ColorHelper;
import com.github.omoflop.crazypainting.client.CrazyPaintingClient;
import com.github.omoflop.crazypainting.client.screens.editor.types.*;
import com.github.omoflop.crazypainting.client.texture.CanvasTexture;
import com.github.omoflop.crazypainting.content.CrazySounds;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;

import static com.github.omoflop.crazypainting.client.screens.PaintingEditorScreen.EDITOR_TEXTURE_ID;

public class CanvasWidget extends EditorWidget implements Renderable, MouseListener, KeyListener, Tickable {
    private final EditorState state;
    private final CanvasTexture texture;
    private final DrawHelper drawHelper;

    public int zoom = 10;
    public boolean panStart;
    public int panX;
    public int panY;
    public double panOffsetX;
    public double panOffsetY;

    public boolean leftDown = false;
    public boolean rightDown = false;
    private boolean leftJustDown;
    private boolean rightJustDown;

    public CanvasWidget(EditorState state, CanvasTexture texture, AtomicBoolean hasChanges) {
        this.state = state;
        this.texture = texture;
        this.drawHelper = new DrawHelper(state, texture, hasChanges);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        Renderable.drawBorder(context, x-1, y-1, width+2, height+2, CrazyPainting.BLACK);
        context.enableScissor(x, y, right(), bottom());
        for(int i = 0; i < texture.pixels.length; i++) {
            int drawX = x + (i % texture.width) * zoom;
            int drawY = y + (i / texture.width) * zoom;
            context.fill(drawX + panX, drawY + panY, drawX + zoom + panX, drawY + zoom + panY, texture.pixels[i]);
        }

        int cursorX = (mouseX - (x + panX)) / zoom;
        int cursorY = (mouseY - (y + panY)) / zoom;
        boolean mouseInCanvas = (cursorX >= 0 && cursorY >= 0 && cursorX < texture.width && cursorY < texture.height);
        if (mouseInCanvas) {

            if (state.colorPickerActive) {
                int cursorDrawX = x + panX + cursorX * zoom;
                int cursorDrawY = y + panY + cursorY * zoom;
                int hoveredColor = texture.pixels[cursorX + cursorY*texture.width];
                Renderable.drawBorder(context, cursorDrawX, cursorDrawY, zoom, zoom, ColorHelper.contrast(hoveredColor));
            } else {
                // Brush pattern
                state.brushType.iteratePatternCentered(cursorX, cursorY, (px, py, ignored) -> {
                    if (texture.isPixelOOB(px, py)) return;

                    int cursorDrawX = x + panX + px * zoom;
                    int cursorDrawY = y + panY + py * zoom;
                    int hoveredColor = texture.pixels[px + py*texture.width];
                    Renderable.drawBorder(context, cursorDrawX, cursorDrawY, zoom, zoom, ColorHelper.contrast(hoveredColor));
                });

            }

            if (leftDown || rightDown) {
                if (state.colorPickerActive) {
                    if (leftJustDown) {
                        state.primaryColor = texture.pixels[cursorX + cursorY*texture.width];
                        CrazyPaintingClient.play(CrazySounds.COLOR_PICKER_USE, 1);
                    } else if (rightJustDown) {
                        state.secondaryColor = texture.pixels[cursorX + cursorY*texture.width];
                        CrazyPaintingClient.play(CrazySounds.COLOR_PICKER_USE, 1);
                    }

                } else {
                    drawHelper.useBrush(cursorX, cursorY, leftDown ? state.primaryColor : state.secondaryColor, state.opacity);
                }
            }
        }
        context.disableScissor();

        if (panStart) {
            panX = (int) (mouseX - panOffsetX);
            panY = (int) (mouseY - panOffsetY);
            context.blit(RenderPipelines.GUI_TEXTURED, EDITOR_TEXTURE_ID, mouseX - 8, mouseY - 8, 32, 32, 16, 16, 64, 64, 0xFFFFFFFF);
            GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().handle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
        } else if (containsPoint(mouseX, mouseY) && mouseInCanvas) {

            if (state.colorPickerActive) {
                context.blit(RenderPipelines.GUI_TEXTURED, EDITOR_TEXTURE_ID, mouseX, mouseY - 16, 16, 32, 16, 16, 64, 64, 0xFFFFFFFF);
                context.blit(RenderPipelines.GUI_TEXTURED, EDITOR_TEXTURE_ID, mouseX, mouseY - 16, 0, 32, 16, 16, 64, 64, state.primaryColor);
            } else {
                context.blit(RenderPipelines.GUI_TEXTURED, EDITOR_TEXTURE_ID, mouseX, mouseY, 16, 0, 16, 16, 64, 64, 0xFFFFFFFF);

                if (state.primaryColor == 0 || state.primaryColor == -1) {
                    context.blit(RenderPipelines.GUI_TEXTURED, EDITOR_TEXTURE_ID, mouseX, mouseY, 0, 16, 16, 16, 64, 64, CrazyPainting.BLACK);
                } else {
                    context.blit(RenderPipelines.GUI_TEXTURED, EDITOR_TEXTURE_ID, mouseX, mouseY, 0, 0, 16, 16, 64, 64, state.primaryColor);

                }
            }
            GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().handle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
        } else {
            GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().handle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        }

        leftJustDown = false;
        rightJustDown = false;
    }

    @Override
    public void calculateSize(int screenWidth, int screenHeight) {
        width = Math.min(texture.width * 10, screenWidth/2);
        height = Math.min(texture.height * 10, screenHeight - 64);
    }

    @Override
    public boolean onMousePressed(MouseButtonEvent mouseButtonEvent, boolean bl) {
        int button = mouseButtonEvent.button();
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        if (!containsPoint(mouseX, mouseY)) return false;

        if (button == 0) {
            leftDown = true;
            leftJustDown = true;
            drawHelper.beginStroke();
        } else if (button == 1) {
            rightDown = true;
            rightJustDown = true;
            drawHelper.beginStroke();
        } else if (button == 2) {
            panStart = true;
            panOffsetX = mouseX - panX;
            panOffsetY = mouseY - panY;
            return true;
        }

        return false;
    }

    @Override
    public boolean onMouseReleased(MouseButtonEvent mouseButtonEvent) {
        panStart = false;

        if (mouseButtonEvent.button() == 0) leftDown = false;
        if (mouseButtonEvent.button() == 1) rightDown = false;
        drawHelper.endStroke();

        return false;
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!containsPoint(mouseX, mouseY)) return false;
        if (verticalAmount == 0) return false;

        double worldMouseX = (mouseX - (x + panX)) / zoom;
        double worldMouseY = (mouseY - (y + panY)) / zoom;

        if (verticalAmount > 0) {
            zoom = Math.min(64, zoom * 2);
        } else {
            zoom = Math.max(1, zoom / 2);
        }

        panX = (int) ((mouseX - x) - worldMouseX * zoom);
        panY = (int) ((mouseY - y) - worldMouseY * zoom);

        return true;
    }

    @Override
    public boolean onCharTyped(CharacterEvent event) {
        if (!state.shortcutsEnabled) return false;

        if (state.colorPickerActive && event.codepoint() == 'b') {
            state.setColorPicker(false);
            return true;
        }

        if (!state.colorPickerActive && event.codepoint() == 'i') {
            state.setColorPicker(true);
            return true;
        }

        return false;
    }

    @Override
    public boolean onKeyPressed(KeyEvent event) {
        if (!state.shortcutsEnabled) return false;
        if (event.key() == GLFW.GLFW_KEY_Z && (event.modifiers() & GLFW.GLFW_MOD_CONTROL) == GLFW.GLFW_MOD_CONTROL) {
            if (drawHelper.undo()) {
                CrazyPaintingClient.play(CrazySounds.UNDO, 1);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onKeyReleased(KeyEvent event) {
        return false;
    }

    @Override
    public void tick() {
        drawHelper.tick();
    }
}
