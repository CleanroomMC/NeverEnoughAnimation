package com.cleanroommc.neverenoughanimations.core.mixin.early;

import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.animations.OpeningAnimation;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiScreen.class)
public class GuiScreenMixin extends Gui {

    @Redirect(method = "drawWorldBackground",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;drawGradientRect(IIIIII)V"))
    public void fadeBackground(GuiScreen instance, int left, int top, int width, int height, int startColor, int endColor) {
        float alpha = OpeningAnimation.getValue(instance);
        if (alpha < 1f) {
            startColor = NEA.withAlpha(startColor, (int) (NEA.getAlpha(startColor) * alpha));
            endColor = NEA.withAlpha(endColor, (int) (NEA.getAlpha(endColor) * alpha));
        }
        drawGradientRect(left, top, width, height, startColor, endColor);
    }

}
