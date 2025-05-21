package com.cleanroommc.neverenoughanimations.core.mixin.early;

import net.minecraft.entity.player.InventoryPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cleanroommc.neverenoughanimations.animations.HotbarAnimation;

@Mixin(InventoryPlayer.class)
public class InventoryPlayerMixin {

    @Shadow
    public int currentItem;

    @Inject(method = "changeCurrentItem", at = @At("HEAD"), cancellable = true)
    public void animeCurrentItem(int direction, CallbackInfo ci) {
        if (direction == 0) {
            ci.cancel();
            return;
        }
        int dir = direction > 0 ? 1 : -1;
        int old = currentItem;
        currentItem -= dir;
        if (currentItem < 0) currentItem += 9;
        currentItem %= 9;
        HotbarAnimation.animate(old, currentItem);
        ci.cancel();
    }
}
