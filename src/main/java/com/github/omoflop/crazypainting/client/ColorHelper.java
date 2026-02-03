package com.github.omoflop.crazypainting.client;

import com.github.omoflop.crazypainting.CrazyPainting;
import java.awt.*;

public final class ColorHelper {
    public static int mix(int colorA, int colorB) {
        if (colorA == CrazyPainting.TRANSPARENT) return colorB;
        if (colorB == CrazyPainting.TRANSPARENT) return colorA;

        Color fg = new Color(colorA, true);
        Color bg = new Color(colorB, true);

        float fgA = fg.getAlpha() / 255f;
        float fgR = fg.getRed() / 255f;
        float fgG = fg.getGreen() / 255f;
        float fgB = fg.getBlue() / 255f;

        float bgA = bg.getAlpha() / 255f;
        float bgR = bg.getRed() / 255f;
        float bgG = bg.getGreen() / 255f;
        float bgB = bg.getBlue() / 255f;

        float a = 1 - (1 - fgA) * (1 - fgA);
        float r = bgR * bgA / a + fgR * fgA * (1 - bgA) / a;
        float g = bgG * bgA / a + fgG * fgA * (1 - bgA) / a;
        float b = bgB * bgA / a + fgB * fgA * (1 - bgA) / a;

        return create(r, g, b, a);
    }

    public static int setOpacity(int color, float opacity) {
        Color color2 = new Color(color, true);
        return create(color2.getRed(), color2.getGreen(), color2.getBlue(), (int) (opacity * 255));
    }


    public static int create(Color color) {
        return create(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static int create(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | (b);
    }

    public static int create(float r, float g, float b, float a) {
        return create((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255));
    }

    public static int contrast(int color) {
        Color col = new Color(color, true);

        double lum = (0.299 * col.getRed() + 0.587 * col.getGreen() + 0.114 * col.getBlue())/255;

        if (lum > 0.5) {
            return CrazyPainting.BLACK;
        } else {
            return CrazyPainting.WHITE;
        }
    }

    public static int antiContrast(int color) {
        Color col = new Color(color, true);

        double lum = (0.299 * col.getRed() + 0.587 * col.getGreen() + 0.114 * col.getBlue())/255;

        if (lum > 0.5) {
            return CrazyPainting.WHITE;
        } else {
            return CrazyPainting.BLACK;
        }
    }

    public static String hexString(int color) {
        Color col = new Color(color, true);
        StringBuilder sb = new StringBuilder("#");

        String r = Integer.toHexString(col.getRed());
        String g = Integer.toHexString(col.getGreen());
        String b = Integer.toHexString(col.getBlue());

        if (r.length() == 1) sb.append('0');
        sb.append(r);
        if (g.length() == 1) sb.append('0');
        sb.append(g);
        if (b.length() == 1) sb.append('0');
        sb.append(b);

        if (col.getAlpha() != 0xFF) {
            String a = Integer.toHexString(col.getBlue());

            if (a.length() == 1) sb.append('0');
            sb.append(a);
        }

        return sb.toString();
    }
}
