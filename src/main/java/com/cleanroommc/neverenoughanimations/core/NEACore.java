package com.cleanroommc.neverenoughanimations.core;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("unused")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class NEACore implements IFMLLoadingPlugin, IEarlyMixinLoader {

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public String getMixinConfig() {
        return "mixins.neverenoughanimations.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        final List<String> mixins = new ArrayList<>();
        for (Mixins mixin : Mixins.values()) {
            if (mixin.phase == Mixins.Phase.EARLY && mixin.shouldLoad(loadedCoreMods, Collections.emptySet())) {
                mixins.add(mixin.mixinClass);
            }
        }
        return mixins;
    }
}
