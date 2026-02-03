package com.github.omoflop.crazypainting.recipe;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.components.CanvasDataComponent;
import com.github.omoflop.crazypainting.components.PaletteColorsComponent;
import com.github.omoflop.crazypainting.content.CrazyComponents;
import com.github.omoflop.crazypainting.content.CrazyRecipes;
import com.github.omoflop.crazypainting.items.CanvasItem;
import com.github.omoflop.crazypainting.items.PaletteItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class CanvasShieldApplyingRecipe extends CustomRecipe {
    public CanvasShieldApplyingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level world) {
        return findMatch(input).isPresent();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Match match = findMatch(input).orElseThrow();
        CanvasDataComponent canvasData = match.canvas.get(CrazyComponents.CANVAS_DATA);

        ItemStack result = match.shield.copy();
        result.set(CrazyComponents.CANVAS_DATA, canvasData);
        ItemLore lore = result.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
        ItemLore newLore = lore.withLineAdded(Component.literal(canvasData.title()).withStyle(ChatFormatting.YELLOW));
        result.set(DataComponents.LORE, newLore);

        return result;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return CrazyRecipes.CANVAS_SHIELD_APPLYING;
    }

    // Requires a signed canvas and a shield with no banner applied
    private Optional<Match> findMatch(CraftingInput input) {
        if (input.ingredientCount() != 2) return Optional.empty();

        List<ItemStack> stacks = input.items();
        ItemStack shieldStack = null;
        ItemStack canvasStack = null;

        for (ItemStack stack : stacks) {
            if (stack.is(Items.SHIELD)) {
                if (shieldStack != null) return Optional.empty();
                shieldStack = stack;
            }
            if (stack.getItem() instanceof CanvasItem canvas && canvas.width == 1 && canvas.height == 2 && CanvasItem.isSigned(stack)) {
                if (canvasStack != null) return Optional.empty();
                canvasStack = stack;
            }
        }

        if (shieldStack == null || canvasStack == null) return Optional.empty();
        return Optional.of(new Match(shieldStack, canvasStack));
    }

    record Match(ItemStack shield, ItemStack canvas) {}
}
