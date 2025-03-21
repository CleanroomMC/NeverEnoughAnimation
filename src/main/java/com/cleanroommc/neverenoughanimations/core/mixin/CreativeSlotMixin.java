package com.cleanroommc.neverenoughanimations.core.mixin;

import com.cleanroommc.neverenoughanimations.api.IItemLocation;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.client.gui.inventory.GuiContainerCreative$CreativeSlot")
public abstract class CreativeSlotMixin extends Slot implements IItemLocation {

    @Shadow
    @Final
    private Slot slot;

    private CreativeSlotMixin(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public int nea$getX() {
        return xPos;
    }

    @Override
    public int nea$getY() {
        return yPos;
    }

    @Override
    public int nea$getSlotNumber() {
        return slot.slotNumber;
    }

    @Override
    public ItemStack nea$getStack() {
        return slot.getStack();
    }
}
