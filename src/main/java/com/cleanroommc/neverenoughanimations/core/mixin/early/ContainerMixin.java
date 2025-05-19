package com.cleanroommc.neverenoughanimations.core.mixin.early;

import com.cleanroommc.neverenoughanimations.api.IItemLocation;
import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import com.cleanroommc.neverenoughanimations.animations.ItemMoveAnimation;
import com.cleanroommc.neverenoughanimations.animations.ItemMovePacket;
import com.cleanroommc.neverenoughanimations.animations.ItemPickupThrowAnimation;
import com.cleanroommc.neverenoughanimations.animations.SwapHolder;
import com.cleanroommc.neverenoughanimations.util.ClickType;
import com.cleanroommc.neverenoughanimations.util.Platform;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.entity.player.EntityPlayer;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Container.class)
public abstract class ContainerMixin {

    @Shadow private int field_94536_g; // dragMode

    @Shadow public List<Slot> inventorySlots;

    @Shadow
    public abstract ItemStack transferStackInSlot(EntityPlayer playerIn, int index);

    @Inject(method = "slotClick", at = @At("HEAD"), cancellable = true)
    public void slotClick(int slotId, int dragType, int mode, EntityPlayer player,
                          CallbackInfoReturnable<ItemStack> cir, @Share("swapHolder") LocalRef<SwapHolder> swapHolder) {
        if (player == null || player.worldObj == null || !player.worldObj.isRemote) return;
        ClickType clickTypeIn = ClickType.fromNumber(mode);
        if (clickTypeIn == ClickType.QUICK_MOVE && (dragType == 0 || dragType == 1) && field_94536_g == 0 && slotId != -999) {
            if (slotId < 0) {
                cir.setReturnValue(Platform.EMPTY_STACK);
                return;
            }

            Container c = (Container) (Object) this;
            // creative gui does stuff very differently
            List<Slot> inventorySlots = c instanceof ContainerPlayer &&
                    Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative gui ?
                    gui.inventorySlots.inventorySlots : this.inventorySlots;
            Slot fromSlot = inventorySlots.get(slotId);

            if (fromSlot == null || !fromSlot.canTakeStack(player) || !fromSlot.getHasStack()) {
                cir.setReturnValue(Platform.EMPTY_STACK);
                return;
            }

            ItemStack oldStack = fromSlot.getStack().copy();
            Pair<List<Slot>, List<ItemStack>> candidates = ItemMoveAnimation.getCandidates(fromSlot, inventorySlots);
            ItemStack returnable = Platform.EMPTY_STACK;
            // looping so that crafting works properly
            ItemStack remainder;
            do {
                remainder = transferStackInSlot(player, slotId);
                returnable = Platform.copyStack(remainder);
            } while (!Platform.isStackEmpty(remainder) && Platform.canItemStacksStack(fromSlot.getStack(), remainder));
            if (candidates != null) ItemMoveAnimation.handleMove(fromSlot, oldStack, candidates);
            cir.setReturnValue(returnable);
        } else if (clickTypeIn == ClickType.SWAP && dragType >= 0 && dragType < 9) {
            // fuck creative inventory
            // TODO if ((Object) this instanceof GuiContainerCreative.ContainerCreative || NEAConfig.moveAnimationTime == 0) return;
            if (NEAConfig.moveAnimationTime == 0) return;
            Slot targetSlot = this.inventorySlots.get(slotId);
            if (SwapHolder.INSTANCE.init(targetSlot, this.inventorySlots, dragType)) {
                swapHolder.set(SwapHolder.INSTANCE);
            }
        }
    }

    @Inject(method = "slotClick", at = @At("TAIL"))
    public void slotClickPost(int slotId, int dragType, int clickTypeIn, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir,
                              @Share("swapHolder") LocalRef<SwapHolder> swapHolder) {
        if (swapHolder.get() != null) {
            swapHolder.get().performSwap();
        }
    }

    @Inject(method = "slotClick",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/entity/player/InventoryPlayer;getItemStack()Lnet/minecraft/item/ItemStack;",
                     ordinal = 18))
    public void pickupAllPre(int slotId, int dragType, int clickTypeIn, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir,
                             @Share("cursor") LocalRef<ItemStack> cursor) {
        if (NEAConfig.moveAnimationTime == 0) return;
        cursor.set(player.inventory.getItemStack().copy());
    }

    @WrapOperation(method = "slotClick", at = @At(value = "FIELD", target = "Lnet/minecraft/item/ItemStack;stackSize:I", ordinal = 41))
    public void pickupAllMid(ItemStack instance, int quantity, Operation<Void> original,
                             @Share("packets") LocalRef<Int2ObjectArrayMap<ItemMovePacket>> packets, @Local(ordinal = 1) Slot slot) {
        if (NEAConfig.moveAnimationTime > 0) {
            // handle animation
            if (packets.get() == null) packets.set(new Int2ObjectArrayMap<>());
            IItemLocation source = IItemLocation.of(slot);
            ItemStack movingStack = instance.copy();
            Platform.setCount(movingStack, quantity - instance.stackSize); // contrary to 1.12 this doesnt grow stack size by quantity, but sets stack size by quantity, so we need to adjust it here
            packets.get().put(source.nea$getSlotNumber(), new ItemMovePacket(NEA.time(), source, IItemLocation.CURSOR, movingStack));
        }
        // do the redirected action
        original.call(instance, quantity);
    }

    @Inject(method = "slotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;detectAndSendChanges()V"))
    public void pickupAllPost(int slotId, int dragType, int clickTypeIn, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir,
                              @Share("packets") LocalRef<Int2ObjectArrayMap<ItemMovePacket>> packets,
                              @Share("cursor") LocalRef<ItemStack> cursor) {
        if (NEAConfig.moveAnimationTime == 0) return;
        if (packets.get() != null && !packets.get().isEmpty()) {
            for (var iterator = packets.get().int2ObjectEntrySet().fastIterator(); iterator.hasNext(); ) {
                var e = iterator.next();
                ItemMoveAnimation.queueAnimation(e.getIntKey(), e.getValue());
                ItemMoveAnimation.updateVirtualStack(-1, cursor.get(), 1);
            }
        }
    }

    @Inject(method = "slotClick",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/entity/player/EntityPlayer;dropPlayerItemWithRandomChoice(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/item/EntityItem;",
                     ordinal = 2))
    public void throwItem(int slotId, int dragType, int clickTypeIn, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir,
                          @Local(ordinal = 1) LocalRef<ItemStack> throwing) {
        if (NEAConfig.appearAnimationTime == 0) return;
        IItemLocation slot = IItemLocation.of(this.inventorySlots.get(slotId));
        if (Platform.isStackEmpty(slot.nea$getStack())) {
            // only animate when shift is held (throw hole stack) or only one item is left
            ItemPickupThrowAnimation.animate(slot.nea$getX(), slot.nea$getY(), throwing.get(), false);
        }
    }

    @ModifyArg(method = "slotClick",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/entity/player/EntityPlayer;dropPlayerItemWithRandomChoice(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/item/EntityItem;"))
    public ItemStack animateThrow(ItemStack itemStackIn, @Local(ordinal = 0, argsOnly = true) int slot) {
        if (NEAConfig.appearAnimationTime > 0 && slot == -999) {
            ItemPickupThrowAnimation.animate(NEA.getMouseX() - 8, NEA.getMouseY() - 8, itemStackIn.copy(), true);
        }
        return itemStackIn;
    }
}
