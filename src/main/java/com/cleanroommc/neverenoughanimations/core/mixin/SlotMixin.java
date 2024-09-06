package com.cleanroommc.neverenoughanimations.core.mixin;

import com.cleanroommc.neverenoughanimations.IItemLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Slot.class)
public abstract class SlotMixin implements IItemLocation {

    @Shadow public abstract ItemStack getItem();

    @Shadow @Final public int x;

    @Shadow @Final public int y;

    @Shadow public int index;

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
        return index;
    }

    @Override
    public ItemStack nea$getStack() {
        return getItem();
    }
}
