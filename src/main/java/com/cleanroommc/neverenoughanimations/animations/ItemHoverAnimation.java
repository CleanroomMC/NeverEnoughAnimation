package com.cleanroommc.neverenoughanimations.animations;

import com.cleanroommc.neverenoughanimations.NEAConfig;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.ApiStatus;

@SideOnly(Side.CLIENT)
public class ItemHoverAnimation {

    private static GuiContainer lastHoveredGui = null;
    private static int lastHoveredSlot = -1;
    private static final LongArrayList hoveredSlots = new LongArrayList(32);

    @SideOnly(Side.CLIENT)
    @ApiStatus.Internal
    public static void onGuiTick(GuiScreen current) {
        if (NEAConfig.hoverAnimationTime > 0) {
            if (!(current instanceof GuiContainer)) {
                if (lastHoveredGui != null) {
                    lastHoveredGui = null;
                    lastHoveredSlot = -1;
                    for (int i = 0; i < hoveredSlots.size(); i++) hoveredSlots.set(i, -1);
                    hoveredSlots.clear();
                }
                return;
            }
            if (current != lastHoveredGui) {
                lastHoveredGui = (GuiContainer) current;
                lastHoveredSlot = -1;
                for (int i = 0; i < hoveredSlots.size(); i++) hoveredSlots.set(i, -1);
                hoveredSlots.clear();
            }
            Slot hoveredSlot = lastHoveredGui.getSlotUnderMouse();
            if (lastHoveredSlot >= 0 && (hoveredSlot == null || hoveredSlot.slotNumber != lastHoveredSlot)) {
                // last slot is no longer hovered
                hoveredSlots.set(lastHoveredSlot, -Minecraft.getSystemTime());
            }
            if (hoveredSlot != null) {
                lastHoveredSlot = hoveredSlot.slotNumber;
                if (hoveredSlots.size() <= hoveredSlot.slotNumber) {
                    // increase storage size
                    int n = lastHoveredGui.inventorySlots.inventorySlots.size();
                    while (n <= hoveredSlot.slotNumber) n += 16;
                    hoveredSlots.size(n);
                }
                if (hoveredSlots.getLong(hoveredSlot.slotNumber) == 0) {
                    // started hovering
                    hoveredSlots.set(hoveredSlot.slotNumber, Minecraft.getSystemTime());
                }
            } else {
                lastHoveredSlot = -1;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static float getRenderScale(GuiContainer gui, int slot) {
        if (lastHoveredGui != gui || slot >= hoveredSlots.size() || hoveredSlots.getLong(slot) == 0) return 1f;
        float min = 1f, max = 1.25f;
        float slotTime = hoveredSlots.getLong(slot);
        float time = Math.min(NEAConfig.hoverAnimationTime, Minecraft.getSystemTime() - Math.abs(slotTime));
        if (slotTime < 0) {
            // negative time means slot is no longer hovered
            time = NEAConfig.hoverAnimationTime - time;
            if (time == 0) {
                // animation ended
                hoveredSlots.set(slot, 0);
                return 1f;
            }
        }
        float val = time / NEAConfig.hoverAnimationTime;
        return NEAConfig.hoverAnimationCurve.interpolate(min, max, val);
    }
}
