package com.cleanroommc.neverenoughanimations;

import com.cleanroommc.neverenoughanimations.util.Interpolation;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MODID)
public class NEAConfig {

    @Config.Name("Hover animation time")
    @Config.SlidingOption
    @Config.RangeInt(min = 0, max = 500)
    @Config.Comment("How many millieseconds it takes until an item is scaled to its full size on hover. 0 to disable.")
    public static int hoverAnimationTime = 100;
    @Config.Name("Hover animation easing curve")
    public static Interpolation hoverAnimationCurve = Interpolation.QUAD_INOUT;

    @Config.Name("Item hover overlay")
    @Config.Comment("If the gray slot overlay (minecraft feature) should be rendered at all on hover. Default: false")
    public static boolean itemHoverOverlay = false;

    @Config.Name("Item move animation time")
    @Config.SlidingOption
    @Config.RangeInt(min = 0, max = 500)
    @Config.Comment("How many millieseconds it takes until an item has moved to its target (activated on shift click). 0 to disable.")
    public static int moveAnimationTime = 100;
    @Config.Name("Item move animation easing curve")
    public static Interpolation moveAnimationCurve = Interpolation.SINE_OUT;

    @Config.Name("Item (dis)appear animation time")
    @Config.SlidingOption
    @Config.RangeInt(min = 0, max = 500)
    @Config.Comment("How many millieseconds it takes until an item has moved to its target (activated on shift click). 0 to disable.")
    public static int appearAnimationTime = 100;
    @Config.Name("Item (dis)appear animation easing curve")
    public static Interpolation appearAnimationCurve = Interpolation.SINE_OUT;

    @Config.Name("Hotbar animation time")
    @Config.SlidingOption
    @Config.RangeInt(min = 0, max = 500)
    @Config.Comment("How many millieseconds it takes until the current item marker in the hotbar moved to its new location. 0 to disable.")
    public static int hotbarAnimationTime = 100;
    @Config.Name("Hotbar animation easing curve")
    public static Interpolation hotbarAnimationCurve = Interpolation.QUAD_INOUT;

    @Config.Name("Opening/Closing animation time")
    @Config.SlidingOption
    @Config.RangeInt(min = 0, max = 500)
    @Config.Comment("How many millieseconds it takes until the gui is fully opened. 0 to disable. 200 and lower is recommended.")
    public static int openingAnimationTime = 90;
    @Config.Name("Opening/Closing animation easing curve")
    public static Interpolation openingAnimationCurve = Interpolation.SINE_OUT;

    @Config.Name("Opening/Closing start/end scale")
    @Config.SlidingOption
    @Config.RangeDouble(min = 0.0, max = 1.0)
    @Config.Comment("The scale at which the opening animation starts. What looks good depends on the animation time. Rule of thumb is the shorter the animation time, the larger the start scale.")
    public static float openingStartScale = 0.9f;

    @Config.Name("Animate dark GUI background")
    @Config.Comment("If the dark background should be animated too during opening animation. This will fade in the alpha if the background color. However this may cause issues with resource packs which disable the dark background.")
    public static boolean animateDarkGuiBackground = true;

    @Config.Name("Gui class animation blacklist")
    @Config.Comment({"Add class names (works with * at the end) which should be blacklisted from any animations.",
            "This is used to prevent visual issues with certain mods."})
    public static String[] guiAnimationBlacklist = {"gregtech.*", "com.cleanroommc.modularui.*", "com.creativemd.creativecore.*"};

    @Config.Ignore
    public static Object2BooleanOpenHashMap<Class<?>> blacklistCache = new Object2BooleanOpenHashMap<>();

    public static boolean isBlacklisted(Object screen) {
        if (screen == null) return true;
        if (guiAnimationBlacklist.length == 0) return false;
        if (blacklistCache.containsKey(screen.getClass())) return blacklistCache.getBoolean(screen.getClass());
        String name = screen.getClass().getName();
        for (String gui : guiAnimationBlacklist) {
            if (gui.endsWith("*")) {
                if (name.startsWith(gui.substring(0, gui.length() - 1))) {
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
