package com.github.omoflop.crazypainting.client.screens.editor.widgets;

import com.github.omoflop.crazypainting.client.screens.editor.NetSync;
import com.github.omoflop.crazypainting.client.screens.editor.types.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class SignWidget extends EditorWidget implements KeyListener, MouseListener, Tickable, Renderable {
    private static final Component SIGN_TEXT = Component.translatable("gui.crazypainting.painting_editor.sign");

    private final EditBox textField;
    private final Button button;
    private final EditorState state;

    public SignWidget(EditorState state, NetSync netSync) {
        this.state = state;
        this.textField = new EditBox(Minecraft.getInstance().font, 32, 32, Component.empty());
        this.button = Button.builder(SIGN_TEXT, (ignored) -> netSync.signAndClose(textField.getValue())).bounds(0, 0, 32, 32).build();
        button.active = false;

        textField.setMaxLength(32);
        textField.setVisible(true);
        textField.setTextColor(-1);
        textField.setValue("Untitled Painting");
        textField.moveCursorToStart(false);
    }

    @Override
    public void calculateSize(int screenWidth, int screenHeight) {
        super.calculateSize(screenWidth, screenHeight);
        textField.setX(x);
        textField.setWidth(width);
        textField.setY(y);
        textField.setHeight(height / 2);

        button.setX(x);
        button.setWidth(width);
        button.setY(y + height/2);
        button.setHeight(height/2);
    }

    @Override
    public boolean onCharTyped(CharacterEvent event) {
        return textField.isFocused() && textField.charTyped(event);
    }

    @Override
    public boolean onKeyPressed(KeyEvent event) {
        return textField.isFocused() && textField.keyPressed(event);
    }

    @Override
    public boolean onKeyReleased(KeyEvent event) {
        return textField.isFocused() && textField.keyReleased(event);
    }

    @Override
    public boolean onMousePressed(MouseButtonEvent event, boolean bl) {
        if (textField.mouseClicked(event, bl)) {
            textField.setFocused(true);
            state.shortcutsEnabled = false;
            return true;
        } else {
            state.shortcutsEnabled = true;
            textField.setFocused(false);
        }
        if (this.button.mouseClicked(event, bl)) return true;

        return false;
    }

    @Override
    public boolean onMouseReleased(MouseButtonEvent event) {
        if (textField.mouseReleased(event)) return true;
        return this.button.mouseReleased(event);
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return false;
    }

    @Override
    public void tick() {
        button.active = !textField.getValue().equals("Untitled Painting");
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        button.render(context, mouseX, mouseY, deltaTicks);
        textField.render(context, mouseX, mouseY, deltaTicks);
    }
}
