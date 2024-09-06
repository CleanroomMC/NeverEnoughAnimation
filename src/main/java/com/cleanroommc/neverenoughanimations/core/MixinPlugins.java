package com.cleanroommc.neverenoughanimations.core;

/**
 * This class holds mixin plugin class which are defined in their respective mixin config.
 * These classes are instantiated by Mixin.
 */
public class MixinPlugins {

    public static class MouseTweaks extends AbstractMixinPlugin {

        public MouseTweaks() {
            super("yalter.mousetweaks.Logger");
        }
    }

    public static class Jei extends AbstractMixinPlugin {

        public Jei() {
            super("mezz.jei.api.JeiPlugin");
        }
    }
}
