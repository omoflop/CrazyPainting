package com.github.omoflop.crazypainting.client.screens.editor.types;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface Renderable {
    void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks);

    static void drawBorder(GuiGraphicsExtractor context, int x, int y, int w, int h, int borderColor) {
        context.outline(x, y, w, h, borderColor);
    }
}
