package com.cleanroommc.neverenoughanimations.core.mixin;

import com.cleanroommc.neverenoughanimations.IItemLocation;
import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import com.cleanroommc.neverenoughanimations.animations.ItemHoverAnimation;
import com.cleanroommc.neverenoughanimations.animations.ItemMoveAnimation;
import com.cleanroommc.neverenoughanimations.animations.ItemPickupThrowAnimation;
import com.cleanroommc.neverenoughanimations.animations.OpeningAnimation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// lower priority than 1000 so that we are earlier than item borders mod
@Mixin(value = AbstractContainerScreen.class, priority = 900)
public abstract class AbstractContainerScreenMixin extends Screen {

    protected AbstractContainerScreenMixin(Component pTitle) {
        super(pTitle);
    }

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    public void injectVirtualStack(GuiGraphics pGuiGraphics, Slot pSlot, CallbackInfo ci,
                                   @Local(ordinal = 0) LocalRef<ItemStack> itemStack) {
        if (NEAConfig.moveAnimationTime > 0) {
            ItemStack virtualStack = ItemMoveAnimation.getVirtualStack((AbstractContainerScreen) (Object) this, pSlot);
            if (virtualStack != null) {
                itemStack.set(virtualStack);
            }
        }
    }

    @Inject(method = "renderSlotContents", at = @At("HEAD"))
    public void injectHoverScale(GuiGraphics graphics, ItemStack itemstack, Slot slot, String countString, CallbackInfo ci) {
        if (NEAConfig.hoverAnimationTime > 0 && slot.isHighlightable()) {
            graphics.pose().pushPose();
            float scale = ItemHoverAnimation.getRenderScale((AbstractContainerScreen<?>) (Object) this, slot);
            if (scale > 1f) {
                int x = slot.x + 8;
                int y = slot.y + 8;
                graphics.pose().translate(x, y, 0);
                graphics.pose().scale(scale, scale, 1);
                graphics.pose().translate(-x, -y, 0);
            }
        }
    }

    @Inject(method = "renderSlotContents", at = @At(value = "TAIL"))
    public void endHoverScale(GuiGraphics graphics, ItemStack itemstack, Slot slot, String countString, CallbackInfo ci) {
        if (NEAConfig.hoverAnimationTime > 0 && slot.isHighlightable()) {
            // pop the stack here so that the item borders mod is not affected
            graphics.pose().popPose();
        }
    }

    @Inject(method = "renderSlotHighlight(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;IIF)V",
            at = @At("HEAD"),
            cancellable = true)
    public void dontDrawOverlay(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!((Object) this instanceof CreativeModeInventoryScreen) && (!slot.isHighlightable() || !NEAConfig.itemHoverOverlay)) ci.cancel();
    }

    @Inject(method = "render",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", shift = At.Shift.BEFORE))
    public void drawMovingItems(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick, CallbackInfo ci) {
        ItemPickupThrowAnimation.drawIndependentAnimations((AbstractContainerScreen<?>) (Object) this, pGuiGraphics, font);
        ItemMoveAnimation.drawAnimations(pGuiGraphics, font);
    }

    @ModifyArg(method = "render",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderFloatingItem(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
                        ordinal = 0),
               index = 1)
    public ItemStack injectVirtualCursorStack(ItemStack stack) {
        if (NEAConfig.moveAnimationTime > 0) {
            ItemStack virtual = ItemMoveAnimation.getVirtualStack((AbstractContainerScreen<?>) (Object) this, IItemLocation.CURSOR);
            return virtual == null ? stack : virtual;
        }
        return stack;
    }

    @Inject(method = "renderBackground",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V",
                     shift = At.Shift.BEFORE))
    public void injectRenderScale(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        NEA.drawScreenDebug(guiGraphics, (AbstractContainerScreen<?>) (Object) this, mouseX, mouseY);
        if (NEAConfig.moveAnimationTime > 0) {
            OpeningAnimation.handleScale(guiGraphics, (AbstractContainerScreen<?>) (Object) this, true);
        }
    }
}
