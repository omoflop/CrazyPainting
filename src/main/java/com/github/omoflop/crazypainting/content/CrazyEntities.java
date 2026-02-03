package com.github.omoflop.crazypainting.content;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.entities.CanvasEntity;
import com.github.omoflop.crazypainting.entities.CanvasEaselEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class CrazyEntities {
    public static final ResourceKey<EntityType<?>> EASEL_ENTITY_REGISTRY_KEY = ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), CrazyPainting.id("easel"));
    public static final ResourceKey<EntityType<?>> CANVAS_ENTITY_REGISTRY_KEY = ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), CrazyPainting.id("canvas"));

    public static final EntityType<CanvasEaselEntity> EASEL_ENTITY_TYPE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            CrazyPainting.id("easel"),
            EntityType.Builder.of(CanvasEaselEntity::new, MobCategory.MISC).sized(0.75f, 1.95f).build(EASEL_ENTITY_REGISTRY_KEY)
    );
    public static final EntityType<CanvasEntity> CANVAS_ENTITY_TYPE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            CrazyPainting.id("canvas"),
            EntityType.Builder.of(CanvasEntity::new, MobCategory.MISC).sized(1, 1).build(CANVAS_ENTITY_REGISTRY_KEY)
    );

    public static void register() {
        FabricDefaultAttributeRegistry.register(EASEL_ENTITY_TYPE, CanvasEaselEntity.createAttributes());
    }
}
