package com.cleanroommc.neverenoughanimations.animations;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;

import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import com.cleanroommc.neverenoughanimations.api.IAnimatedScreen;
import com.cleanroommc.neverenoughanimations.util.GlStateManager;
import com.cleanroommc.neverenoughanimations.util.Interpolations;

public class OpeningAnimation {

    public static boolean onGuiOpen(GuiOpenEvent event) {
        if (onGuiOpen(event.gui)) {
            event.setCanceled(true);
            return true;
        }
        return false;
    }

    public static boolean onGuiOpen(GuiScreen screen) {
        if (screen instanceof IAnimatedScreen animatedScreen) {
            if (Minecraft.getMinecraft().currentScreen == null) {
                animate(animatedScreen, true);
            }
        } else if (Minecraft.getMinecraft().currentScreen == lastGui && screen == null && !shouldCloseLast) {
            if (animatedGui == null || getValue(animatedGui) >= 1f || startTime > 0) {
                // only start close animation when we aren't already closing
                // if we are currently opening we start at the end of the animation
                // this can look wonky on long animation times
                animate(lastGui, false);
            }
            return true;
        }
        return false;
    }

    public static boolean currentlyScaling = false;

    private static IAnimatedScreen lastGui;
    private static IAnimatedScreen animatedGui;
    // private static boolean oldAllowAllInteractions = false;
    private static long startTime = 0;
    private static boolean shouldCloseLast = false;

    public static void animate(IAnimatedScreen container, boolean open) {
        if (NEAConfig.openingAnimationTime == 0 || NEAConfig.isBlacklisted(container)) return;
        animatedGui = container;
        lastGui = container;
        startTime = NEA.time();
        if (!open) {
            startTime = -startTime;
            Minecraft.getMinecraft()
                .setIngameFocus();
            // oldAllowAllInteractions = ((GuiScreen) container).allowUserInput;
            // ((GuiScreen) container).allowUserInput = true;
        }
    }

    public static float getScale(GuiScreen screen) {
        return screen instanceof IAnimatedScreen animatedScreen ? getScale(animatedScreen) : 1f;
    }

    public static float getScale(IAnimatedScreen container) {
        float min = NEAConfig.openingStartScale, max = 1f;
        return Interpolations.lerp(min, max, getValue(container));
    }

    public static float getValue(GuiScreen screen) {
        return screen instanceof IAnimatedScreen animatedScreen ? getValue(animatedScreen) : 1f;
    }

    public static float getValue(IAnimatedScreen container) {
        if (shouldCloseLast) return 0.001f;
        if (animatedGui != container) return 1f;
        float val = (NEA.time() - Math.abs(startTime)) / (float) NEAConfig.openingAnimationTime;
        if (startTime < 0) {
            val = 1f - val;
            if (val <= 0) {
                animatedGui = null;
                shouldCloseLast = true;
                return 0f;
            }
        } else if (val >= 1f) {
            animatedGui = null;
            return 1f;
        }

        return NEAConfig.openingAnimationCurve.interpolate(0f, 1f, val);
    }

    public static boolean handleScale(GuiScreen screen, boolean translateToPanel) {
        return screen instanceof IAnimatedScreen animatedScreen && handleScale(animatedScreen, translateToPanel);
    }

    public static boolean handleScale(IAnimatedScreen screen, boolean translateToPanel) {
        float scale = getScale(screen);
        if (scale == 1 || NEAConfig.moveAnimationTime == 0) return false;
        if (translateToPanel) GlStateManager.translate(screen.nea$getX(), screen.nea$getY(), 0);
        GlStateManager.translate(screen.nea$getWidth() / 2f, screen.nea$getHeight() / 2f, 0);
        GlStateManager.scale(scale, scale, 1f);
        GlStateManager.translate(-screen.nea$getWidth() / 2f, -screen.nea$getHeight() / 2f, 0);
        if (translateToPanel) GlStateManager.translate(-screen.nea$getX(), -screen.nea$getY(), 0);
        return true;
    }

    public static boolean isAnimating(GuiScreen screen) {
        return screen instanceof IAnimatedScreen animatedScreen && isAnimating(animatedScreen);
    }

    public static boolean isAnimating(IAnimatedScreen screen) {
        return getValue(screen) < 1f;
    }

    public static boolean isAnimatingClose(GuiScreen screen) {
        return screen instanceof IAnimatedScreen animatedScreen && isAnimatingClose(animatedScreen);
    }

    public static boolean isAnimatingClose(IAnimatedScreen screen) {
        return isAnimating(screen) && startTime < 0;
    }

    public static void checkGuiToClose() {
        if (shouldCloseLast && lastGui != null) {
            // ((GuiScreen) lastGui).allowUserInput = oldAllowAllInteractions;
            // oldAllowAllInteractions = false;
            Minecraft.getMinecraft()
                .displayGuiScreen(null);
            shouldCloseLast = false;
            lastGui = null;
        }
    }
}
