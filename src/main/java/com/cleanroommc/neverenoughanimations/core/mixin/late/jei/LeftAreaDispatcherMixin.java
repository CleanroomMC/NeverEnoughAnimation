package com.cleanroommc.neverenoughanimations.core.mixin.late.jei;
/*
import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.animations.OpeningAnimation;
import mezz.jei.config.Config;
import mezz.jei.gui.overlay.bookmarks.LeftAreaDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(value = LeftAreaDispatcher.class, remap = false)
public abstract class LeftAreaDispatcherMixin {

    @Shadow private Rectangle displayArea;

    @Shadow private boolean canShow;

    @Shadow protected abstract boolean hasContent();

    @Inject(method = "drawScreen", at = @At("HEAD"))
    public void drawScreenPre(Minecraft minecraft, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (canShow && hasContent() && (!NEA.isHeiLoaded() || !Config.bufferIngredientRenders())) {
            GlStateManager.pushMatrix();
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if (screen instanceof GuiContainer container) {
                float val = 1f - OpeningAnimation.getValue(container);
                if (val <= 0f) return;
                GlStateManager.translate(-displayArea.width * val, 0, 0);
            }
        }
    }

    @Inject(method = "drawScreen", at = @At("TAIL"))
    public void drawScreenPost(Minecraft minecraft, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (canShow && hasContent() && (!NEA.isHeiLoaded() || !Config.bufferIngredientRenders())) {
            GlStateManager.popMatrix();
        }
    }
}*/
