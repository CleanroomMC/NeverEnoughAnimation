package com.cleanroommc.neverenoughanimations.core.mixin.early;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import com.cleanroommc.neverenoughanimations.animations.ItemHoverAnimation;
import com.cleanroommc.neverenoughanimations.animations.ItemMoveAnimation;
import com.cleanroommc.neverenoughanimations.animations.ItemPickupThrowAnimation;
import com.cleanroommc.neverenoughanimations.api.IAnimatedScreen;
import com.cleanroommc.neverenoughanimations.api.IItemLocation;
import com.cleanroommc.neverenoughanimations.util.GlStateManager;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

@Mixin(value = GuiContainer.class, priority = 950)
public abstract class GuiContainerMixin extends GuiScreen implements IAnimatedScreen {

    @Shadow
    protected int guiLeft;

    @Shadow
    protected int guiTop;

    @Shadow
    protected int xSize;

    @Shadow
    protected int ySize;

    @Inject(
        method = "func_146977_a",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderItem;zLevel:F", ordinal = 0))
    public void injectVirtualStack(Slot slotIn, CallbackInfo ci, @Local(ordinal = 0) LocalRef<ItemStack> itemStack) {
        if (NEAConfig.moveAnimationTime > 0) {
            ItemStack virtualStack = ItemMoveAnimation.getVirtualStack((GuiContainer) (Object) this, slotIn);
            if (virtualStack != ItemMoveAnimation.NULL_MARKER) {
                itemStack.set(virtualStack);
            }
        }
    }

    /*@Inject(
        method = "func_146977_a",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemAndEffectIntoGUI(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/item/ItemStack;II)V",
            shift = At.Shift.NONE))
    public void injectHoverScale(Slot slotIn, CallbackInfo ci, @Share("scale") LocalFloatRef scaleRef) {
        // this only works while NEI is not loaded
        if (NEAConfig.hoverAnimationTime > 0) {
            GlStateManager.pushMatrix();
            float scale = ItemHoverAnimation.getRenderScale((GuiContainer) (Object) this, slotIn);
            scaleRef.set(scale);
            if (scale > 1f) {
                int x = slotIn.xDisplayPosition + 8;
                int y = slotIn.yDisplayPosition + 8;
                GlStateManager.translate(x, y, 0);
                GlStateManager.scale(scale, scale, 1);
                GlStateManager.translate(-x, -y, 0);
            }
        }
    }

    @Inject(
        method = "func_146977_a",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemAndEffectIntoGUI(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/item/ItemStack;II)V",
            shift = At.Shift.AFTER))
    public void midHoverScale(Slot slotIn, CallbackInfo ci) {
        // this only works while NEI is not loaded
        if (NEA.isItemBordersLoaded() && NEAConfig.hoverAnimationTime > 0) {
            // itemborders wants to draw the borders now -> undo scale
            GlStateManager.popMatrix();
        }
    }

    @Inject(
        method = "func_146977_a",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemOverlayIntoGUI(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            shift = At.Shift.AFTER))
    public void endHoverScale(Slot slotIn, CallbackInfo ci, @Share("scale") LocalFloatRef scaleRef) {
        // this only works while NEI is not loaded
        if (NEAConfig.hoverAnimationTime == 0) return;
        if (NEA.isItemBordersLoaded()) {
            // itemborders did draw its borders -> reapply scale
            GlStateManager.pushMatrix();
            float scale = scaleRef.get();
            if (scale > 1f) {
                int x = slotIn.xDisplayPosition + 8;
                int y = slotIn.yDisplayPosition + 8;
                GlStateManager.translate(x, y, 0);
                GlStateManager.scale(scale, scale, 1);
                GlStateManager.translate(-x, -y, 0);
            }
        }
        GlStateManager.popMatrix();
    }*/

    @Redirect(
        method = "drawScreen",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGradientRect(IIIIII)V"))
    public void dontDrawOverlay(GuiContainer instance, int i1, int i2, int i3, int i4, int i5, int i6) {
        if (NEAConfig.itemHoverOverlay) {
            drawGradientRect(i1, i2, i3, i4, i5, i6);
        }
    }

    @Inject(
        method = "drawScreen",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V", remap = false))
    public void drawMovingItems(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        zLevel = 200;
        itemRender.zLevel = 200;
        ItemPickupThrowAnimation.drawIndependentAnimations((GuiContainer) (Object) this, itemRender, fontRendererObj);
        ItemMoveAnimation.drawAnimations(itemRender, fontRendererObj);
        itemRender.zLevel = 0;
        zLevel = 0;
    }

    @ModifyArg(
        method = "drawScreen",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawItemStack(Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            ordinal = 0),
        index = 0)
    public ItemStack injectVirtualCursorStack(ItemStack stack) {
        if (NEAConfig.moveAnimationTime > 0) {
            ItemStack virtual = ItemMoveAnimation.getVirtualStack((GuiContainer) (Object) this, IItemLocation.CURSOR);
            return virtual == ItemMoveAnimation.NULL_MARKER ? stack : virtual;
        }
        return stack;
    }

    @Override
    public int nea$getX() {
        return guiLeft;
    }

    @Override
    public int nea$getY() {
        return guiTop;
    }

    @Override
    public int nea$getWidth() {
        return xSize;
    }

    @Override
    public int nea$getHeight() {
        return ySize;
    }
}
