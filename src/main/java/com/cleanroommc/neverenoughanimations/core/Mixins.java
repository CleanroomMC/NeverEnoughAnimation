package com.cleanroommc.neverenoughanimations.core;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.cleanroommc.neverenoughanimations.core.TargetedMod.NEI;
import static com.cleanroommc.neverenoughanimations.core.TargetedMod.VANILLA;

public enum Mixins {

    // Vanilla client
    ContainerMixin("ContainerMixin", Phase.EARLY, Side.CLIENT, VANILLA),
    GuiContainerAccessor("GuiContainerAccessor", Phase.EARLY, Side.CLIENT, VANILLA),
    GuiContainerMixin("GuiContainerMixin", Phase.EARLY, Side.CLIENT, VANILLA),
    GuiIngameMixin("GuiIngameMixin", Phase.EARLY, Side.CLIENT, VANILLA),
    GuiScreenMixin("GuiScreenMixin", Phase.EARLY, Side.CLIENT, VANILLA),
    InventoryPlayerMixin("InventoryPlayerMixin", Phase.EARLY, Side.CLIENT, VANILLA),
    MinecraftMixin("MinecraftMixin", Phase.EARLY, Side.CLIENT, VANILLA),
    SlotMixin("SlotMixin", Phase.EARLY, Side.CLIENT, VANILLA),
    TessellatorMixin("TessellatorMixin", Phase.EARLY, Side.CLIENT, VANILLA),

    GuiContainerManagerMixin("nei.GuiContainerManagerMixin", Phase.LATE, Side.CLIENT, NEI);

    public final String mixinClass;
    public final Phase phase;
    private final Side side;
    private final List<TargetedMod> targetedMods;

    Mixins(String mixinClass, TargetedMod... targetedMods) {
        this.mixinClass = mixinClass;
        this.phase = Phase.LATE;
        this.side = Side.BOTH;
        this.targetedMods = Arrays.asList(targetedMods);
    }

    Mixins(String mixinClass, Phase phase, TargetedMod... targetedMods) {
        this.mixinClass = mixinClass;
        this.phase = phase;
        this.side = Side.BOTH;
        this.targetedMods = Arrays.asList(targetedMods);
    }

    Mixins(String mixinClass, Side side, TargetedMod... targetedMods) {
        this.mixinClass = mixinClass;
        this.phase = Phase.LATE;
        this.side = side;
        this.targetedMods = Arrays.asList(targetedMods);
    }

    Mixins(String mixinClass, Phase phase, Side side, TargetedMod... targetedMods) {
        this.mixinClass = mixinClass;
        this.phase = phase;
        this.side = side;
        this.targetedMods = Arrays.asList(targetedMods);
    }

    public boolean shouldLoad(Set<String> loadedCoreMods, Set<String> loadedMods) {
        return shouldLoadSide() && allModsLoaded(targetedMods, loadedCoreMods, loadedMods);
    }

    private boolean shouldLoadSide() {
        return (side == Side.BOTH || (side == Side.SERVER && FMLLaunchHandler.side().isServer())
            || (side == Side.CLIENT && FMLLaunchHandler.side().isClient()));
    }

    private boolean allModsLoaded(List<TargetedMod> targetedMods, Set<String> loadedCoreMods, Set<String> loadedMods) {
        if (targetedMods.isEmpty()) return false;
        for (TargetedMod target : targetedMods) {
            if (target == TargetedMod.VANILLA) continue;
            // Check coremod first
            if (!loadedCoreMods.isEmpty() && target.coreModClass != null
                && !loadedCoreMods.contains(target.coreModClass)) {
                return false;
            } else if (!loadedMods.isEmpty() && target.modId != null && !loadedMods.contains(target.modId)) {
                return false;
            }
        }
        return true;
    }

    enum Side {
        BOTH,
        CLIENT,
        SERVER
    }

    public enum Phase {
        EARLY,
        LATE
    }
}
