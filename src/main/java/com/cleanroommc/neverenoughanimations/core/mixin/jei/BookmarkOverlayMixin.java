package com.cleanroommc.neverenoughanimations.core.mixin.jei;

import com.cleanroommc.neverenoughanimations.animations.OpeningAnimation;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BookmarkOverlay.class, remap = false)
public abstract class BookmarkOverlayMixin {

    @Shadow
    @Final
    private IngredientGridWithNavigation contents;

    @Inject(method = "drawScreen", at = @At("HEAD"))
    public void drawScreenPre(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        guiGraphics.pose().pushPose();
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof AbstractContainerScreen<?> container) {
            float val = 1f - OpeningAnimation.getValue(container);
            if (val <= 0f) return;
            guiGraphics.pose().translate(-contents.getBackgroundArea().width() * val, 0, 0);
        }
    }

    @Inject(method = "drawScreen", at = @At("TAIL"))
    public void drawScreenPost(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        guiGraphics.pose().popPose();
    }
}
