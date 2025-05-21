package com.cleanroommc.neverenoughanimations.core.mixin.early;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.cleanroommc.neverenoughanimations.api.IItemLocation;

@Mixin(Slot.class)
public abstract class SlotMixin implements IItemLocation {

    @Shadow
    public int xDisplayPosition;

    @Shadow
    public int yDisplayPosition;

    @Shadow
    public int slotNumber;

    @Shadow
    public abstract ItemStack getStack();

    @Override
    public int nea$getX() {
        return xDisplayPosition;
    }

    @Override
    public int nea$getY() {
        return yDisplayPosition;
    }

    @Override
    public int nea$getSlotNumber() {
        return slotNumber;
    }

    @Override
    public ItemStack nea$getStack() {
        return getStack();
    }
}
