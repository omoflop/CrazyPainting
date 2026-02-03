package com.github.omoflop.crazypainting.recipe;

import com.github.omoflop.crazypainting.components.CanvasDataComponent;
import com.github.omoflop.crazypainting.content.CrazyComponents;
import com.github.omoflop.crazypainting.content.CrazyRecipes;
import com.github.omoflop.crazypainting.items.CanvasItem;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class CanvasCopyingRecipe extends CustomRecipe {
    public CanvasCopyingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level world) {
        return gatherMatchingCanvases(input.items()).isPresent();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Optional<Match> result = gatherMatchingCanvases(input.items());
        if (result.isEmpty()) return ItemStack.EMPTY; // This shouldn't happen
        Match match = result.get();

        ItemStack resultStack = new ItemStack(match.signedCanvas.getItem());

        CanvasDataComponent data = match.signedCanvas.get(CrazyComponents.CANVAS_DATA);
        assert data != null;

        resultStack.set(CrazyComponents.CANVAS_DATA, data.withGeneration((byte) (data.generation() + 1)));
        return resultStack;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> list = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < list.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (CanvasItem.isSigned(stack)) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                list.set(i, copy);
                break;
            }
        }

        return list;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return CrazyRecipes.CANVAS_COPYING;
    }

    private static Optional<Match> gatherMatchingCanvases(List<ItemStack> stacks) {
        if (stacks.size() != 2) return Optional.empty();

        ItemStack stackA = stacks.getFirst();
        ItemStack stackB = stacks.getLast();

        if (stackA.getItem() != stackB.getItem()) return Optional.empty();

        if (CanvasItem.isSigned(stackA) && CanvasItem.getCanvasId(stackB) == -1 && CanvasItem.getGeneration(stackA) < 2)
            return Optional.of(new Match(stackA, stackB));

        if (CanvasItem.isSigned(stackB) && CanvasItem.getCanvasId(stackA) == -1 && CanvasItem.getGeneration(stackB) < 2)
            return Optional.of(new Match(stackB, stackA));

        return Optional.empty();
    }

    private record Match(ItemStack signedCanvas, ItemStack emptyCanvas) {}
}
