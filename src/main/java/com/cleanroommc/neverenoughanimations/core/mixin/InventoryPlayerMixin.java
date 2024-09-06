package com.cleanroommc.neverenoughanimations.core.mixin;

import com.cleanroommc.neverenoughanimations.animations.HotbarAnimation;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public class InventoryPlayerMixin {

    @Inject(method = "swapPaint", at = @At("HEAD"))
    public void preSwap(double direction, CallbackInfo ci) {
        HotbarAnimation.preTick();
    }

    @Inject(method = "swapPaint", at = @At("RETURN"))
    public void postSwap(double direction, CallbackInfo ci) {
        HotbarAnimation.postTick();
    }
}
