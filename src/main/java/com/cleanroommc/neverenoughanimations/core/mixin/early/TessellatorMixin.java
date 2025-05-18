package com.cleanroommc.neverenoughanimations.core.mixin.early;

import com.cleanroommc.neverenoughanimations.NEA;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;

import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Tessellator.class)
public class TessellatorMixin {

    @Shadow private boolean isColorDisabled;

    @Inject(method = "setColorRGBA", at = @At("HEAD"))
    public void alphaFade(int red, int green, int blue, int alpha, CallbackInfo ci, @Local(ordinal = 3, argsOnly = true) LocalIntRef alphaRef) {
        if (!isColorDisabled && NEA.isCurrentGuiAnimating()) {
            alphaRef.set((int) (alphaRef.get() * NEA.getCurrentOpenAnimationValue()));
        }
    }
}
