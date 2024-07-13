package com.cleanroommc.neverenoughanimations;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public interface IItemLocation {

    static IItemLocation of(Slot slot) {
        return (IItemLocation) slot;
    }

    int nea$getX();

    int nea$getY();

    int nea$getSlotNumber();

    ItemStack nea$getStack();
}
