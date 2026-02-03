package com.github.omoflop.crazypainting.client.texture;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.items.CanvasItem;
import com.github.omoflop.crazypainting.mixin.client.TextureManagerAccessor;
import com.github.omoflop.crazypainting.network.types.PaintingData;
import com.github.omoflop.crazypainting.network.types.PaintingId;
import com.github.omoflop.crazypainting.network.types.PaintingSize;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class CanvasTexture implements AutoCloseable {
    // Only set when the canvas is editable
    public int[] pixels;

    // Dimensions of the image
    public final int width;
    public final int height;

    // Image data uploaded to the GPU for rendering
    public final NativeImage nativeImage;
    public final DynamicTexture glTexture;

    // Identifier used for lookups or rendering or something
    public final Identifier textureId;

    // Size and id for networking
    private final PaintingId id;
    private final PaintingSize size;

    private boolean ready = false;
    private boolean editable;
    private boolean registered = false;
    private boolean isClosed = false;


    public CanvasTexture(CanvasItem base, int id) {
        this(base.width, base.height, id, true);
    }

    public CanvasTexture(byte canvasWidth, byte canvasHeight, int id, boolean editable) {
        textureId = CrazyPainting.id("canvas/" + id);

        this.id = new PaintingId(id);
        size = new PaintingSize(canvasWidth, canvasHeight);

        width = (canvasWidth * 16);
        height = (canvasHeight * 16);
        if (editable) {
            pixels = new int[width * height];
            Arrays.fill(pixels, CrazyPainting.WHITE);
        }

        if (width == 0 || height == 0) {
            throw new RuntimeException("Image cannot be width or height of 0!");
        }

        this.editable = editable;
        nativeImage = new NativeImage(width, height, true);
        glTexture = new DynamicTexture(textureId::toString, nativeImage);
    }

    public boolean isPixelOOB(int x, int y) {
        return x < 0 || x >= width || y < 0 || y >= height;
    }

    public boolean isReady() {
        return ready;
    }

    public void markUneditable() {
        editable = false;
        pixels = null;
    }

    public void updateTexture() {
        updateTexture(pixels);
    }

    public void updateTexture(int[] newPixels) {
        if (isClosed) return;

        // If the data is invalid then just ignore it (failsafe)
        if (width * height != newPixels.length) {
            System.out.println("Received invalid painting texture data");
            return;
        }

        if (!registered) {
            Minecraft.getInstance().getTextureManager().register(textureId, glTexture);
            registered = true;
        }

        // Mark as unready while editing the texture because threads or something IDK
        // Set back to true once this.upload is called
        ready = false;

        // If it's editable, save the new pixel data for editing on this client
        if (editable) pixels = newPixels;

        // Write new received texture data to the GPU :)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = x + y * width;
                nativeImage.setPixel(x, y, newPixels[i]);
            }
        }

        upload();
    }

    private void upload() {
        glTexture.upload();

        // Mark as ready to be rendered once upload is called
        ready = true;
    }

    @Override
    public void close() {
        if (isClosed) return;
        glTexture.close(); // Closes nativeImage automatically

        if (registered) {
            ((TextureManagerAccessor)(Minecraft.getInstance().getTextureManager())).textures().remove(textureId);
            registered = false;
        }

        isClosed = true;
    }

    public @Nullable PaintingData toData() {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, width, height, pixels, 0, width);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(img, "qoi", out);
            return new PaintingData(out.toByteArray(), size, id);

        } catch (IOException e) {
            return null;
        }
    }
}
