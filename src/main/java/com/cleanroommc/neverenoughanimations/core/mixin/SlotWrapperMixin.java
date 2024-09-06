package com.cleanroommc.neverenoughanimations.core.mixin;

import com.cleanroommc.neverenoughanimations.IItemLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$SlotWrapper")
public abstract class SlotWrapperMixin extends Slot implements IItemLocation {

    @Shadow @Final Slot target;

    @Shadow public abstract @NotNull ItemStack getItem();

    public SlotWrapperMixin(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public int nea$getX() {
        return x;
    }

    @Override
    public int nea$getY() {
        return y;
    }

    @Override
    public int nea$getSlotNumber() {
        return target.index;
    }

    @Override
    public ItemStack nea$getStack() {
        return getItem();
    }
}
