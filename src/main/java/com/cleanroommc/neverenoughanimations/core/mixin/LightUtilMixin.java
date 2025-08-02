package com.cleanroommc.neverenoughanimations.core.mixin;

import com.cleanroommc.neverenoughanimations.NEA;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightUtil.class)
public class LightUtilMixin {

    @Inject(method = "renderQuadColor", at = @At("HEAD"), remap = false)
    private static void injectItemAlpha(BufferBuilder buffer, BakedQuad quad, int auxColor, CallbackInfo ci, @Local(argsOnly = true, ordinal = 0) LocalIntRef colorRef) {
        if (NEA.isCurrentGuiAnimating()) {
            colorRef.set(NEA.withAlpha(colorRef.get(), (int) (NEA.getAlpha(colorRef.get()) * NEA.getCurrentOpenAnimationValue())));
        }
    }

}
