package com.cleanroommc.neverenoughanimations.core.mixin;

import com.cleanroommc.neverenoughanimations.NEA;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.renderer.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BufferBuilder.class)
public class BufferBuilderMixin {

    @Shadow private boolean noColor;

    @Inject(method = "color(IIII)Lnet/minecraft/client/renderer/BufferBuilder;", at = @At("HEAD"))
    public void alphaFade(int red, int green, int blue, int alpha, CallbackInfoReturnable<BufferBuilder> cir, @Local LocalIntRef alphaRef) {
        if (!noColor && NEA.isCurrentGuiAnimating()) {
            alphaRef.set((int) (alphaRef.get() * NEA.getCurrentOpenAnimationValue()));
        }
    }
}
