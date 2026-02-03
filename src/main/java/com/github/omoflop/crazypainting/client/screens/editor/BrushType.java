package com.github.omoflop.crazypainting.client.screens.editor;

import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public final class BrushType {
    private static final HashMap<String, ArrayList<BrushType>> BRUSH_CATEGORIES = new HashMap<>();

    public final String name;
    private final boolean[][] pattern;

    private BrushType(String name, boolean[][] pattern) {
        this.name = name;
        this.pattern = pattern;
    }

    public void iteratePattern(int x, int y, PatternConsumer consumer) {
        for (int xx = 0; xx < getWidth(); xx++) {
            for (int yy = 0; yy < getHeight(); yy++) {
                if (pattern[xx][yy]) {
                    consumer.receive(xx + x, yy + y, 1.0f);
                }
            }
        }
    }

    public void iteratePatternCentered(int x, int y, PatternConsumer consumer) {
        for (int xx = 0; xx < getWidth(); xx++) {
            for (int yy = 0; yy < getHeight(); yy++) {
                if (pattern[xx][yy]) {
                    consumer.receive(xx + x - getWidth()/2, yy + y - getHeight()/2, 1.0f);
                }
            }
        }
    }

    public int getWidth() {
        return pattern.length;
    }

    public int getHeight() {
        return pattern[0].length;
    }

    public interface PatternConsumer {
        void receive(int x, int y, float opacity);
    }

    public static void register(String category, String name, BufferedImage img) {
        boolean[][] pattern = new boolean[img.getWidth()][img.getHeight()];
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                pattern[x][y] = img.getRGB(x, y) != 0x00000000;
            }
        }

        if (!BRUSH_CATEGORIES.containsKey(category))
            BRUSH_CATEGORIES.put(category, new ArrayList<>());

        BRUSH_CATEGORIES.get(category).add(new BrushType(name, pattern));
    }

    public static void clear() {
        BRUSH_CATEGORIES.clear();
    }

    public static Collection<String> getCategories() {
        return BRUSH_CATEGORIES.keySet();
    }

    public static Collection<BrushType> getBrushes(String category) {
        return BRUSH_CATEGORIES.get(category);
    }

    public static @Nullable BrushType getBrush(String category, String name) {
        for (BrushType brushType : BRUSH_CATEGORIES.get(category)) {
            if (brushType.name.equals(name)) {
                return brushType;
            }
        }

        return null;
    }
}
