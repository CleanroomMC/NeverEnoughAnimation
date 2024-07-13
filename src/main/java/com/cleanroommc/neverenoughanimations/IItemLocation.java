package com.cleanroommc.neverenoughanimations;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
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

    IItemLocation CURSOR = new IItemLocation() {

        @Override
        public int nea$getX() {
            int guiX = 0;
            if (Minecraft.getMinecraft().currentScreen instanceof GuiContainer container) {
                guiX = container.getGuiLeft();
            }
            return NEA.getMouseX() - 8 - guiX;
        }

        @Override
        public int nea$getY() {
            int guiY = 0;
            if (Minecraft.getMinecraft().currentScreen instanceof GuiContainer container) {
                guiY = container.getGuiTop();
            }
            return NEA.getMouseY() - 8 - guiY;
        }

        @Override
        public int nea$getSlotNumber() {
            return -1;
        }

        @Override
        public ItemStack nea$getStack() {
            return Minecraft.getMinecraft().player.inventory.getItemStack();
        }
    };
}
