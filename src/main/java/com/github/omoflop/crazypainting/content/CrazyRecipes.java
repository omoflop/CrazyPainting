package com.github.omoflop.crazypainting.content;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.recipe.CanvasCopyingRecipe;
import com.github.omoflop.crazypainting.recipe.CanvasShieldApplyingRecipe;
import com.github.omoflop.crazypainting.recipe.PaletteFillingRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CrazyRecipes {
    public static final RecipeSerializer<PaletteFillingRecipe> PALETTE_FILLING = new RecipeSerializer<>(PaletteFillingRecipe.MAP_CODEC, PaletteFillingRecipe.STREAM_CODEC);
    public static final RecipeSerializer<CanvasCopyingRecipe> CANVAS_COPYING = new RecipeSerializer<>(CanvasCopyingRecipe.MAP_CODEC, CanvasCopyingRecipe.STREAM_CODEC);
    public static final RecipeSerializer<CanvasShieldApplyingRecipe> CANVAS_SHIELD_APPLYING = new RecipeSerializer<>(CanvasShieldApplyingRecipe.MAP_CODEC, CanvasShieldApplyingRecipe.STREAM_CODEC);

    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, CrazyPainting.id("palette_filling"), PALETTE_FILLING);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, CrazyPainting.id("canvas_copying"), CANVAS_COPYING);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, CrazyPainting.id("canvas_shield_applying"), CANVAS_SHIELD_APPLYING);
    }
}
