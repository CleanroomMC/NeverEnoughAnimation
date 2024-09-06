package com.cleanroommc.neverenoughanimations.core.mixin;

import com.cleanroommc.neverenoughanimations.animations.OpeningAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow @Nullable public Screen screen;

    @Shadow @Final private SoundManager soundManager;

    @Shadow @Final public MouseHandler mouseHandler;

    // this is ugly, but since open event are no longer called on close and closing event cant be cancelled we need to use mixin
    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;clearGuiLayers(Lnet/minecraft/client/Minecraft;)V", shift = At.Shift.BEFORE),
            cancellable = true)
    public void setScreen(Screen guiScreen, CallbackInfo ci) {
        if (OpeningAnimation.onGuiOpen(guiScreen, screen, () -> {
            this.soundManager.resume();
            this.mouseHandler.grabMouse();
        })) {
            ci.cancel();
        }
    }

}
