package com.cleanroommc.neverenoughanimations.animations;

import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.jetbrains.annotations.ApiStatus;

public class ItemHoverAnimation {

    private static AbstractContainerScreen<?> lastHoveredGui = null;
    private static Slot lastHoveredSlot = null;
    private static final Object2LongOpenHashMap<Slot> hoveredSlots = new Object2LongOpenHashMap<>(32);

    @ApiStatus.Internal
    public static void onGuiOpen(ScreenEvent.Opening event) {
        if (NEAConfig.hoverAnimationTime > 0) {
            if (!(event.getNewScreen() instanceof AbstractContainerScreen<?>)) {
                onGuiClose();
                return;
            }
            if (!NEAConfig.isBlacklisted(event.getNewScreen())) {
                lastHoveredGui = (AbstractContainerScreen<?>) event.getNewScreen();
                lastHoveredSlot = null;
                hoveredSlots.clear();
            }
        }
    }

    public static void onGuiClose() {
        lastHoveredGui = null;
        lastHoveredSlot = null;
        hoveredSlots.clear();
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
        Slot hoveredSlot = lastHoveredGui.getSlotUnderMouse();
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

    public static float getRenderScale(AbstractContainerScreen<?> gui, Slot slot) {
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
