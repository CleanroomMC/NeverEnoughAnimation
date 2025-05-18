package com.cleanroommc.neverenoughanimations;

import codechicken.nei.ItemStackSet;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.util.ReadableNumberConverter;
import com.cleanroommc.neverenoughanimations.animations.ItemHoverAnimation;
import com.cleanroommc.neverenoughanimations.util.GlStateManager;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import static codechicken.lib.gui.GuiDraw.fontRenderer;
import static codechicken.lib.gui.GuiDraw.renderEngine;

public class NEICompat {

    private static final ItemStackSet renderingErrorItems = new ItemStackSet();

    // NEI is so invasive that replacing it is the only option
    public static void safeItemRenderContext(ItemStack stack, int x, int y, FontRenderer fontRenderer,
                                              Runnable callback) {
        float zLevel = GuiContainerManager.drawItems.zLevel += 100F;
        GuiContainerManager.enableMatrixStackLogging();
        GuiContainerManager.enable3DRender();

        try {
            if (renderingErrorItems.contains(stack)) {
                GuiContainerManager.drawItems.renderItemIntoGUI(fontRenderer, renderEngine, new ItemStack(Blocks.fire), x, y);
            } else {
                callback.run();
            }

            if (!GuiContainerManager.checkMatrixStack()) throw new IllegalStateException("Modelview matrix stack too deep");
            //if (Tessellator.instance.isDrawing) throw new IllegalStateException("Still drawing");
        } catch (Exception e) {
            System.err.println("Error while rendering: " + stack + " (" + e.getMessage() + ")");
            e.printStackTrace();

            GuiContainerManager.restoreMatrixStack();
            //if (Tessellator.instance.isDrawing) Tessellator.instance.draw();

            GuiContainerManager.drawItems.zLevel = zLevel;
            GuiContainerManager.drawItems.renderItemIntoGUI(fontRenderer, renderEngine, new ItemStack(Blocks.fire), x, y);
            renderingErrorItems.add(stack);
        }

        GuiContainerManager.enable2DRender();
        GuiContainerManager.disableMatrixStackLogging();
        GuiContainerManager.drawItems.zLevel = zLevel - 100;
    }

    public static void renderItem(GuiContainer container, Slot slotIn, int offsetX, int offsetY, ItemStack itemstack, FontRenderer fontRenderer,
                                  boolean smallAmount, String quantity) {

        safeItemRenderContext(itemstack, offsetX, offsetY, fontRenderer, () -> {
            float scale = smallAmount ? 0.5f : 1f;
            String stackSize = quantity;

            if (stackSize == null) {
                if (itemstack.stackSize > 1) {
                    stackSize = ReadableNumberConverter.INSTANCE.toWideReadableForm(itemstack.stackSize);

                    if (stackSize.length() == 3) {
                        scale = 0.8f;
                    } else if (stackSize.length() == 4) {
                        scale = 0.6f;
                    } else if (stackSize.length() > 4) {
                        scale = 0.5f;
                    }

                } else {
                    stackSize = "";
                }
            }

            float nea$scale = 1f;
            if (NEAConfig.hoverAnimationTime > 0) {
                GlStateManager.pushMatrix();
                nea$scale = ItemHoverAnimation.getRenderScale(container, slotIn);
                if (nea$scale > 1f) {
                    int x = slotIn.xDisplayPosition + 8;
                    int y = slotIn.yDisplayPosition + 8;
                    GlStateManager.translate(x, y, 0);
                    GlStateManager.scale(nea$scale, nea$scale, 1);
                    GlStateManager.translate(-x, -y, 0);
                }
            }
            GuiContainerManager.drawItems.renderItemAndEffectIntoGUI(fontRenderer, renderEngine, itemstack, offsetX, offsetY);

            if (scale != 1f && !stackSize.isEmpty()) {
                drawBigStackSize(offsetX, offsetY, stackSize, scale);
                stackSize = "";
            }

            GuiContainerManager.drawItems.renderItemOverlayIntoGUI(fontRenderer, renderEngine, itemstack, offsetX, offsetY, stackSize);
            if (NEAConfig.hoverAnimationTime > 0) {
                GlStateManager.popMatrix();
            }
        });
    }

    // copy from appeng.client.render.AppEngRenderItem
    protected static void drawBigStackSize(int offsetX, int offsetY, String stackSize, float scale) {
        final float inverseScaleFactor = 1.0f / scale;

        GuiContainerManager.enable2DRender();
        GL11.glScaled(scale, scale, scale);

        final int X = (int) ((offsetX + 16.0f - fontRenderer.getStringWidth(stackSize) * scale) * inverseScaleFactor);
        final int Y = (int) ((offsetY + 16.0f - 7.0f * scale) * inverseScaleFactor);
        fontRenderer.drawStringWithShadow(stackSize, X, Y, 16777215);

        GL11.glScaled(inverseScaleFactor, inverseScaleFactor, inverseScaleFactor);
        GuiContainerManager.enable3DRender();
    }

}
