package com.cleanroommc.neverenoughanimations.api;

import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.core.mixin.early.GuiContainerAccessor;
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
                guiX = ((GuiContainerAccessor) container).getGuiLeft();
            }
            return NEA.getMouseX() - 8 - guiX;
        }

        @Override
        public int nea$getY() {
            int guiY = 0;
            if (Minecraft.getMinecraft().currentScreen instanceof GuiContainer container) {
                guiY = ((GuiContainerAccessor) container).getGuiTop();
            }
            return NEA.getMouseY() - 8 - guiY;
        }

        @Override
        public int nea$getSlotNumber() {
            return -1;
        }

        @Override
        public ItemStack nea$getStack() {
            return Minecraft.getMinecraft().thePlayer.inventory.getItemStack();
        }
    };

    class Impl implements IItemLocation {

        private final int x, y;
        private final int slotNumber;
        private final ItemStack stack;

        public Impl(int x, int y, ItemStack stack) {
            this(x, y, -2, stack);
        }

        public Impl(int x, int y, int slotNumber, ItemStack stack) {
            this.x = x;
            this.y = y;
            this.slotNumber = slotNumber;
            this.stack = stack;
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
            return slotNumber;
        }

        @Override
        public ItemStack nea$getStack() {
            return stack;
        }
    }
}
