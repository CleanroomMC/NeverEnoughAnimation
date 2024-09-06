/*package com.cleanroommc.neverenoughanimations.core.mixin.trashslot;

import com.cleanroommc.neverenoughanimations.animations.OpeningAnimation;
import net.blay09.mods.trashslot.client.ClientProxy;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientProxy.class, remap = false)
public class ClientProxyMixin {

    @Inject(method = "onDrawScreen(Lnet/minecraftforge/client/event/GuiScreenEvent$DrawScreenEvent$Pre;)V",
            at = @At(value = "INVOKE", target = "Lnet/blay09/mods/trashslot/client/gui/GuiTrashSlot;update(II)V", shift = At.Shift.BEFORE))
    public void onDrawScreenPre(GuiScreenEvent.DrawScreenEvent.Pre event, CallbackInfo ci) {
        GlStateManager.pushMatrix();
        OpeningAnimation.handleScale((GuiContainer) event.getGui(), true);
    }

    @Inject(method = "onDrawScreen(Lnet/minecraftforge/client/event/GuiScreenEvent$DrawScreenEvent$Pre;)V", at = @At("TAIL"))
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Pre event, CallbackInfo ci) {
        GlStateManager.popMatrix();
    }

    @Inject(method = "onBackgroundDrawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawSlot(Lnet/minecraft/inventory/Slot;)V", shift = At.Shift.BEFORE))
    public void onDrawBackground(GuiScreenEvent.BackgroundDrawnEvent event, CallbackInfo ci) {
        OpeningAnimation.handleScale((GuiContainer) event.getGui(), false);
    }
}*/
