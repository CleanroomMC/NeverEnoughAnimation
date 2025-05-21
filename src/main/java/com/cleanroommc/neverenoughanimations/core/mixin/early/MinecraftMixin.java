package com.cleanroommc.neverenoughanimations.core.mixin.early;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cleanroommc.neverenoughanimations.NEA;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(
        method = "runGameLoop",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V",
            ordinal = 1,
            shift = At.Shift.AFTER))
    public void timer(CallbackInfo ci) {
        NEA.timer60Tps.updateTimer();
        for (int j = 0; j < Math.min(20, NEA.timer60Tps.elapsedTicks); ++j) {
            NEA.onFrameTick();
        }
    }

    // TODO maybe allow opening other uis while the current one is closing
    /*
     * @WrapOperation(method = "runTick", at = @At(value = "INVOKE", target =
     * "Lnet/minecraft/client/gui/GuiScreen;handleInput()V"))
     * public void cancelInteraction(GuiScreen instance, Operation<Void> original) {
     * if (!OpeningAnimation.isAnimatingClose(instance)) {
     * original.call(instance);
     * }
     * }
     */
}
