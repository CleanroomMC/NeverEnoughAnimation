package com.cleanroommc.neverenoughanimations.core.mixin;

import com.cleanroommc.neverenoughanimations.animations.ItemMoveAnimation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Container.class)
public abstract class ContainerMixin {

    @Shadow private int dragEvent;

    @Shadow public List<Slot> inventorySlots;

    @Shadow
    public abstract ItemStack transferStackInSlot(EntityPlayer playerIn, int index);

    @Inject(method = "slotClick", at = @At("HEAD"), cancellable = true)
    public void slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        if (player == null || player.world == null || !player.world.isRemote) return;
        if (clickTypeIn == ClickType.QUICK_MOVE && (dragType == 0 || dragType == 1) && dragEvent == 0 && slotId != -999) {
            if (slotId < 0) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }

            Slot slot5 = inventorySlots.get(slotId);

            if (slot5 == null || !slot5.canTakeStack(player) || !slot5.getHasStack()) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }

            ItemStack oldStack = slot5.getStack().copy();
            Pair<List<Slot>, List<ItemStack>> candidates = ItemMoveAnimation.getCandidates(slot5, inventorySlots);
            ItemStack itemstack = ItemStack.EMPTY;
            for (ItemStack itemstack7 = transferStackInSlot(player, slotId); !itemstack7.isEmpty() && ItemStack.areItemsEqual(slot5.getStack(), itemstack7); itemstack7 = this.transferStackInSlot(player, slotId)) {
                itemstack = itemstack7.copy();
            }
            if (candidates != null) ItemMoveAnimation.handleMove(slot5, oldStack, candidates);
            cir.setReturnValue(itemstack);
        }
    }
}
