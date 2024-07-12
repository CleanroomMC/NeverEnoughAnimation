package com.cleanroommc.neverenoughanimations;

import com.cleanroommc.neverenoughanimations.util.Interpolation;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MODID)
public class NEAConfig {

    @Config.Name("Hover animation time")
    @Config.SlidingOption
    @Config.RangeInt(min = 0, max = 1000)
    @Config.Comment("How many millieseconds it takes until an item is scaled to its full size on hover. 0 to disable.")
    public static int hoverAnimationTime = 150;
    @Config.Name("Hover animation easing curve")
    public static Interpolation hoverAnimationCurve = Interpolation.QUAD_INOUT;

    @Config.Name("Item hover overlay")
    @Config.Comment("If the gray slot overlay (minecraft feature) should be rendered at all on hover. Default: false")
    public static boolean itemHoverOverlay = false;

    @Config.Name("Move animation time")
    @Config.SlidingOption
    @Config.RangeInt(min = 0, max = 1000)
    @Config.Comment("How many millieseconds it takes until an item has moved to its target (activated on shift click). 0 to disable.")
    public static int moveAnimationTime = 200;
    @Config.Name("Move animation easing curve")
    public static Interpolation moveAnimationCurve = Interpolation.SINE_OUT;

    @Config.Name("Hotbar animation time")
    @Config.SlidingOption
    @Config.RangeInt(min = 0, max = 1000)
    @Config.Comment("How many millieseconds it takes until the current item marker in the hotbar moved to its new location. 0 to disable.")
    public static int hotbarAnimationTime = 100;
    @Config.Name("Hotbar animation easing curve")
    public static Interpolation hotbarAnimationCurve = Interpolation.QUAD_INOUT;

    @Config.Name("Opening/Closing animation time")
    @Config.SlidingOption
    @Config.RangeInt(min = 0, max = 1000)
    @Config.Comment("How many millieseconds it takes until the gui is fully opened. 0 to disable.")
    public static int openingAnimationTime = 60;
    @Config.Name("Opening/Closing animation easing curve")
    public static Interpolation openingAnimationCurve = Interpolation.SINE_OUT;

    @Config.Name("Gui class animation blacklist")
    @Config.Comment({"Add class names (works with * at the end) which should be blacklisted from any animations.",
            "This is used to prevent visual issues with certain mods."})
    public static String[] guiAnimationBlacklist = {"gregtech.*", "com.cleanroommc.modularui.*"};

    @Config.Ignore
    public static Object2BooleanOpenHashMap<Class<?>> blacklistCache = new Object2BooleanOpenHashMap<>();

    public static boolean isBlacklisted(GuiScreen screen) {
        if (screen == null) return true;
        if (guiAnimationBlacklist.length == 0) return false;
        if (blacklistCache.containsKey(screen.getClass())) return blacklistCache.getBoolean(screen.getClass());
        String name = screen.getClass().getName();
        for (String gui : guiAnimationBlacklist) {
            if (gui.endsWith("*")) {
                if (name.startsWith(gui.substring(gui.length() - 1))) {
                    blacklistCache.put(screen.getClass(), true);
                    return true;
                }
            } else if (name.equals(gui)) {
                blacklistCache.put(screen.getClass(), true);
                return true;
            }
        }
        blacklistCache.put(screen.getClass(), false);
        return false;
    }
}
