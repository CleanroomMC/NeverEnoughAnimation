package com.cleanroommc.neverenoughanimations.core.mixin;

import com.cleanroommc.neverenoughanimations.IItemLocation;
import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import com.cleanroommc.neverenoughanimations.animations.ItemHoverAnimation;
import com.cleanroommc.neverenoughanimations.animations.ItemMoveAnimation;
import com.cleanroommc.neverenoughanimations.animations.ItemPickupThrowAnimation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiContainer.class, priority = 950)
public class GuiContainerMixin extends GuiScreen {

    @Inject(method = "drawSlot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 5, shift = At.Shift.BEFORE))
    public void injectVirtualStack(Slot slotIn, CallbackInfo ci, @Local(ordinal = 0) LocalRef<ItemStack> itemStack) {
        if (NEAConfig.moveAnimationTime > 0) {
            ItemStack virtualStack = ItemMoveAnimation.getVirtualStack((GuiContainer) (Object) this, slotIn);
            if (virtualStack != null) {
                itemStack.set(virtualStack);
            }
        }
    }

    @Inject(method = "drawSlot",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/RenderItem;renderItemAndEffectIntoGUI(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;II)V",
                     shift = At.Shift.BEFORE))
    public void injectHoverScale(Slot slotIn, CallbackInfo ci, @Share("scale") LocalFloatRef scaleRef) {
        if (NEAConfig.hoverAnimationTime > 0) {
            GlStateManager.pushMatrix();
            float scale = ItemHoverAnimation.getRenderScale((GuiContainer) (Object) this, slotIn);
            scaleRef.set(scale);
            if (scale > 1f) {
                int x = slotIn.xPos + 8;
                int y = slotIn.yPos + 8;
                GlStateManager.translate(x, y, 0);
                GlStateManager.scale(scale, scale, 1);
                GlStateManager.translate(-x, -y, 0);
            }
        }
    }

    @Inject(method = "drawSlot",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/RenderItem;renderItemAndEffectIntoGUI(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;II)V",
                     shift = At.Shift.AFTER))
    public void midHoverScale(Slot slotIn, CallbackInfo ci) {
        if (NEA.isItemBordersLoaded() && NEAConfig.hoverAnimationTime > 0) {
            // itemborders wants to draw the borders now -> undo scale
            GlStateManager.popMatrix();
        }
    }


    @Inject(method = "drawSlot",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/RenderItem;renderItemOverlayIntoGUI(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
                     shift = At.Shift.AFTER))
    public void endHoverScale(Slot slotIn, CallbackInfo ci, @Share("scale") LocalFloatRef scaleRef) {
        if (NEAConfig.hoverAnimationTime == 0) return;
        if (NEA.isItemBordersLoaded()) {
            // itemborders did draw its borders -> reapply scale
            GlStateManager.pushMatrix();
            float scale = scaleRef.get();
            if (scale > 1f) {
                int x = slotIn.xPos + 8;
                int y = slotIn.yPos + 8;
                GlStateManager.translate(x, y, 0);
                GlStateManager.scale(scale, scale, 1);
                GlStateManager.translate(-x, -y, 0);
            }
        }
        GlStateManager.popMatrix();
    }

    @Redirect(method = "drawScreen",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGradientRect(IIIIII)V"))
    public void dontDrawOverlay(GuiContainer instance, int i1, int i2, int i3, int i4, int i5, int i6) {
        if (NEAConfig.itemHoverOverlay) {
            drawGradientRect(i1, i2, i3, i4, i5, i6);
        }
    }

    @Inject(method = "drawScreen",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;popMatrix()V", shift = At.Shift.BEFORE))
    public void drawMovingItems(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        zLevel = 200;
        itemRender.zLevel = 200;
        ItemPickupThrowAnimation.drawIndependentAnimations((GuiContainer) (Object) this, itemRender, fontRenderer);
        ItemMoveAnimation.drawAnimations(itemRender, fontRenderer);
        itemRender.zLevel = 0;
        zLevel = 0;
    }

    @ModifyArg(method = "drawScreen",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawItemStack(Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
                        ordinal = 0),
               index = 0)
    public ItemStack injectVirtualCursorStack(ItemStack stack) {
        if (NEAConfig.moveAnimationTime > 0) {
            ItemStack virtual = ItemMoveAnimation.getVirtualStack((GuiContainer) (Object) this, IItemLocation.CURSOR);
            return virtual == null ? stack : virtual;
        }
        return stack;
    }
}
