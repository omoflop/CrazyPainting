package com.github.omoflop.crazypainting.client.compat;

import net.fabricmc.loader.api.FabricLoader;

public class IrisCompat {
    public static final boolean HAS_IRIS;

    public static boolean isShaderEnabled() {
        return HAS_IRIS && net.irisshaders.iris.api.v0.IrisApi.getInstance().isShaderPackInUse();
    }

    static {
        HAS_IRIS = FabricLoader.getInstance().isModLoaded("iris");
    }
}
