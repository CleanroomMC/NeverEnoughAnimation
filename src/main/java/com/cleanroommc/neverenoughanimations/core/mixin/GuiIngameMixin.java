package com.cleanroommc.neverenoughanimations.core.mixin;

import com.cleanroommc.neverenoughanimations.animations.HotbarAnimation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GuiIngame.class)
public class GuiIngameMixin extends Gui {

    @ModifyArg(method = "renderHotbar",
               at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;drawTexturedModalRect(IIIIII)V", ordinal = 1),
               index = 0)
    public int renderCurrentItemMarker(int x, @Local(ordinal = 0, argsOnly = true) ScaledResolution sr) {
        return HotbarAnimation.getX(sr);
    }
}
