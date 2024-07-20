package com.cleanroommc.neverenoughanimations.core.mixin;

import com.cleanroommc.neverenoughanimations.IItemLocation;
import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import com.cleanroommc.neverenoughanimations.animations.ItemMoveAnimation;
import com.cleanroommc.neverenoughanimations.animations.ItemMovePacket;
import com.cleanroommc.neverenoughanimations.animations.ItemPickupThrowAnimation;
import com.cleanroommc.neverenoughanimations.animations.SwapHolder;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Container.class)
public abstract class ContainerMixin {

    @Shadow private int dragEvent;

    @Shadow public List<Slot> inventorySlots;

    @Shadow
    public abstract ItemStack transferStackInSlot(EntityPlayer playerIn, int index);

    @Inject(method = "slotClick", at = @At("HEAD"), cancellable = true)
    public void slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir,
                          @Share("swapHolder") LocalRef<SwapHolder> swapHolder) {
        if (player == null || player.world == null || !player.world.isRemote) return;
        if (clickTypeIn == ClickType.QUICK_MOVE && (dragType == 0 || dragType == 1) && dragEvent == 0 && slotId != -999) {
            if (slotId < 0) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }

            Container c = (Container) (Object) this;
            // creative gui does stuff very differently
            List<Slot> inventorySlots = c instanceof ContainerPlayer &&
                    Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative gui ?
                    gui.inventorySlots.inventorySlots : this.inventorySlots;
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
        } else if (clickTypeIn == ClickType.SWAP && dragType >= 0 && dragType < 9) {
            // fuck creative inventory
            if ((Object) this instanceof GuiContainerCreative.ContainerCreative) return;
            Slot targetSlot = this.inventorySlots.get(slotId);
            if (SwapHolder.INSTANCE.init(targetSlot, this.inventorySlots, dragType)) {
                swapHolder.set(SwapHolder.INSTANCE);
            }
        }
    }

    @Inject(method = "slotClick", at = @At("TAIL"))
    public void slotClickPost(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir,
                              @Share("swapHolder") LocalRef<SwapHolder> swapHolder) {
        if (swapHolder.get() != null) {
            swapHolder.get().performSwap();
        }
    }

    @Inject(method = "slotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;getItemStack()Lnet/minecraft/item/ItemStack;", ordinal = 13))
    public void pickupAllPre(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player,
                             CallbackInfoReturnable<ItemStack> cir, @Share("cursor") LocalRef<ItemStack> cursor) {
        cursor.set(player.inventory.getItemStack().copy());
    }

    @Redirect(method = "slotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;grow(I)V", ordinal = 2))
    public void pickupAllMid(ItemStack instance, int quantity, @Share("packets") LocalRef<Int2ObjectArrayMap<ItemMovePacket>> packets, @Local(ordinal = 1) Slot slot) {
        if (NEAConfig.moveAnimationTime == 0) return;
        if (packets.get() == null) packets.set(new Int2ObjectArrayMap<>());
        IItemLocation source = IItemLocation.of(slot);
        ItemStack movingStack = instance.copy();
        movingStack.setCount(quantity);
        packets.get().put(source.nea$getSlotNumber(), new ItemMovePacket(NEA.time(), source, IItemLocation.CURSOR, movingStack));
        instance.grow(quantity);
    }

    @Inject(method = "slotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;detectAndSendChanges()V"))
    public void pickupAllPost(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player,
                              CallbackInfoReturnable<ItemStack> cir, @Share("packets") LocalRef<Int2ObjectArrayMap<ItemMovePacket>> packets,
                              @Share("cursor") LocalRef<ItemStack> cursor) {
        if (packets.get() != null && !packets.get().isEmpty()) {
            for (var iterator = packets.get().int2ObjectEntrySet().fastIterator(); iterator.hasNext(); ) {
                var e = iterator.next();
                ItemMoveAnimation.queueAnimation(e.getIntKey(), e.getValue());
                ItemMoveAnimation.updateVirtualStack(-1, cursor.get(), 1);
            }
        }
    }

    @Inject(method = "slotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/item/EntityItem;", ordinal = 3))
    public void throwItem(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player,
                          CallbackInfoReturnable<ItemStack> cir, @Local(ordinal = 1) LocalRef<ItemStack> throwing) {
        IItemLocation slot = IItemLocation.of(this.inventorySlots.get(slotId));
        if (slot.nea$getStack().isEmpty()) {
            // only animate when shift is held (throw hole stack) or only one item is left
            ItemPickupThrowAnimation.animate(slot.nea$getX(), slot.nea$getY(), throwing.get(), false);
        }
    }

    @ModifyArg(method = "slotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/item/EntityItem;"))
    public ItemStack animateThrow(ItemStack itemStackIn, @Local(ordinal = 0, argsOnly = true) int slot) {
        if (slot == -999) {
            ItemPickupThrowAnimation.animate(NEA.getMouseX() - 8, NEA.getMouseY() - 8, itemStackIn.copy(), true);
        }
        return itemStackIn;
    }
}
