package com.github.omoflop.crazypainting.client.screens.editor.types;

import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;


public interface KeyListener {
    boolean onCharTyped(CharacterEvent event);
    boolean onKeyPressed(KeyEvent event);
    boolean onKeyReleased(KeyEvent event);
}
