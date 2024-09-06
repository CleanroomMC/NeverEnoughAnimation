package com.cleanroommc.neverenoughanimations.core.mixin.mousetweaks;

import com.cleanroommc.neverenoughanimations.animations.ItemMoveAnimation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yalter.mousetweaks.IGuiScreenHandler;
import yalter.mousetweaks.Main;

import java.util.List;

@Mixin(value = Main.class, remap = false)
public abstract class MainMixin {

    @Shadow private static IGuiScreenHandler handler;

    @Shadow
    private static Slot findPullSlot(List<Slot> slots, Slot selectedSlot) {
        return null;
    }

    @Shadow
    private static List<Slot> findPushSlots(List<Slot> slots, Slot selectedSlot, int itemCount, boolean mustDistributeAll) {
        return null;
    }

    @Redirect(method = "onMouseScrolled",
              at = @At(value = "INVOKE",
                       target = "Lyalter/mousetweaks/Main;findPullSlot(Ljava/util/List;Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;"))
    private static Slot pullItems(List<Slot> slots, Slot selectedSlot,
                                  @Share("candidates") LocalRef<Pair<List<Slot>, List<ItemStack>>> candidates,
                                  @Share("sourceStack") LocalRef<ItemStack> sourceStack, @Share("sourceSlot") LocalRef<Slot> sourceSlot) {
        Slot pullSlot = findPullSlot(slots, selectedSlot);
        if (pullSlot == null) return null;
        sourceStack.set(pullSlot.getItem().copy());
        sourceSlot.set(pullSlot);
        candidates.set(ItemMoveAnimation.getCandidates(pullSlot, handler.getSlots()));
        return pullSlot;
    }

    @Redirect(method = "onMouseScrolled",
              at = @At(value = "INVOKE",
                       target = "Lyalter/mousetweaks/Main;findPushSlots(Ljava/util/List;Lnet/minecraft/world/inventory/Slot;IZ)Ljava/util/List;"))
    private static List<Slot> pushItems(List<Slot> slots, Slot selectedSlot, int itemCount, boolean mustDistributeAll,
                                        @Share("candidates") LocalRef<Pair<List<Slot>, List<ItemStack>>> candidates,
                                        @Share("sourceStack") LocalRef<ItemStack> sourceStack,
                                        @Share("sourceSlot") LocalRef<Slot> sourceSlot) {
        List<Slot> pushSlots = findPushSlots(slots, selectedSlot, itemCount, mustDistributeAll);
        sourceStack.set(selectedSlot.getItem().copy());
        sourceSlot.set(selectedSlot);
        candidates.set(ItemMoveAnimation.getCandidates(selectedSlot, handler.getSlots()));
        return pushSlots;
    }

    @Inject(method = "onMouseScrolled", at = @At("RETURN"))
    private static void handleWheelPost(Screen screen, double x, double y, double scrollDelta, CallbackInfoReturnable<Boolean> cir,
                                        @Share("candidates") LocalRef<Pair<List<Slot>, List<ItemStack>>> candidates,
                                        @Share("sourceStack") LocalRef<ItemStack> sourceStack,
                                        @Share("sourceSlot") LocalRef<Slot> sourceSlot) {
        if (candidates.get() != null) {
            ItemMoveAnimation.handleMove(sourceSlot.get(), sourceStack.get(), candidates.get());
        }
    }
}
