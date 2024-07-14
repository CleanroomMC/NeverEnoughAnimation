package com.cleanroommc.neverenoughanimations.animations;

import com.cleanroommc.neverenoughanimations.NEAConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiOpenEvent;

public class OpeningAnimation {

    public static boolean onGuiOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiContainer container) {
            if (Minecraft.getMinecraft().currentScreen == null) {
                animate(container, true);
            }
        } else if (Minecraft.getMinecraft().currentScreen == lastGui && event.getGui() == null && !shouldCloseLast) {
            animate(lastGui, false);
            event.setCanceled(true);
            return true;
        }
        return false;
    }

    private static GuiContainer lastGui;
    private static GuiContainer animatedGui;
    private static long startTime = 0;
    private static boolean shouldCloseLast = false;

    public static void animate(GuiContainer container, boolean open) {
        if (NEAConfig.openingAnimationTime == 0 || NEAConfig.isBlacklisted(container)) return;
        animatedGui = container;
        lastGui = container;
        startTime = Minecraft.getSystemTime() * (open ? 1 : -1);
    }

    public static float getScale(GuiContainer container) {
        if (shouldCloseLast) return 0.001f;
        if (animatedGui != container) return 1f;
        float val = (Minecraft.getSystemTime() - Math.abs(startTime)) / (float) NEAConfig.openingAnimationTime;
        float min = 0.75f, max = 1f;
        if (startTime < 0) {
            val = 1f - val;
            if (val <= 0) {
                animatedGui = null;
                shouldCloseLast = true;
                return min;
            }
        } else if (val >= 1f) {
            animatedGui = null;
            return 1f;
        }

        return NEAConfig.openingAnimationCurve.interpolate(min, max, val);
    }

    public static boolean handleScale(GuiContainer container, boolean translateToPanel) {
        float scale = getScale(container);
        if (scale == 1 || NEAConfig.moveAnimationTime == 0) return false;
        if (translateToPanel) GlStateManager.translate(container.getGuiLeft(), container.getGuiTop(), 0);
        GlStateManager.translate(container.getXSize() / 2f, container.getYSize() / 2f, 0);
        GlStateManager.scale(scale, scale, 1f);
        GlStateManager.translate(-container.getXSize() / 2f, -container.getYSize() / 2f, 0);
        if (translateToPanel) GlStateManager.translate(-container.getGuiLeft(), -container.getGuiTop(), 0);
        // GlStateManager.color(1f, 1f, 1f, scale);
        return true;
    }

    public static void checkGuiToClose() {
        if (shouldCloseLast && lastGui != null) {
            Minecraft.getMinecraft().displayGuiScreen(null);
            shouldCloseLast = false;
            lastGui = null;
        }
    }
}
