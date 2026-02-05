package com.github.omoflop.crazypainting.components;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.content.CrazyComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record PaletteColorsComponent(List<Integer> colors) implements TooltipProvider {

    public static final Codec<PaletteColorsComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.list(Codec.INT).fieldOf("colors").forGetter(PaletteColorsComponent::colors)
    ).apply(builder, PaletteColorsComponent::new));

    public static PaletteColorsComponent empty() {
        return new PaletteColorsComponent(new ArrayList<>());
    }

    public static PaletteColorsComponent filled() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i : CrazyPainting.VANILLA_COLOR_ORDER) {
            list.add(i);
        }
        return new PaletteColorsComponent(list);
    }

    public static void sort(ArrayList<Integer> palette) {
        List<Integer> colors = Arrays.stream(CrazyPainting.VANILLA_COLOR_ORDER).boxed().toList();
        palette.sort((a, b) -> {
            int aIndex = 10000;
            int bIndex = 10000;
            if (colors.contains(a)) {
                aIndex = colors.indexOf(a);
                bIndex = colors.indexOf(b);
            }
            return aIndex - bIndex;
        });
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> textConsumer, TooltipFlag type, DataComponentGetter components) {
        PaletteColorsComponent data = components.get(CrazyComponents.PALETTE_COLORS);
        assert data != null;


        if (data.colors == null || data.colors.isEmpty()) {
            textConsumer.accept(Component.translatable("item.crazypainting.palette.tooltip.empty").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            return;
        }

        MutableComponent text = (Component.translatable("item.crazypainting.palette.tooltip.colors").append(": ").withStyle(ChatFormatting.BLUE));
        for (int i = 0; i < data.colors.size(); i++) {
            int color = data.colors.get(i);
            if (color == CrazyPainting.TRANSPARENT) {
                text.append(Component.translatable("item.crazypainting.palette.tooltip.color_entry.transparent").withStyle(ChatFormatting.WHITE));
            } else {
                text.append(Component.translatable("item.crazypainting.palette.tooltip.color_entry").withColor(data.colors.get(i)));

            }
        }
        textConsumer.accept(text);
    }
}
