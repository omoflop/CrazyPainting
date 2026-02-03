package com.github.omoflop.crazypainting.client.resources;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.client.screens.editor.BrushType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Optional;

public class BrushReloadListener implements SimpleSynchronousResourceReloadListener {
    private static final Identifier BRUSHES_JSON = CrazyPainting.id("brushes/brushes.json");

    @Override
    public Identifier getFabricId() {
        return CrazyPainting.id("brush_reload_listener");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        BrushType.clear();

        int categoryCount = 0;
        int brushTypeCount = 0;
        for (Resource resource : manager.getResourceStack(BRUSHES_JSON)) {
            try {
                JsonArray categories = JsonParser.parseReader(resource.openAsReader()).getAsJsonArray();

                for (JsonElement element : categories) {
                    JsonObject category = element.getAsJsonObject();
                    String categoryName = category.get("category").getAsString();
                    JsonArray brushes = category.getAsJsonArray("brushes");
                    categoryCount += 1;

                    for (JsonElement brush : brushes) {
                        String brushName = brush.getAsString();
                        Identifier path = CrazyPainting.id(String.format("brushes/%s/%s.qoi", categoryName, brushName));
                        Optional<Resource> brushResource = manager.getResource(path);
                        if (brushResource.isEmpty()) {
                            CrazyPainting.LOGGER.error("Invalid brush defined in {}, no brush exists at path {}", BRUSHES_JSON.toString(), path);
                            continue;
                        }

                        BufferedImage brushImg = ImageIO.read(brushResource.get().open());
                        BrushType.register(categoryName, brushName, brushImg);
                        brushTypeCount += 1;
                    }

                }

            } catch (Exception e) {
                CrazyPainting.LOGGER.error("Failed to parse {}, error: {}", BRUSHES_JSON.toString(), e);
            }
        }

        CrazyPainting.LOGGER.info("Loaded brush {} categories, and {} brushes", categoryCount, brushTypeCount);


    }
}
