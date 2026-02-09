package com.github.omoflop.crazypainting.client.screens.editor.types;

import net.minecraft.client.gui.GuiGraphics;

public interface Renderable {
    void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks);

    static void drawBorder(GuiGraphics context, int x, int y, int w, int h, int borderColor) {
        context.renderOutline(x, y, w, h, borderColor);
    }
}
