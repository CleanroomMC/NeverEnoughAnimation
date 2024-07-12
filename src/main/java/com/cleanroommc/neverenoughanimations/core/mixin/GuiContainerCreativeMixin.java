package com.cleanroommc.neverenoughanimations.core.mixin;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiContainerCreative.class)
public abstract class GuiContainerCreativeMixin extends GuiContainer {

    private GuiContainerCreativeMixin(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Inject(method = "setCurrentCreativeTab", at = @At("TAIL"))
    public void assignSlotNumbers(CreativeTabs tab, CallbackInfo ci) {
        List<Slot> slots = inventorySlots.inventorySlots;
        for (int i = 0; i < slots.size(); i++) {
            slots.get(i).slotNumber = i;
        }
    }
}
