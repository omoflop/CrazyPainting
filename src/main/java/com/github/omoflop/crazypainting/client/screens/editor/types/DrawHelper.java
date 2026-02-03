package com.github.omoflop.crazypainting.client.screens.editor.types;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.client.ColorHelper;
import com.github.omoflop.crazypainting.client.CrazyPaintingClient;
import com.github.omoflop.crazypainting.client.texture.CanvasTexture;
import com.github.omoflop.crazypainting.content.CrazySounds;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.util.RandomSource;

public class DrawHelper {
    private final Stack<UndoEntry> undoHistory = new Stack<>();
    private final EditorState state;
    private final CanvasTexture canvasTexture;
    private final AtomicBoolean hasChanges;

    private Int2IntMap currentUndo;
    private int soundCooldown;

    public DrawHelper(EditorState state, CanvasTexture canvasTexture, AtomicBoolean hasChanges) {
        this.state = state;
        this.canvasTexture = canvasTexture;
        this.hasChanges = hasChanges;
    }

    public void useBrush(int x, int y, int baseColor, float opacity) {
        if (state.brushType == null) return;

        if (baseColor == -1) return;
        final int color = baseColor == CrazyPainting.TRANSPARENT ? baseColor : ColorHelper.setOpacity(baseColor, opacity);

        state.brushType.iteratePatternCentered(x, y, (px, py, alpha) -> {
            if (canvasTexture.isPixelOOB(px, py)) return;
            int i = px + py * canvasTexture.width;
            int drawColor = color == CrazyPainting.TRANSPARENT ? color : ColorHelper.mix(canvasTexture.pixels[i], color);

            if (currentUndo == null || !currentUndo.containsKey(i)) {
                if (currentUndo != null)
                    currentUndo.put(i, canvasTexture.pixels[i]);

                canvasTexture.pixels[i] = drawColor;
                hasChanges.set(true);
                if (soundCooldown <= 0) {
                    CrazyPaintingClient.play(CrazySounds.BRUSH_USE, 0.3f, RandomSource.create().nextIntBetweenInclusive(8, 12)/10f);
                    soundCooldown = RandomSource.create().nextIntBetweenInclusive(6, 9);
                }

            }



        });

    }

    public void beginStroke() {
        if (state.colorPickerActive) return;
        currentUndo = new Int2IntArrayMap();
    }

    public void endStroke() {
        if (currentUndo == null || currentUndo.isEmpty()) return;

        undoHistory.push(new UndoEntry(currentUndo));
        currentUndo = null;
    }

    public boolean undo() {
        if (undoHistory.isEmpty()) return false;
        UndoEntry undo = undoHistory.pop();
        for (int i : undo.oldColors.keySet()) {
            canvasTexture.pixels[i] = undo.oldColors.get(i);
        }
        hasChanges.set(true);
        return true;
    }

    public void tick() {
        if (soundCooldown > 0) soundCooldown -= 1;
    }

    record UndoEntry(Int2IntMap oldColors) { }
}
