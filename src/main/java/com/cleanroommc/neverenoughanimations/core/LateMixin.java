package com.cleanroommc.neverenoughanimations.core;

import com.cleanroommc.neverenoughanimations.Tags;
import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LateMixin implements ILateMixinLoader {

    private static final String[] mods = {
            "trashslot",
            "jei",
            "mousetweaks",
            "thermalexpansion",
            "draconicevolution",
            "avaritiaddons",
            "colossalchests",
            "rustic"
    };

    @Override
    public List<String> getMixinConfigs() {
        return Arrays.stream(mods)
                .map(m -> "mixin." + Tags.MODID + "." + m + ".json")
                .collect(Collectors.toList());
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        return Loader.isModLoaded(mixinConfig.split("\\.")[2]);
    }
}
