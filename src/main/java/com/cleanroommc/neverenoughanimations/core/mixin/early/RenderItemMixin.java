package com.cleanroommc.neverenoughanimations.core.mixin.early;

/*
import com.cleanroommc.neverenoughanimations.NEA;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItem.class)
public class RenderItemMixin {

    @Inject(method = "renderModel(Lnet/minecraft/client/renderer/block/model/IBakedModel;ILnet/minecraft/item/ItemStack;)V", at = @At("HEAD"))
    public void renderItem(IBakedModel model, int color, ItemStack stack, CallbackInfo ci, @Local(argsOnly = true, ordinal = 0) LocalIntRef colorRef) {
        if (NEA.isCurrentGuiAnimating()) {
            colorRef.set(NEA.withAlpha(colorRef.get(), (int) (NEA.getAlpha(colorRef.get()) * NEA.getCurrentOpenAnimationValue())));
        }
    }
}*/
