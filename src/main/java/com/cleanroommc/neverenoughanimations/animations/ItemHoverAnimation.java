package com.cleanroommc.neverenoughanimations.animations;

import com.cleanroommc.neverenoughanimations.IItemLocation;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.ApiStatus;

@SideOnly(Side.CLIENT)
public class ItemHoverAnimation {

    private static GuiContainer lastHoveredGui = null;
    private static int lastHoveredSlot = -1;
    private static final LongArrayList hoveredSlots = new LongArrayList(256);

    @ApiStatus.Internal
    public static void onGuiOpen(GuiOpenEvent event) {
        if (NEAConfig.hoverAnimationTime > 0) {
            if (!(event.getGui() instanceof GuiContainer)) {
                if (lastHoveredGui != null) {
                    lastHoveredGui = null;
                    lastHoveredSlot = -1;
                    hoveredSlots.clear();
                }
                return;
            }
            if (!NEAConfig.isBlacklisted(event.getGui())) {
                lastHoveredGui = (GuiContainer) event.getGui();
                lastHoveredSlot = -1;
                hoveredSlots.clear();
            }
        }
    }

    private static void startAnimation(int slot, boolean grow) {
        while (hoveredSlots.size() <= slot) {
            hoveredSlots.add(0);
        }
        hoveredSlots.set(slot, Minecraft.getSystemTime() * (grow ? 1 : -1));
    }

    public static boolean isAnimating(int slot) {
        return hoveredSlots.size() > slot && hoveredSlots.getLong(slot) != 0;
    }

    @ApiStatus.Internal
    public static void onGuiTick() {
        if (NEAConfig.hoverAnimationTime == 0 || lastHoveredGui == null) return;
        IItemLocation hoveredSlot = IItemLocation.of(lastHoveredGui.getSlotUnderMouse());
        if (lastHoveredSlot >= 0 && (hoveredSlot == null || hoveredSlot.nea$getSlotNumber() != lastHoveredSlot)) {
            // last slot is no longer hovered
            startAnimation(lastHoveredSlot, false);
        }
        if (hoveredSlot != null) {
            lastHoveredSlot = hoveredSlot.nea$getSlotNumber();
            if (!isAnimating(hoveredSlot.nea$getSlotNumber())) {
                // started hovering
                startAnimation(hoveredSlot.nea$getSlotNumber(), true);
            }
        } else {
            lastHoveredSlot = -1;
        }
    }

    public static float getRenderScale(GuiContainer gui, int slot) {
        if (lastHoveredGui != gui ||
                slot >= hoveredSlots.size() ||
                !isAnimating(slot) ||
                NEAConfig.isBlacklisted(gui)) return 1f;
        float min = 1f, max = 1.25f;
        float slotTime = hoveredSlots.getLong(slot);
        float val = (Minecraft.getSystemTime() - Math.abs(slotTime)) / (float) NEAConfig.hoverAnimationTime;
        if (slotTime < 0) {
            // negative time means slot is no longer hovered
            val = 1f - val;
            if (val <= 0) {
                // animation ended
                hoveredSlots.set(slot, 0);
                return 1f;
            }
        } else if (val >= 1f) {
            return max;
        }
        return NEAConfig.hoverAnimationCurve.interpolate(min, max, val);
    }
}
