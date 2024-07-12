package com.cleanroommc.neverenoughanimations.core.mixin;

import com.cleanroommc.neverenoughanimations.NEAConfig;
import com.cleanroommc.neverenoughanimations.animations.ItemHoverAnimation;
import com.cleanroommc.neverenoughanimations.animations.ItemMoveAnimation;
import com.cleanroommc.neverenoughanimations.animations.OpeningAnimation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public class GuiContainerMixin extends GuiScreen {

    @Shadow protected int xSize;

    @Shadow protected int ySize;

    @Shadow protected int guiLeft;

    @Shadow protected int guiTop;

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 5, shift = At.Shift.BEFORE))
    public void injectVirtualStack(Slot slotIn, CallbackInfo ci, @Local(ordinal = 0) LocalRef<ItemStack> itemStack) {
        if (NEAConfig.moveAnimationTime > 0) {
            ItemStack virtualStack = ItemMoveAnimation.getVirtualStack((GuiContainer) (Object) this, slotIn);
            if (virtualStack != null) {
                itemStack.set(virtualStack);
            }
        }
    }

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderItem;renderItemAndEffectIntoGUI(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;II)V", shift = At.Shift.BEFORE))
    public void injectHoverScale(Slot slotIn, CallbackInfo ci) {
        if (NEAConfig.hoverAnimationTime > 0) {
            GlStateManager.pushMatrix();
            float scale = ItemHoverAnimation.getRenderScale((GuiContainer) (Object) this, slotIn.slotNumber);
            if (scale > 1f) {
                int x = slotIn.xPos;
                int y = slotIn.yPos;
                GlStateManager.translate(x + 8, y + 8, 0);
                GlStateManager.scale(scale, scale, 1);
                GlStateManager.translate(-x - 8, -y - 8, 0);
            }
        }
    }

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderItem;renderItemOverlayIntoGUI(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", shift = At.Shift.AFTER))
    public void endHoverScale(Slot slotIn, CallbackInfo ci) {
        if (NEAConfig.hoverAnimationTime == 0) return;
        GlStateManager.popMatrix();
    }

    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGradientRect(IIIIII)V"))
    public void dontDrawOverlay(GuiContainer instance, int i1, int i2, int i3, int i4, int i5, int i6) {
        if (NEAConfig.itemHoverOverlay) {
            drawGradientRect(i1, i2, i3, i4, i5, i6);
        }
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;popMatrix()V", shift = At.Shift.BEFORE))
    public void drawMovingItems(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        zLevel = 200;
        itemRender.zLevel = 200;
        ItemMoveAnimation.drawAnimations(itemRender, fontRenderer);
        itemRender.zLevel = 0;
        zLevel = 0;
    }

    @Inject(method = "drawScreen", at = @At(value = "HEAD"))
    public void drawOpeningAnimation(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (NEAConfig.openingAnimationTime > 0) {
            float scale = OpeningAnimation.getScale((GuiContainer) (Object) this);
            GlStateManager.pushMatrix();
            GlStateManager.translate(guiLeft, guiTop, 0);
            GlStateManager.translate(xSize / 2f, ySize / 2f, 0);
            GlStateManager.scale(scale, scale, 1f);
            GlStateManager.translate(-xSize / 2f, -ySize / 2f, 0);
            GlStateManager.translate(-guiLeft, -guiTop, 0);
            // GlStateManager.color(1f, 1f, 1f, scale);
        }
    }

    @Inject(method = "drawScreen", at = @At("TAIL"))
    public void endOpeningAnimation(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (NEAConfig.openingAnimationTime > 0) {
            GlStateManager.popMatrix();
            OpeningAnimation.checkGuiToClose();
        }
    }
}
