package com.cleanroommc.neverenoughanimations.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class Platform {

    public static final ItemStack EMPTY_STACK = null;

    public static boolean isStackEmpty(ItemStack stack) {
        return stack == null || stack.getItem() == null || stack.stackSize <= 0;
    }

    public static ItemStack copyStack(ItemStack stack) {
        return isStackEmpty(stack) ? EMPTY_STACK : stack.copy();
    }

    public static int getCount(ItemStack stack) {
        return isStackEmpty(stack) ? 0 : stack.stackSize;
    }

    public static ItemStack setCount(ItemStack stack, int count) {
        if (!isStackEmpty(stack)) {
            stack.stackSize = count;
        }
        return stack;
    }

    public static ItemStack shrink(ItemStack stack, int count) {
        return grow(stack, -count);
    }

    public static ItemStack grow(ItemStack stack, int count) {
        if (!isStackEmpty(stack)) {
            stack.stackSize += count;
        }
        return stack;
    }

    public static boolean canItemStacksStack(@Nullable ItemStack a, @Nullable ItemStack b) {
        if (a != null && b != null && a.isItemEqual(b) && a.hasTagCompound() == b.hasTagCompound()) {
            return (!a.hasTagCompound() || a.getTagCompound().equals(b.getTagCompound()));
        } else {
            return false;
        }
    }

    public static void drawItem(RenderItem renderItem, ItemStack stack, int x, int y, FontRenderer fontRenderer) {
        if (isStackEmpty(stack)) return;
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = fontRenderer;
        renderItem.renderItemAndEffectIntoGUI(font, Minecraft.getMinecraft().getTextureManager(), stack, x, y);
        renderItem.renderItemOverlayIntoGUI(font, Minecraft.getMinecraft().getTextureManager(), stack, x, y);
        // see RenderItem#L627
        if (stack.hasEffect(0)) {
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

}
