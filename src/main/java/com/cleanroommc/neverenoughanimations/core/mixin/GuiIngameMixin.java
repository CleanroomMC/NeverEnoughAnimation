package com.cleanroommc.neverenoughanimations.core.mixin;

import com.cleanroommc.neverenoughanimations.animations.HotbarAnimation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Gui.class)
public class GuiIngameMixin {

    @ModifyArg(method = "renderItemHotbar",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V",
                        ordinal = 1),
               index = 1)
    public int renderCurrentItemMarker(int x, @Local(argsOnly = true) GuiGraphics graphics) {
        return HotbarAnimation.getX(graphics);
    }
}
