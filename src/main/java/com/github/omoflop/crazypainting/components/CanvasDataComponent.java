package com.github.omoflop.crazypainting.components;

import com.github.omoflop.crazypainting.content.CrazyComponents;
import com.github.omoflop.crazypainting.items.CanvasItem;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;


public record CanvasDataComponent(int id, boolean glow, String signedBy, String title, byte generation) implements TooltipProvider {
    public static final Codec<CanvasDataComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("id").forGetter(CanvasDataComponent::id),
            Codec.BOOL.fieldOf("glow").forGetter(CanvasDataComponent::glow),
            Codec.STRING.fieldOf("signed_by").forGetter(CanvasDataComponent::signedBy),
            Codec.STRING.fieldOf("title").forGetter(CanvasDataComponent::title),
            Codec.BYTE.fieldOf("generation").forGetter(CanvasDataComponent::generation)
    ).apply(builder, CanvasDataComponent::new));

    public static final CanvasDataComponent DEFAULT = new CanvasDataComponent(-1, false, "", CanvasItem.UNTITLED, (byte) 0);


    public static CanvasDataComponent withId(@Nullable CanvasDataComponent data, int id) {
        if (data == null) data = DEFAULT;
        return new CanvasDataComponent(id, data.glow, data.signedBy, data.title, data.generation);
    }

    public CanvasDataComponent withId(int id) {
        return new CanvasDataComponent(id, glow, signedBy, title, generation);
    }

    public CanvasDataComponent withGlow(boolean glow) {
        return new CanvasDataComponent(id, glow, signedBy, title, generation);
    }

    public CanvasDataComponent withSignedBy(String signedBy) {
        return new CanvasDataComponent(id, glow, signedBy, title, generation);
    }

    public CanvasDataComponent withTitle(String title) {
        return new CanvasDataComponent(id, glow, signedBy, title, generation);
    }

    public CanvasDataComponent withGeneration(byte generation) {
        return new CanvasDataComponent(id, glow, signedBy, title, generation);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> textConsumer, TooltipFlag type, DataComponentGetter components) {

        CanvasDataComponent data = components.get(CrazyComponents.CANVAS_DATA);
        assert data != null;
        if (type.isAdvanced()) {
            textConsumer.accept(Component.literal("Canvas ID: " + data.id).withStyle(ChatFormatting.GRAY));
        }

        if (data.signedBy != null && !data.signedBy.isEmpty()) {
            appendSignedByTooltip(data, textConsumer);
        }
    }

    private void appendSignedByTooltip(CanvasDataComponent data, Consumer<Component> textConsumer) {
        var selector = SelectorPattern.parse(data.signedBy);

        var arg = Component.literal("Unknown");
        if (selector.isSuccess()) {
            arg = Component.selector(selector.getOrThrow(), Optional.empty());
        }

        textConsumer.accept(Component.translatable("item.crazypainting.canvas.tooltip.signed", arg).withStyle(ChatFormatting.YELLOW));

        if (this.generation >= 0)
            textConsumer.accept(Component.translatable("book.generation." + this.generation).withStyle(ChatFormatting.GRAY));
    }
}
