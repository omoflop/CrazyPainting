package com.github.omoflop.crazypainting.content;

import com.github.omoflop.crazypainting.CrazyPainting;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public class CrazySounds {
    public static final SoundEvent COLOR_PICKER_USE = register("color_picker_use");
    public static final SoundEvent BRUSH_USE = register("brush_use");
    public static final SoundEvent UNDO = register("undo");

    public static void register() { }

    private static SoundEvent register(String name, Optional<Float> optional) {
        SoundEvent soundEvent = new SoundEvent(CrazyPainting.id(name), optional);
        Registry.register(BuiltInRegistries.SOUND_EVENT, soundEvent.location(), soundEvent);
        return soundEvent;
    }

    private static SoundEvent register(String name) {
        return register(name, Optional.empty());
    }

}
