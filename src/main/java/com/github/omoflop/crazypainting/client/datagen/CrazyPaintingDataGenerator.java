package com.github.omoflop.crazypainting.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class CrazyPaintingDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(CrazyModelProvider::new);
        pack.addProvider(CrazyRecipeProvider::new);
        pack.addProvider(CrazySoundProvider::new);
    }

}
