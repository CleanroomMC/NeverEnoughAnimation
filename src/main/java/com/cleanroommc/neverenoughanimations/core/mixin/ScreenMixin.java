package com.cleanroommc.neverenoughanimations.core.mixin;

import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.animations.OpeningAnimation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Screen.class)
public class ScreenMixin {

    // applies value from open/close animation to alpha of transparent background
    @Redirect(method = "renderTransparentBackground",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fillGradient(IIIIII)V"))
    public void injectAlpha(GuiGraphics instance, int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        if ((Object) this instanceof AbstractContainerScreen<?> containerScreen) {
            float val = OpeningAnimation.getValue(containerScreen);
            if (val != 1f) {
                colorFrom = NEA.withAlpha(colorFrom, val * NEA.getAlphaF(colorFrom));
                colorTo = NEA.withAlpha(colorTo, val * NEA.getAlphaF(colorTo));
            }
        }
        instance.fillGradient(x1, y1, x2, y2, colorFrom, colorTo);
    }
}
