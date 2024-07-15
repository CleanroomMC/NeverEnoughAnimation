package com.cleanroommc.neverenoughanimations.animations;

import com.cleanroommc.neverenoughanimations.IItemLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import java.util.List;

public class SwapHolder {

    public static final SwapHolder INSTANCE = new SwapHolder();

    private Slot targetSlot;
    private Slot hotbarSlot;
    private ItemStack targetStack;
    private ItemStack hotbarStack;

    public boolean init(Slot hoveredSlot, List<Slot> slots, int hotbarIndex) {
        this.targetSlot = hoveredSlot;
        this.hotbarSlot = findHotbarSlot(slots, hotbarIndex);
        if (this.hotbarSlot == null) {
            reset();
            return false;
        }
        this.targetStack = this.targetSlot.getStack();
        this.hotbarStack = this.hotbarSlot.getStack();
        if (this.targetStack.isEmpty() && this.hotbarStack.isEmpty()) {
            reset();
            return false;
        }
        this.targetStack = this.targetStack.copy();
        this.hotbarStack = this.hotbarStack.copy();
        return true;
    }

    public void performSwap() {
        IItemLocation hotbar = IItemLocation.of(this.hotbarSlot);
        IItemLocation hovering = IItemLocation.of(this.targetSlot);
        long time = Minecraft.getSystemTime();
        if (this.targetStack.isEmpty()) {
            if (!hovering.nea$getStack().isEmpty()) {
                ItemMoveAnimation.queueAnimation(hotbar.nea$getSlotNumber(),
                                                 new ItemMovePacket(time, hotbar, hovering, hovering.nea$getStack().copy()));
                ItemMoveAnimation.updateVirtualStack(hovering.nea$getSlotNumber(), ItemStack.EMPTY, 1);
            }
        } else if (this.hotbarStack.isEmpty()) {
            if (!hotbar.nea$getStack().isEmpty()) {
                ItemMoveAnimation.queueAnimation(hovering.nea$getSlotNumber(),
                                                 new ItemMovePacket(time, hovering, hotbar, hotbar.nea$getStack().copy()));
                ItemMoveAnimation.updateVirtualStack(hotbar.nea$getSlotNumber(), ItemStack.EMPTY, 1);
            }
        } else {
            ItemMoveAnimation.queueAnimation(hotbar.nea$getSlotNumber(),
                                             new ItemMovePacket(time, hotbar, hovering, hovering.nea$getStack().copy()));
            ItemMoveAnimation.queueAnimation(hovering.nea$getSlotNumber(),
                                             new ItemMovePacket(time, hovering, hotbar, hotbar.nea$getStack().copy()));
            ItemMoveAnimation.updateVirtualStack(hovering.nea$getSlotNumber(), ItemStack.EMPTY, 1);
            ItemMoveAnimation.updateVirtualStack(hotbar.nea$getSlotNumber(), ItemStack.EMPTY, 1);
        }
        reset();
    }

    public void reset() {
        this.targetSlot = null;
        this.hotbarSlot = null;
        this.targetStack = null;
        this.hotbarStack = null;
    }

    public ItemStack getTargetStack() {
        return targetStack;
    }

    public ItemStack getHotbarStack() {
        return hotbarStack;
    }

    public Slot getHotbarSlot() {
        return hotbarSlot;
    }

    public Slot getTargetSlot() {
        return targetSlot;
    }

    public static Slot findHotbarSlot(List<Slot> slots, int index) {
        for (Slot slot : slots) {
            if (slot.getSlotIndex() != index) continue;
            if (slot.inventory instanceof InventoryPlayer ||
                    (slot instanceof SlotItemHandler slotItemHandler &&
                            (slotItemHandler.getItemHandler() instanceof PlayerMainInvWrapper ||
                                    slotItemHandler.getItemHandler() instanceof PlayerInvWrapper))) {
                return slot;
            }
        }
        return null;
    }
}
