package com.cleanroommc.neverenoughanimations.core.mixin.early;

import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.GuiIngameForge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.cleanroommc.neverenoughanimations.animations.HotbarAnimation;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(GuiIngameForge.class)
public class GuiIngameMixin extends Gui {

    @ModifyArg(
        method = "renderHotbar",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V",
            ordinal = 1),
        index = 0)
    public int renderCurrentItemMarker(int x, @Local(ordinal = 0, argsOnly = true) int width) {
        return HotbarAnimation.getX(width);
    }
}
