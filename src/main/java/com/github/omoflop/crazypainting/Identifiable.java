package com.github.omoflop.crazypainting;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public interface Identifiable {
    Identifier getId();
    static ResourceKey<Item> key(String registryName) {
        return ResourceKey.create(Registries.ITEM, CrazyPainting.id(registryName));
    }
}
