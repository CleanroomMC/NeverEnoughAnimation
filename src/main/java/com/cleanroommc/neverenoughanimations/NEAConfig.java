package com.cleanroommc.neverenoughanimations;

import com.cleanroommc.neverenoughanimations.util.Interpolation;
import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MODID)
public class NEAConfig {

    @Config.RangeInt(min = 0, max = 2000)
    @Config.Comment("How many millieseconds it takes until an item is scaled to its full size on hover. 0 to disable.")
    public static int hoverAnimationTime = 250;

    public static Interpolation hoverAnimationCurve = Interpolation.QUAD_INOUT;

    @Config.Comment("If the gray slot overlay should be rendered at all on hover.")
    public static boolean itemHoverOverlay = false;

    @Config.Comment("How many millieseconds it takes until an item has moved to its target. 0 to disable.")
    public static int moveAnimationTime = 500;

    public static Interpolation moveAnimationCurve = Interpolation.QUART_OUT;

}
