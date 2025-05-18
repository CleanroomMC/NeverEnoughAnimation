package com.cleanroommc.neverenoughanimations.animations;

import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import com.cleanroommc.neverenoughanimations.core.mixin.early.GuiContainerAccessor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiOpenEvent;
import org.jetbrains.annotations.ApiStatus;

@SideOnly(Side.CLIENT)
public class ItemHoverAnimation {

    private static GuiContainer lastHoveredGui = null;
    private static Slot lastHoveredSlot = null;
    private static final Object2LongOpenHashMap<Slot> hoveredSlots = new Object2LongOpenHashMap<>(32);

    @ApiStatus.Internal
    public static void onGuiOpen(GuiOpenEvent event) {
        if (NEAConfig.hoverAnimationTime > 0) {
            if (!(event.gui instanceof GuiContainer)) {
                if (lastHoveredGui != null) {
                    lastHoveredGui = null;
                    lastHoveredSlot = null;
                    hoveredSlots.clear();
                }
                return;
            }
            if (!NEAConfig.isBlacklisted(event.gui)) {
                lastHoveredGui = (GuiContainer) event.gui;
                lastHoveredSlot = null;
                hoveredSlots.clear();
            }
        }
    }

    private static void startAnimation(Slot slot, boolean grow) {
        hoveredSlots.put(slot, NEA.time() * (grow ? 1 : -1));
    }

    public static boolean isAnimating(Slot slot) {
        return hoveredSlots.containsKey(slot);
    }

    @ApiStatus.Internal
    public static void onGuiTick() {
        if (NEAConfig.hoverAnimationTime == 0 || lastHoveredGui == null) return;
        Slot hoveredSlot = ((GuiContainerAccessor) lastHoveredGui).getHoveredSlot();
        if (lastHoveredSlot != null && (hoveredSlot == null || hoveredSlot != lastHoveredSlot)) {
            // last slot is no longer hovered
            startAnimation(lastHoveredSlot, false);
        }
        if (hoveredSlot != null) {
            lastHoveredSlot = hoveredSlot;
            if (!isAnimating(hoveredSlot)) {
                // started hovering
                startAnimation(hoveredSlot, true);
            }
        } else {
            lastHoveredSlot = null;
        }
    }

    public static float getRenderScale(GuiContainer gui, Slot slot) {
        if (lastHoveredGui != gui || !isAnimating(slot) || NEAConfig.isBlacklisted(gui)) return 1f;
        float min = 1f, max = 1.25f;
        long slotTime = hoveredSlots.getLong(slot);
        float val = (NEA.time() - Math.abs(slotTime)) / (float) NEAConfig.hoverAnimationTime;
        if (slotTime < 0) {
            // negative time means slot is no longer hovered
            val = 1f - val;
            if (val <= 0) {
                // animation ended
                hoveredSlots.removeLong(slot);
                return 1f;
            }
        } else if (val >= 1f) {
            return max;
        }
        return NEAConfig.hoverAnimationCurve.interpolate(min, max, val);
    }
}
