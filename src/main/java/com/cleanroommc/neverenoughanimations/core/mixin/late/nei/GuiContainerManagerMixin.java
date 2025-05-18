package com.cleanroommc.neverenoughanimations.core.mixin.late.nei;

import codechicken.nei.guihook.GuiContainerManager;
import com.cleanroommc.neverenoughanimations.NEICompat;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiContainerManager.class, remap = false)
public class GuiContainerManagerMixin {

    @Shadow public GuiContainer window;

    @Inject(method = "drawSlotItem",
            at = @At(value = "INVOKE",
                     target = "Lcodechicken/nei/guihook/GuiContainerManager;drawItem(IILnet/minecraft/item/ItemStack;ZLjava/lang/String;)V"),
            cancellable = true)
    private void injectHoverScale(Slot slot, ItemStack stack, int x, int y, String quantity, CallbackInfo ci) {
        // NEI replaces item rendering, but doesn't keep slot and container information in the draw method
        // so we need to replace it with a modified version
        NEICompat.renderItem(window, slot, x, y, stack, GuiContainerManager.getFontRenderer(stack), false, quantity);
        ci.cancel();
    }

}
