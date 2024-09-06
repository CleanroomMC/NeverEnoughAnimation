package com.cleanroommc.neverenoughanimations.core;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * The whole purpose of this class is to not load mixins if its required mod is not loaded.
 */
public abstract class AbstractMixinPlugin implements IMixinConfigPlugin {

    private final boolean classLoaded;

    /**
     * Creates a class which checks if a mod is loaded by checking if a specific class of the mod can be loaded.
     * The given class should not be something, people may want to mixin to.
     *
     * @param modClassName mod class name to try to load
     */
    public AbstractMixinPlugin(String modClassName) {
        this.classLoaded = isClassFound(modClassName);
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return classLoaded;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    public static boolean isClassFound(String className) {
        try {
            Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
