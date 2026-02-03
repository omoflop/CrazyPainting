package com.github.omoflop.crazypainting.client.datagen;

import com.github.omoflop.crazypainting.content.CrazyItems;
import com.github.omoflop.crazypainting.items.CanvasItem;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class CrazyRecipeProvider extends FabricRecipeProvider {
    public CrazyRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider wrapperLookup, RecipeOutput exporter) {
        return new RecipeProvider(wrapperLookup, exporter) {
            @Override
            public void buildRecipes() {
                shapeless(RecipeCategory.TOOLS, CrazyItems.PALETTE_ITEM)
                        .requires(ItemTags.WOODEN_SLABS)
                        .unlockedBy(getHasName(Items.OAK_SLAB), has(ItemTags.WOODEN_SLABS))
                        .save(output);

                shaped(RecipeCategory.DECORATIONS, CrazyItems.SMALL_CANVAS_ITEM)
                        .pattern("///")
                        .pattern("/p/")
                        .pattern("///")
                        .define('p', Items.PAPER)
                        .define('/', Items.STICK)
                        .unlockedBy(getHasName(CrazyItems.PALETTE_ITEM), has(CrazyItems.PALETTE_ITEM))
                        .save(output);

                shaped(RecipeCategory.DECORATIONS, CrazyItems.EASEL_ITEM)
                        .pattern(" / ")
                        .pattern(" / ")
                        .pattern("/_/")
                        .define('_', Items.SMOOTH_STONE_SLAB)
                        .define('/', Items.STICK)
                        .unlockedBy(getHasName(Items.STICK), has(Items.STICK))
                        .save(output);

                for (CanvasItem canvas : CrazyItems.allCanvases) {
                    if (canvas == CrazyItems.SMALL_CANVAS_ITEM) continue;
                    offerCanvas(canvas, output);
                }

            }

            public void offerCanvas(CanvasItem targetCanvas, RecipeOutput exporter) {
                byte targetWidth = targetCanvas.width;
                byte targetHeight = targetCanvas.height;

                // Generate recipe from 1x1 base canvases
                String[] pattern = new String[targetHeight];
                Arrays.fill(pattern, "@".repeat(Math.max(0, targetWidth)));

                // Create the base recipe from 1x1 canvases
                ShapedRecipeBuilder builder = shaped(RecipeCategory.DECORATIONS, targetCanvas);
                for (String patternRow : pattern) {
                    builder.pattern(patternRow);
                }
                builder.define('@', CrazyItems.SMALL_CANVAS_ITEM)
                        .unlockedBy(getHasName(CrazyItems.PALETTE_ITEM), has(CrazyItems.PALETTE_ITEM))
                        .group(getCanvasName(targetCanvas))
                        .save(exporter, getCanvasName(targetCanvas) + "_from_small_canvas");

                // Generate recipes from other canvas combinations
                for (CanvasItem sourceCanvas : CrazyItems.allCanvases) {
                    // Skip if it's the same canvas or the base 1x1 canvas
                    if (sourceCanvas == targetCanvas || sourceCanvas == CrazyItems.SMALL_CANVAS_ITEM) {
                        continue;
                    }

                    byte sourceWidth = sourceCanvas.width;
                    byte sourceHeight = sourceCanvas.height;

                    // Check horizontal arrangement (source canvases side by side)
                    if (targetHeight == sourceHeight && targetWidth % sourceWidth == 0) {
                        int canvasesNeeded = targetWidth / sourceWidth;
                        if (canvasesNeeded <= 3 && canvasesNeeded > 1) { // Crafting grid limit and must use more than 1

                            shaped(RecipeCategory.DECORATIONS, targetCanvas)
                                    .pattern("@".repeat(canvasesNeeded))
                                    .define('@', sourceCanvas)
                                    .unlockedBy(getHasName(CrazyItems.PALETTE_ITEM), has(CrazyItems.PALETTE_ITEM))
                                    .group(getCanvasName(targetCanvas))
                                    .save(exporter, getCanvasName(targetCanvas) + "_from_" + getCanvasName(sourceCanvas) + "_horizontal");
                        }
                    }

                    // Check vertical arrangement (source canvases stacked)
                    if (targetWidth == sourceWidth && targetHeight % sourceHeight == 0) {
                        int canvasesNeeded = targetHeight / sourceHeight;
                        if (canvasesNeeded <= 3 && canvasesNeeded > 1) { // Crafting grid limit and must use more than 1
                            ShapedRecipeBuilder verticalBuilder = shaped(RecipeCategory.DECORATIONS, targetCanvas);

                            for (int i = 0; i < canvasesNeeded; i++) {
                                verticalBuilder.pattern("@");
                            }

                            verticalBuilder.define('@', sourceCanvas)
                                    .unlockedBy(getHasName(CrazyItems.PALETTE_ITEM), has(CrazyItems.PALETTE_ITEM))
                                    .group(getCanvasName(targetCanvas))
                                    .save(exporter, getCanvasName(targetCanvas) + "_from_" + getCanvasName(sourceCanvas) + "_vertical");
                        }
                    }

                    // Check 2D grid arrangement
                    if (targetWidth % sourceWidth == 0 && targetHeight % sourceHeight == 0) {
                        int horizontalCount = targetWidth / sourceWidth;
                        int verticalCount = targetHeight / sourceHeight;
                        int totalCanvases = horizontalCount * verticalCount;

                        // Make sure it fits in crafting grid and uses more than 1 canvas
                        // Also skip if it's the same as horizontal or vertical arrangements we already handled
                        if (horizontalCount <= 3 && verticalCount <= 3 && totalCanvases > 1 && totalCanvases <= 9
                                && !(verticalCount == 1 || horizontalCount == 1)) { // Skip 1D arrangements as they're handled above

                            StringBuilder gridRow = new StringBuilder();
                            gridRow.append("@".repeat(Math.max(0, horizontalCount)));

                            ShapedRecipeBuilder gridBuilder = shaped(RecipeCategory.DECORATIONS, targetCanvas);
                            for (int i = 0; i < verticalCount; i++) {
                                gridBuilder.pattern(gridRow.toString());
                            }

                            gridBuilder.define('@', sourceCanvas)
                                    .unlockedBy(getHasName(CrazyItems.PALETTE_ITEM), has(CrazyItems.PALETTE_ITEM))
                                    .group(getCanvasName(targetCanvas))
                                    .save(exporter,  getCanvasName(targetCanvas) + "_from_" + getCanvasName(sourceCanvas) + "_grid");
                        }
                    }
                }
            }

            // Helper method to get a clean name for the canvas
            private String getCanvasName(CanvasItem canvas) {
                // Extract a clean name from the canvas item
                // This assumes your canvas items have predictable names or you can modify this logic
                String name = canvas.getId().getPath().toLowerCase();
                // Remove common prefixes/suffixes and clean up
                //name = name.replace("canvas", "").replace("item", "").replace("_", "");
                // You might want to customize this based on your actual canvas naming
                if (name.isEmpty()) {
                    return canvas.width + "x" + canvas.height;
                }
                return name;
            }
        };
    }

    @Override
    public String getName() {
        return "CrazyPaintingRecipeProvider";
    }
}
