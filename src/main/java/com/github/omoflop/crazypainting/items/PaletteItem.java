package com.github.omoflop.crazypainting.items;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.Identifiable;
import com.github.omoflop.crazypainting.content.CrazyComponents;
import com.github.omoflop.crazypainting.content.CrazyItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"OptionalGetWithoutIsPresent", "NullPointerException"})
public class PaletteItem extends Item implements Identifiable {
    public final Identifier id;

    public PaletteItem(String registryName) {
        super(new Settings().maxCount(1).registryKey(Identifiable.key(registryName)));
        this.id = CrazyPainting.id(registryName);
    }

    @Override
    public Identifier getId() {
        return id;
    }

    public static void addColor(ItemStack stack, int color) {
        if (!stack.hasChangedComponent(CrazyComponents.PALETTE_COLORS)) return;
        var component = stack.getComponentChanges().get(CrazyComponents.PALETTE_COLORS);

        List<Integer> colors = Objects.requireNonNull(component).get().colors();
        if (!colors.contains(color)) {
            colors.add(color);
        }
    }

    public static void removeColor(ItemStack stack, int color) {
        if (!stack.hasChangedComponent(CrazyComponents.PALETTE_COLORS)) return;
        var component = stack.getComponentChanges().get(CrazyComponents.PALETTE_COLORS);

        List<Integer> colors = Objects.requireNonNull(component).get().colors();
        if (colors.contains(color)) {
            colors.remove(color);
        }
    }

    public static void addColors(ItemStack stack, Iterable<Integer> iterable) {
        if (!stack.hasChangedComponent(CrazyComponents.PALETTE_COLORS)) return;
        var component = stack.getComponentChanges().get(CrazyComponents.PALETTE_COLORS);

        List<Integer> colors = Objects.requireNonNull(component).get().colors();
        for (Integer i : iterable) {
            if (!colors.contains(i)) {
                colors.add(i);
            }
        }
    }

    public static ItemStack createFullPalette() {
        ItemStack stack = new ItemStack(CrazyItems.PALETTE_ITEM);
        ItemStack outStack = stack.copy();
        addColors(outStack, CrazyPainting.VANILLA_COLORS.values());

        return stack;
    }

    public static boolean hasColor(ItemStack stack, int color) {
        if (!stack.hasChangedComponent(CrazyComponents.PALETTE_COLORS)) return false;
        var component = stack.getComponentChanges().get(CrazyComponents.PALETTE_COLORS);

        List<Integer> colors = Objects.requireNonNull(component).get().colors();
        return colors.contains(color);
    }

    public static boolean hasAnyColor(ItemStack stack, Iterable<Integer> iterable) {
        if (!stack.hasChangedComponent(CrazyComponents.PALETTE_COLORS)) return false;
        var component = stack.getComponentChanges().get(CrazyComponents.PALETTE_COLORS);

        List<Integer> colors = Objects.requireNonNull(component).get().colors();
        for (Integer i : iterable) {
            if (colors.contains(i)) return true;
        }
        return false;
    }

    public static Collection<Integer> getColors(ItemStack stack) {
        if (!stack.hasChangedComponent(CrazyComponents.PALETTE_COLORS)) return List.of();
        var component = stack.getComponentChanges().get(CrazyComponents.PALETTE_COLORS);

        return List.copyOf(Objects.requireNonNull(component).get().colors());
    }
}