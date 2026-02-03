package com.github.omoflop.crazypainting.client.screens;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.client.ColorHelper;
import com.github.omoflop.crazypainting.network.types.PaintingData;
import com.github.omoflop.crazypainting.network.types.PaintingSize;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PaintingViewerScreen extends Screen {
    private final int[] pixels;
    private final PaintingSize size;
    private final int pixelSize;
    private final String title;

    public PaintingViewerScreen(PaintingData data, String title) {
        super(Component.translatable("gui.crazypainting.painting_viewer.title"));
        this.title = title;
        try {
            size = data.size();
            pixelSize = (int)Math.max(1, 10.0f / (Math.max(size.width(), size.height())));


            pixels = data.readPixelArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        int widthPixels = size.width() * 16;
        int heightPixels = size.height() * 16;

        int canvasPixelX = (int)((this.width / 2f - widthPixels*pixelSize/2f) / pixelSize);
        int canvasPixelY = (int)((this.height / 2f - heightPixels*pixelSize/2f) / pixelSize);

        int canvasX = canvasPixelX * pixelSize;
        int canvasY = canvasPixelY * pixelSize;

        // Draw title
        context.drawCenteredString(font, Component.literal(title), width / 2, 64, CrazyPainting.YELLOW);

        // Draw pixels
        for(int i = 0; i < pixels.length; i++) {
            int drawX = canvasX + (i % widthPixels) * pixelSize;
            int drawY = canvasY + (i / widthPixels) * pixelSize;
            context.fill(drawX, drawY, drawX + pixelSize, drawY + pixelSize, pixels[i]);
        }
    }
}
