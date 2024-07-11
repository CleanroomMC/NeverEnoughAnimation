package com.cleanroommc.neverenoughanimations;

import com.cleanroommc.neverenoughanimations.util.Interpolation;
import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MODID)
public class NEAConfig {

    @Config.SlidingOption
    @Config.RangeInt(min = 0, max = 2000)
    @Config.Comment("How many millieseconds it takes until an item is scaled to its full size on hover. 0 to disable.")
    public static int hoverAnimationTime = 250;

    public static Interpolation hoverAnimationCurve = Interpolation.QUAD_INOUT;

    @Config.Comment("If the gray slot overlay should be rendered at all on hover.")
    public static boolean itemHoverOverlay = false;

    @Config.SlidingOption
    @Config.RangeInt(min = 0, max = 2000)
    @Config.Comment("How many millieseconds it takes until an item has moved to its target. 0 to disable.")
    public static int moveAnimationTime = 500;

    public static Interpolation moveAnimationCurve = Interpolation.QUART_OUT;

    @Config.SlidingOption
    @Config.RangeInt(min = 0, max = 2000)
    @Config.Comment("How many millieseconds it takes until the current item marker in the hotbar moved to its new location. 0 to disable.")
    public static int hotbarAnimationTime = 100;
    public static Interpolation hotbarAnimationCurve = Interpolation.QUAD_INOUT;
}