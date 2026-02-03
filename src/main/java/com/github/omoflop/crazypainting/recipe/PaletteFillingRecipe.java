package com.github.omoflop.crazypainting.recipe;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.components.PaletteColorsComponent;
import com.github.omoflop.crazypainting.content.CrazyComponents;
import com.github.omoflop.crazypainting.content.CrazyRecipes;
import com.github.omoflop.crazypainting.items.PaletteItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class PaletteFillingRecipe extends CustomRecipe {
    public PaletteFillingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level world) {
        List<ItemStack> stacks = input.items();

        // Ensure there is a palette
        ItemStack paletteStack = tryGetPalette(stacks);
        if (paletteStack == null) return false;

        // Ensure there is at least one color
        List<Integer> colors = gatherColors(stacks);
        if (colors.isEmpty()) return false;

        // Finally, return true if the palette has NONE of the colors that are in the crafting grid
        return !PaletteItem.hasAnyColor(paletteStack, colors);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        List<ItemStack> stacks = input.items();

        // Find the palette item
        ItemStack paletteStack = tryGetPalette(stacks);
        assert paletteStack != null;

        // Create a new palette item for the result, copying over the current colors component
        PaletteItem item = (PaletteItem) paletteStack.getItem();

        // Append the previous colors stored on the palette to the list of new colors in this recipe
        ArrayList<Integer> colorPalette = new ArrayList<>();
        colorPalette.addAll(PaletteItem.getColors(paletteStack));
        colorPalette.addAll(gatherColors(stacks));

        PaletteColorsComponent.sort(colorPalette);

        ItemStack resultStack = new ItemStack(item);
        resultStack.set(CrazyComponents.PALETTE_COLORS, new PaletteColorsComponent(colorPalette));

        return resultStack;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return CrazyRecipes.PALETTE_FILLING;
    }

    private static ArrayList<Integer> gatherColors(List<ItemStack> stacks) {
        ArrayList<Integer> colors = new ArrayList<>();

        for (ItemStack stack : stacks) {
            Item item = stack.getItem();
            if (CrazyPainting.VANILLA_COLORS.containsKey(item)) {
                colors.add(CrazyPainting.VANILLA_COLORS.get(item));
            }
        }

        return colors;
    }

    private static @Nullable ItemStack tryGetPalette(List<ItemStack> stacks) {
        ItemStack paletteStack = null;
        for (ItemStack stack : stacks) {
            Item item = stack.getItem();

            // Store the first palette found,
            // return null if there's a non-palette and non-dye item,
            // otherwise, continue
            if (paletteStack == null && item instanceof PaletteItem) {
                paletteStack = stack;
            } else if (!CrazyPainting.VANILLA_COLORS.containsKey(item) && !stack.isEmpty()) {
                return null;
            }
        }

        return paletteStack;
    }
}
