package com.github.omoflop.crazypainting.client.screens;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.client.screens.editor.types.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public abstract class AbstractPaintingScreen extends Screen {
    private final List<EditorWidget> widgets = new ArrayList<>();
    private final List<Renderable> renderables = new ArrayList<>();
    private final List<Tickable> tickables = new ArrayList<>();
    private final List<MouseListener> mouseListeners = new ArrayList<>();
    private final List<KeyListener> keyListeners = new ArrayList<>();

    private int evilHack = 10;

    protected boolean drawDebugBoundaries = false;

    protected AbstractPaintingScreen(Component title) {
        super(title);
    }

    //============----------------============//
    //============ Helper Methods ============//
    //============----------------============//
    protected <T extends EditorWidget> void addWidget(T widget) {
        widgets.add(widget);
        if (widget instanceof Renderable renderable) renderables.add(renderable);
        if (widget instanceof Tickable tickable) tickables.add(tickable);
        if (widget instanceof MouseListener mouseListener) mouseListeners.add(mouseListener);
        if (widget instanceof KeyListener keyListener) keyListeners.add(keyListener);
    }

    protected void removeWidget(EditorWidget widget) {
        widgets.remove(widget);
        if (widget instanceof Renderable renderable) renderables.remove(renderable);
        if (widget instanceof Tickable tickable) tickables.remove(tickable);
        if (widget instanceof MouseListener mouseListener) mouseListeners.remove(mouseListener);
        if (widget instanceof KeyListener keyListener) keyListeners.remove(keyListener);
    }

    protected void recalculateWidgetPositions(int width, int height) {
        for (EditorWidget widget : widgets) {
            widget.calculateSize(width, height);
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        evilHack = 10;
    }

    //============-----------------============//
    //============ Lifetime events ============//
    //============-----------------============//
    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        if (evilHack > 0) {
            recalculateWidgetPositions(width, height);
            evilHack -= 1;
        }
        for (Renderable r : renderables) r.render(context, mouseX, mouseY, deltaTicks);

        super.render(context, mouseX, mouseY, deltaTicks);
        if (drawDebugBoundaries) {
            for (EditorWidget widget : widgets) {
                Renderable.drawBorder(context, widget.x, widget.y, widget.width, widget.height, CrazyPainting.YELLOW);
            }
        }

    }

    @Override
    public void tick() {
        tickables.forEach(Tickable::tick);
        super.tick();
    }

    //============-----------------============//
    //============ Keyboard Events ============//
    //============-----------------============//
    @Override
    public boolean keyPressed(KeyEvent event) {
        for (KeyListener w : keyListeners) {
            if (w.onKeyPressed(event)) {
                return true;
            }
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        for (KeyListener w : keyListeners) {
            if (w.onKeyReleased(event)) {
                return true;
            }
        }

        return super.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        for (KeyListener w : keyListeners) {
            if (w.onCharTyped(event)) {
                return true;
            }
        }

        return super.charTyped(event);
    }

    //============--------------============//
    //============ Mouse Events ============//
    //============--------------============//
    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        for (MouseListener w : mouseListeners) {
            if (w.onMousePressed(mouseButtonEvent, bl)) {
                return true;
            }
        }

        return super.mouseClicked(mouseButtonEvent, bl);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        for (MouseListener w : mouseListeners) {
            if (w.onMouseReleased(mouseButtonEvent)) {
                return true;
            }
        }

        return super.mouseReleased(mouseButtonEvent);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (MouseListener w : mouseListeners) {
            if (w.onMouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
