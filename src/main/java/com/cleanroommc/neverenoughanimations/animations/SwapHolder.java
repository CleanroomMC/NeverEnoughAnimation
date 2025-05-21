package com.cleanroommc.neverenoughanimations.animations;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import com.cleanroommc.neverenoughanimations.api.IItemLocation;
import com.cleanroommc.neverenoughanimations.util.Platform;

public class SwapHolder {

    public static final SwapHolder INSTANCE = new SwapHolder();

    private Slot targetSlot;
    private Slot hotbarSlot;
    private ItemStack targetStack = ItemMoveAnimation.NULL_MARKER;
    private ItemStack hotbarStack = ItemMoveAnimation.NULL_MARKER;

    public boolean init(Slot hoveredSlot, List<Slot> slots, int hotbarIndex) {
        if (NEAConfig.isBlacklisted(Minecraft.getMinecraft().currentScreen)) return false;
        this.targetSlot = hoveredSlot;
        this.hotbarSlot = findHotbarSlot(slots, hotbarIndex);
        if (this.hotbarSlot == null || this.hotbarSlot == this.targetSlot) {
            reset();
            return false;
        }
        this.targetStack = this.targetSlot.getStack();
        this.hotbarStack = this.hotbarSlot.getStack();
        if (Platform.isStackEmpty(this.targetStack) && Platform.isStackEmpty(this.hotbarStack)) {
            reset();
            return false;
        }
        this.targetStack = Platform.copyStack(this.targetStack);
        this.hotbarStack = Platform.copyStack(this.hotbarStack);
        return true;
    }

    public void performSwap() {
        if (this.hotbarSlot == null || this.targetSlot == null
            || this.hotbarStack == ItemMoveAnimation.NULL_MARKER
            || this.targetStack == ItemMoveAnimation.NULL_MARKER) {
            reset();
            return;
        }
        IItemLocation hotbar = IItemLocation.of(this.hotbarSlot);
        IItemLocation hovering = IItemLocation.of(this.targetSlot);
        long time = NEA.time();
        if (Platform.isStackEmpty(this.targetStack)) {
            if (!Platform.isStackEmpty(hovering.nea$getStack())) {
                ItemMoveAnimation.queueAnimation(
                    hotbar.nea$getSlotNumber(),
                    new ItemMovePacket(time, hotbar, hovering, Platform.copyStack(hovering.nea$getStack())));
                ItemMoveAnimation.updateVirtualStack(hovering.nea$getSlotNumber(), Platform.EMPTY_STACK, 1);
            }
        } else if (Platform.isStackEmpty(this.hotbarStack)) {
            if (!Platform.isStackEmpty(hotbar.nea$getStack())) {
                ItemMoveAnimation.queueAnimation(
                    hovering.nea$getSlotNumber(),
                    new ItemMovePacket(
                        time,
                        hovering,
                        hotbar,
                        Platform.copyStack(
                            hotbar.nea$getStack()
                                .copy())));
                ItemMoveAnimation.updateVirtualStack(hotbar.nea$getSlotNumber(), Platform.EMPTY_STACK, 1);
            }
        } else if (this.targetSlot.inventory instanceof InventoryPlayer) {
            // only swap items if we are in player inventory
            ItemMoveAnimation.queueAnimation(
                hotbar.nea$getSlotNumber(),
                new ItemMovePacket(time, hotbar, hovering, Platform.copyStack(hovering.nea$getStack())));
            ItemMoveAnimation.queueAnimation(
                hovering.nea$getSlotNumber(),
                new ItemMovePacket(time, hovering, hotbar, Platform.copyStack(hotbar.nea$getStack())));
            ItemMoveAnimation.updateVirtualStack(hovering.nea$getSlotNumber(), Platform.EMPTY_STACK, 1);
            ItemMoveAnimation.updateVirtualStack(hotbar.nea$getSlotNumber(), Platform.EMPTY_STACK, 1);
        } else {
            // otherwise move to hotbar
            // TODO animate reinsertion of previous hotbar slot item
            ItemMoveAnimation.queueAnimation(
                hovering.nea$getSlotNumber(),
                new ItemMovePacket(
                    time,
                    hovering,
                    hotbar,
                    Platform.copyStack(
                        hotbar.nea$getStack()
                            .copy())));
            ItemMoveAnimation.updateVirtualStack(hotbar.nea$getSlotNumber(), Platform.EMPTY_STACK, 1);
        }
        reset();
    }

    public void reset() {
        this.targetSlot = null;
        this.hotbarSlot = null;
        this.targetStack = ItemMoveAnimation.NULL_MARKER;
        this.hotbarStack = ItemMoveAnimation.NULL_MARKER;
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
            if (slot.inventory instanceof InventoryPlayer/*
                                                          * || (slot instanceof SlotItemHandler slotItemHandler &&
                                                          * (slotItemHandler.getItemHandler() instanceof
                                                          * PlayerMainInvWrapper || slotItemHandler.getItemHandler()
                                                          * instanceof PlayerInvWrapper))
                                                          */) {
                return slot;
            }
        }
        return null;
    }
}
