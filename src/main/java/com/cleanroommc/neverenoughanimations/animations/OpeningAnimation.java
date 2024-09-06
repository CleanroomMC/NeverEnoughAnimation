package com.cleanroommc.neverenoughanimations.animations;

import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import com.cleanroommc.neverenoughanimations.util.Interpolations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jetbrains.annotations.Nullable;

public class OpeningAnimation {

    public static boolean onGuiOpen(@Nullable Screen newScreen, @Nullable Screen currentScreen, Runnable closeCallback) {
        if (newScreen instanceof AbstractContainerScreen<?> container) {
            if (currentScreen == null) {
                animate(container, true, closeCallback);
            }
        } else if (currentScreen == lastGui && newScreen == null && !shouldCloseLast) {
            animate(lastGui, false, closeCallback);
            return true;
        }
        return false;
    }

    private static AbstractContainerScreen<?> lastGui;
    private static AbstractContainerScreen<?> animatedGui;
    private static long startTime = 0;
    private static boolean shouldCloseLast = false;

    public static void animate(AbstractContainerScreen<?> container, boolean open, Runnable closeCallback) {
        if (NEAConfig.openingAnimationTime == 0 || NEAConfig.isBlacklisted(container)) return;
        animatedGui = container;
        lastGui = container;
        startTime = NEA.time() * (open ? 1 : -1);
        if (!open) closeCallback.run();
    }

    public static float getScale(float value) {
        float min = 0.75f, max = 1f;
        return Interpolations.lerp(min, max, value);
    }

    public static float getValue(AbstractContainerScreen<?> container) {
        if (shouldCloseLast) return 0.001f;
        if (animatedGui != container) return 1f;
        float val = (NEA.time() - Math.abs(startTime)) / (float) NEAConfig.openingAnimationTime;
        if (startTime < 0) {
            val = 1f - val;
            if (val <= 0) {
                animatedGui = null;
                shouldCloseLast = true;
                return 0.001f;
            }
        } else if (val >= 1f) {
            animatedGui = null;
            return 1f;
        }

        return NEAConfig.openingAnimationCurve.interpolate(0f, 1f, val);
    }

    public static boolean handleScale(GuiGraphics graphics, AbstractContainerScreen<?> container, boolean translateToPanel) {
        float value = getValue(container);
        if (value >= 1 || value <= 0 || NEAConfig.openingAnimationTime == 0) return false;
        float scale = getScale(value);
        if (translateToPanel) graphics.pose().translate(container.getGuiLeft(), container.getGuiTop(), 0);
        graphics.pose().translate(container.getXSize() / 2f, container.getYSize() / 2f, 0);
        graphics.pose().scale(scale, scale, 1f);
        graphics.pose().translate(-container.getXSize() / 2f, -container.getYSize() / 2f, 0);
        if (translateToPanel) graphics.pose().translate(-container.getGuiLeft(), -container.getGuiTop(), 0);
        // this only applies to text currently
        // for textures we would probably need to mixin GuiGraphics and modify the buffer builder to accept color
        graphics.setColor(1f, 1f, 1f, value);
        return true;
    }

    public static void checkGuiToClose() {
        if (shouldCloseLast && lastGui != null) {
            Minecraft.getInstance().setScreen(null);
            shouldCloseLast = false;
            lastGui = null;
        }
    }
}
