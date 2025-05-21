package com.cleanroommc.neverenoughanimations.core.mixin.early;

/*
 * @Mixin(RenderItem.class)
 * public class RenderItemMixin {
 * @Inject(method =
 * "renderModel(Lnet/minecraft/client/renderer/block/model/IBakedModel;ILnet/minecraft/item/ItemStack;)V", at
 * = @At("HEAD"))
 * public void renderItem(IBakedModel model, int color, ItemStack stack, CallbackInfo ci, @Local(argsOnly = true,
 * ordinal = 0) LocalIntRef colorRef) {
 * if (NEA.isCurrentGuiAnimating()) {
 * colorRef.set(NEA.withAlpha(colorRef.get(), (int) (NEA.getAlpha(colorRef.get()) *
 * NEA.getCurrentOpenAnimationValue())));
 * }
 * }
 * }
 */
