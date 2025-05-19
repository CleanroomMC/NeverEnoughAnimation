package com.cleanroommc.neverenoughanimations.core.mixin.late.nei;

import codechicken.nei.LayoutManager;
import codechicken.nei.Widget;
import codechicken.nei.guihook.GuiContainerManager;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import com.cleanroommc.neverenoughanimations.animations.OpeningAnimation;
import com.cleanroommc.neverenoughanimations.util.GlStateManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LayoutManager.class, remap = false)
public class LayoutManagerMixin {

    /**
     * Transforms every widget on the screen during open/close animation such that widgets on the left move to/from the left, widgets
     * on the right move to/from the right and widgets which are horizontally centered move towards/away from their closest vertical edge
     * (top, bottom)
     */
    @WrapOperation(method = "renderObjects", at = @At(value = "INVOKE", target = "Lcodechicken/nei/Widget;draw(II)V"))
    public void renderObjects(Widget instance, int mouseX, int mouseY, Operation<Void> original) {
        boolean doAnimate = NEAConfig.openingAnimationTime > 0;
        if (doAnimate) {
            float val = 1f - OpeningAnimation.getValue(GuiContainerManager.getManager().window);
            if (val > 0) {
                GlStateManager.pushMatrix();
                int screenWidth = GuiContainerManager.getManager().window.width;
                int lPos = instance.x - screenWidth / 2;
                int rPos = instance.x + instance.w - screenWidth / 2;
                float x = 0;
                float y = 0;
                if (lPos < 0 && rPos < 0) {
                    x = -screenWidth / 2f; // widget is on left side
                } else if (lPos > 0 && rPos > 0) {
                    x = screenWidth / 2f; // widget is on right side
                } else {
                    // widget is somewhere in the middle
                    int screenHeight = GuiContainerManager.getManager().window.height;
                    // the * 4 makes the animation slightly faster to fit with the rest (they travel a much shorter distance)
                    if (instance.y > screenHeight / 2f) {
                        y = instance.h * 4; // widget is in the bottom
                    } else {
                        y = -instance.h * 4; // widget is in the top
                    }
                }
                GlStateManager.translate(val * x, val * y, 0);
            } else {
                doAnimate = false;
            }
        }
        original.call(instance, mouseX, mouseY);
        if (doAnimate) {
            GlStateManager.popMatrix();
        }
    }
}
