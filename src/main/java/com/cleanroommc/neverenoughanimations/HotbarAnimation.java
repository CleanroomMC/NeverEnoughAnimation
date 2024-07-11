package com.cleanroommc.neverenoughanimations;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class HotbarAnimation {

    private static int oldIndex = -1, newIndex = -1;
    private static int fromX = -1, currentX = -1;
    private static long startTime = 0;

    public static void animate(int oldIndex, int newIndex) {
        if (NEAConfig.hotbarAnimationTime == 0) return;
        if (isAnimationInProgress()) {
            fromX = currentX;
        } else {
            HotbarAnimation.oldIndex = oldIndex;
        }
        HotbarAnimation.newIndex = newIndex;
        if (HotbarAnimation.oldIndex == HotbarAnimation.newIndex) {
            reset();
            return;
        }
        HotbarAnimation.startTime = Minecraft.getSystemTime();
    }

    public static boolean isAnimationInProgress() {
        return HotbarAnimation.oldIndex >= 0;
    }

    public static void reset() {
        HotbarAnimation.oldIndex = -1;
        HotbarAnimation.newIndex = -1;
        HotbarAnimation.fromX = -1;
        HotbarAnimation.currentX = -1;
    }

    public static int getX(ScaledResolution sr) {
        int def = Minecraft.getMinecraft().player.inventory.currentItem;
        if (NEAConfig.hotbarAnimationTime == 0 || oldIndex < 0 || newIndex < 0) return getX(sr, def);
        if (def != newIndex) {
            // index unexpectedly changed, abort animation
            reset();
            return getX(sr, def);
        }
        float val = (Minecraft.getSystemTime() - HotbarAnimation.startTime) / (float) NEAConfig.hotbarAnimationTime;
        if (val >= 1f) {
            // animation ended
            reset();
            return getX(sr, def);
        }
        if (fromX < 0) fromX = getX(sr, oldIndex);
        currentX = (int) NEAConfig.hotbarAnimationCurve.interpolate(fromX, getX(sr, newIndex), val);
        return currentX;
    }

    public static int getX(ScaledResolution sr, int index) {
        return sr.getScaledWidth() / 2 - 91 - 1 + index * 20; // vanilla behaviour
    }
}
