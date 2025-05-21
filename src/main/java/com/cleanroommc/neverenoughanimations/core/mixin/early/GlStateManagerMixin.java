package com.cleanroommc.neverenoughanimations.core.mixin.early;

/*
 * @Mixin(GlStateManager.class)
 * public class GlStateManagerMixin {
 * @Inject(method = "color(FFFF)V", at = @At("HEAD"))
 * private static void color(float colorRed, float colorGreen, float colorBlue, float colorAlpha, CallbackInfo ci,
 * @Local(ordinal = 3, argsOnly = true) LocalFloatRef alpha) {
 * if (NEA.isCurrentGuiAnimating()) {
 * alpha.set(alpha.get() * NEA.getCurrentOpenAnimationValue());
 * }
 * }
 * }
 */
