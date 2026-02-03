package com.github.omoflop.crazypainting.content;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.components.CanvasDataComponent;
import com.github.omoflop.crazypainting.components.PaletteColorsComponent;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;

public class CrazyComponents {
    public static final DataComponentType<PaletteColorsComponent> PALETTE_COLORS = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            CrazyPainting.id("palette_colors"),
            DataComponentType.<PaletteColorsComponent>builder().persistent(PaletteColorsComponent.CODEC).build()
    );
    public static final DataComponentType<CanvasDataComponent> CANVAS_DATA = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            CrazyPainting.id("canvas_data"),
            DataComponentType.<CanvasDataComponent>builder().persistent(CanvasDataComponent.CODEC).build()
    );

    public static void register() {
        ComponentTooltipAppenderRegistry.addBefore(DataComponents.LORE, CANVAS_DATA);
        ComponentTooltipAppenderRegistry.addAfter(DataComponents.ATTRIBUTE_MODIFIERS, PALETTE_COLORS);
    }
}
