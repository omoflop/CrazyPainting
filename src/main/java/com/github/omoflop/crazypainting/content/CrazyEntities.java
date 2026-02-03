package com.github.omoflop.crazypainting.content;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.entities.CanvasEntity;
import com.github.omoflop.crazypainting.entities.CanvasEaselEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class CrazyEntities {
    public static final RegistryKey<EntityType<?>> EASEL_ENTITY_REGISTRY_KEY = RegistryKey.of(Registries.ENTITY_TYPE.getKey(), CrazyPainting.id("easel"));
    public static final RegistryKey<EntityType<?>> CANVAS_ENTITY_REGISTRY_KEY = RegistryKey.of(Registries.ENTITY_TYPE.getKey(), CrazyPainting.id("canvas"));

    public static final EntityType<CanvasEaselEntity> EASEL_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            CrazyPainting.id("easel"),
            EntityType.Builder.create(CanvasEaselEntity::new, SpawnGroup.MISC).dimensions(0.75f, 1.95f).build(EASEL_ENTITY_REGISTRY_KEY)
    );
    public static final EntityType<CanvasEntity> CANVAS_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            CrazyPainting.id("canvas"),
            EntityType.Builder.create(CanvasEntity::new, SpawnGroup.MISC).dimensions(1, 1).build(CANVAS_ENTITY_REGISTRY_KEY)
    );

    public static void register() {
        FabricDefaultAttributeRegistry.register(EASEL_ENTITY_TYPE, CanvasEaselEntity.createAttributes());
    }
}
