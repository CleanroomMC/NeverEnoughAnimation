package com.cleanroommc.neverenoughanimations.animations;

import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.NEAConfig;
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
        HotbarAnimation.startTime = NEA.time();
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

    public static int getX(int scaledWidth) {
        int def = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
        if (NEAConfig.hotbarAnimationTime == 0 || oldIndex < 0 || newIndex < 0) return getX(scaledWidth, def);
        if (def != newIndex) {
            // index unexpectedly changed, abort animation
            reset();
            return getX(scaledWidth, def);
        }
        float val = (NEA.time() - HotbarAnimation.startTime) / (float) NEAConfig.hotbarAnimationTime;
        if (val >= 1f) {
            // animation ended
            reset();
            return getX(scaledWidth, def);
        }
        if (fromX < 0) fromX = getX(scaledWidth, oldIndex);
        currentX = (int) NEAConfig.hotbarAnimationCurve.interpolate(fromX, getX(scaledWidth, newIndex), val);
        return currentX;
    }

    public static int getX(int scaledWidth, int index) {
        return scaledWidth/ 2 - 91 - 1 + index * 20; // vanilla behaviour
    }
}
