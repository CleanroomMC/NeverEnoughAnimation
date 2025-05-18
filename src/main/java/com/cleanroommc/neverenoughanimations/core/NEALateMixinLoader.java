package com.cleanroommc.neverenoughanimations.core;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
@LateMixin
public class NEALateMixinLoader implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.neverenoughanimations.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        final List<String> mixins = new ArrayList<>();
        for (Mixins mixin : Mixins.values()) {
            if (mixin.phase == Mixins.Phase.LATE && mixin.shouldLoad(Collections.emptySet(), loadedMods)) {
                mixins.add(mixin.mixinClass);
            }
        }
        return mixins;
    }
}
