package com.github.omoflop.crazypainting.client.screens.editor.widgets;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.client.screens.PaintingEditorScreen;
import com.github.omoflop.crazypainting.client.screens.editor.types.EditorWidget;
import com.github.omoflop.crazypainting.client.screens.editor.types.Renderable;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class HelpWidget extends EditorWidget implements Renderable {
    private final Font textRenderer;

    public HelpWidget() {
        this.textRenderer = Minecraft.getInstance().font;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(x + 1, y + 1, x + width - 2, y + height - 2, CrazyPainting.WHITE);
        Renderable.drawBorder(context, x - 1, y - 1, width + 1, height + 1, CrazyPainting.BLACK);

        context.blit(RenderPipelines.GUI_TEXTURED, PaintingEditorScreen.EDITOR_TEXTURE_ID, x + (16 - width)/2, y + (16 - height)/2, 32, 16, 16, 16, 64, 64, 0xFFFFFFFF);

        if (mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height) {
            int lineCount = Integer.parseInt(Component.translatable("gui.crazypainting.painting_editor.tutorial.count").getString());
            int width = 0;
            List<Component> lines = new ArrayList<>();
            for (int i = 1; i <= lineCount; i++) {
                Component curLine = Component.translatable("gui.crazypainting.painting_editor.tutorial.%s".formatted(i));
                if (curLine.getString().endsWith(":")) {
                    if (i > 1) {
                        lines.add(Component.literal(""));
                    }
                    curLine = curLine.copy().withColor(CrazyPainting.YELLOW);
                }
                width = Math.max(width, textRenderer.width(curLine));
                lines.add(curLine);
            }

            int height = textRenderer.lineHeight * lines.size();
            TooltipRenderUtil.renderTooltipBackground(context, mouseX+8, mouseY+8, width, height, null);

            int i = 0;
            for (Component line : lines) {
                context.drawString(textRenderer, line, mouseX + 8, mouseY + i + 8, 0xFFFFFFFF, false);
                i += textRenderer.lineHeight;
            }
        }
    }
}
