package com.cleanroommc.neverenoughanimations.core.mixin;

import com.cleanroommc.neverenoughanimations.IItemLocation;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Slot.class)
public abstract class SlotMixin implements IItemLocation {

    @Shadow public int xPos;

    @Shadow public int yPos;

    @Shadow public int slotNumber;

    @Shadow public abstract ItemStack getStack();

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
        return slotNumber;
    }

    @Override
    public ItemStack nea$getStack() {
        return getStack();
    }
}
