package com.github.omoflop.crazypainting.client.datagen;

import com.github.omoflop.crazypainting.CrazyPainting;
import com.github.omoflop.crazypainting.content.CrazySounds;
import net.fabricmc.fabric.api.client.datagen.v1.builder.SoundTypeBuilder;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricSoundsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import java.util.concurrent.CompletableFuture;

public class CrazySoundProvider extends FabricSoundsProvider {
    public CrazySoundProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(HolderLookup.Provider wrapperLookup, SoundExporter sounds) {
        sounds.add(CrazySounds.BRUSH_USE, SoundTypeBuilder.of(CrazySounds.BRUSH_USE)
                .category(SoundSource.UI)
                .sound(SoundTypeBuilder.EntryBuilder.ofFile(CrazyPainting.id("brush0")))
                .sound(SoundTypeBuilder.EntryBuilder.ofFile(CrazyPainting.id("brush1")))
                .sound(SoundTypeBuilder.EntryBuilder.ofFile(CrazyPainting.id("brush2")))
                .sound(SoundTypeBuilder.EntryBuilder.ofFile(CrazyPainting.id("brush3")))
                .sound(SoundTypeBuilder.EntryBuilder.ofFile(CrazyPainting.id("brush4")))
                .sound(SoundTypeBuilder.EntryBuilder.ofFile(CrazyPainting.id("brush5")))
        );

        sounds.add(CrazySounds.COLOR_PICKER_USE, SoundTypeBuilder.of(CrazySounds.COLOR_PICKER_USE)
                .category(SoundSource.UI)
                .sound(SoundTypeBuilder.EntryBuilder.ofFile(CrazyPainting.id("pick_color")))
        );

        sounds.add(CrazySounds.UNDO, SoundTypeBuilder.of(CrazySounds.UNDO)
                .category(SoundSource.UI)
                .sound(SoundTypeBuilder.EntryBuilder.ofEvent(SoundEvents.ITEM_FRAME_REMOVE_ITEM))
        );
    }

    @Override
    public String getName() {
        return "CrazySoundProvider";
    }
}
