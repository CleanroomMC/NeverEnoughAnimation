package com.cleanroommc.neverenoughanimations.animations;

import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public class HotbarAnimation {

    private static int oldIndex = -1, newIndex = -1;
    private static int fromX = -1, currentX = -1;
    private static long startTime = 0;

    private static int currentSelected = -1;

    public static int getSelected() {
        Player player = Minecraft.getInstance().player;
        return player != null ? player.getInventory().selected : -1;
    }

    public static void preTick() {
        currentSelected = getSelected();
    }

    public static void postTick() {
        int selected = getSelected();
        if (currentSelected >= 0 && selected != currentSelected) {
            animate(currentSelected, selected);
        }
        currentSelected = -1;
    }

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

    public static int getX(GuiGraphics graphics) {
        int def = Minecraft.getInstance().player.getInventory().selected;
        if (NEAConfig.hotbarAnimationTime == 0 || oldIndex < 0 || newIndex < 0) return getX(graphics, def);
        if (def != newIndex) {
            // index unexpectedly changed, abort animation
            reset();
            return getX(graphics, def);
        }
        float val = (NEA.time() - HotbarAnimation.startTime) / (float) NEAConfig.hotbarAnimationTime;
        if (val >= 1f) {
            // animation ended
            reset();
            return getX(graphics, def);
        }
        if (fromX < 0) fromX = getX(graphics, oldIndex);
        currentX = (int) NEAConfig.hotbarAnimationCurve.interpolate(fromX, getX(graphics, newIndex), val);
        return currentX;
    }

    public static int getX(GuiGraphics graphics, int index) {
        return graphics.guiWidth() / 2 - 91 - 1 + index * 20; // vanilla behaviour
    }
}
