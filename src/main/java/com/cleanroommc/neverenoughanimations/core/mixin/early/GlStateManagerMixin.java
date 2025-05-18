package com.cleanroommc.neverenoughanimations.core.mixin.early;

/*
import com.cleanroommc.neverenoughanimations.NEA;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class GlStateManagerMixin {

    @Inject(method = "color(FFFF)V", at = @At("HEAD"))
    private static void color(float colorRed, float colorGreen, float colorBlue, float colorAlpha, CallbackInfo ci,
                              @Local(ordinal = 3, argsOnly = true) LocalFloatRef alpha) {
        if (NEA.isCurrentGuiAnimating()) {
            alpha.set(alpha.get() * NEA.getCurrentOpenAnimationValue());
        }
    }
}*/
