package com.github.omoflop.crazypainting.client.screens.editor.types;

import net.minecraft.client.input.MouseButtonEvent;

public interface MouseListener {
    boolean onMousePressed(MouseButtonEvent mouseButtonEvent, boolean bl);

    boolean onMouseReleased(MouseButtonEvent mouseButtonEvent);

    boolean onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount);
}
