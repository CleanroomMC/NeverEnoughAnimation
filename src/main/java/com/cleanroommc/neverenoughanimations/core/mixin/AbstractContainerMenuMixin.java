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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

    @Shadow private int quickcraftStatus;

    @Shadow
    public abstract ItemStack quickMoveStack(Player pPlayer, int pIndex);

    @Shadow
    @Final
    public NonNullList<Slot> slots;

    @Shadow public abstract ItemStack getCarried();

    @Unique
    private List<Slot> nea$getActualSlots() {
        Screen screen = Minecraft.getInstance().screen;
        return screen instanceof CreativeModeInventoryScreen gui ? gui.getMenu().slots : this.slots;
    }

    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    public void slotClick(int slotId, int button, ClickType clickTypeIn, Player player, CallbackInfo ci,
                          @Share("swapHolder") LocalRef<SwapHolder> swapHolder,
                          @Share("cursor") LocalRef<ItemStack> cursor) {
        if (player == null || !player.level().isClientSide()) return;
        if (clickTypeIn == ClickType.QUICK_MOVE && (button == 0 || button == 1) && quickcraftStatus == 0 && slotId != -999) {
            if (slotId < 0) {
                ci.cancel();
                return;
            }
            List<Slot> inventorySlots = nea$getActualSlots();
            Slot slot = inventorySlots.get(slotId);
            if (slot == null || !slot.mayPickup(player) || !slot.hasItem()) {
                ci.cancel();
                return;
            }
            ItemStack oldStack = slot.getItem().copy();
            // take snapshot of all current slots and its items where the item could land in
            Pair<List<Slot>, List<ItemStack>> candidates = ItemMoveAnimation.getCandidates(slot, inventorySlots);

            // minecraft start
            ItemStack itemstack8 = this.quickMoveStack(player, slotId);
            while (!itemstack8.isEmpty() && ItemStack.isSameItem(slot.getItem(), itemstack8)) {
                itemstack8 = this.quickMoveStack(player, slotId);
            }
            // minecraft end

            if (candidates != null) ItemMoveAnimation.handleMove(slot, oldStack, candidates);
            ci.cancel();
        } else if (clickTypeIn == ClickType.SWAP && button >= 0 && button < 9) {
            // fuck creative inventory
            //if ((Object) this instanceof GuiContainerCreative.ContainerCreative || NEAConfig.moveAnimationTime == 0) return;
            Slot targetSlot = nea$getActualSlots().get(slotId);
            if (SwapHolder.INSTANCE.init(targetSlot, nea$getActualSlots(), button)) {
                swapHolder.set(SwapHolder.INSTANCE);
            }
        } else if(clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0) {
            // prepare pickup all
            if (NEAConfig.moveAnimationTime == 0) return;
            cursor.set(getCarried().copy());
        }
    }

    @Inject(method = "doClick", at = @At("TAIL"))
    public void slotClickPost(int pSlotId, int pButton, ClickType pClickType, Player pPlayer, CallbackInfo ci,
                              @Share("swapHolder") LocalRef<SwapHolder> swapHolder) {
        if (swapHolder.get() != null) {
            swapHolder.get().performSwap();
        }
    }

    @Redirect(method = "doClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;grow(I)V"))
    public void pickupAllMid(ItemStack instance, int increment, @Share("packets") LocalRef<Int2ObjectArrayMap<ItemMovePacket>> packets,
                             @Local(ordinal = 1) Slot slot) {
        if (NEAConfig.moveAnimationTime > 0) {
            // handle animation
            if (packets.get() == null) packets.set(new Int2ObjectArrayMap<>());
            IItemLocation source = IItemLocation.of(slot);
            ItemStack movingStack = instance.copy();
            movingStack.setCount(increment);
            packets.get().put(source.nea$getSlotNumber(), new ItemMovePacket(NEA.time(), source, IItemLocation.CURSOR, movingStack));
        }
        // do the redirected action
        instance.grow(increment);
    }

    @Inject(method = "doClick", at = @At("TAIL"))
    public void pickupAllPost(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci,
                              @Share("packets") LocalRef<Int2ObjectArrayMap<ItemMovePacket>> packets,
                              @Share("cursor") LocalRef<ItemStack> cursor) {
        if (NEAConfig.moveAnimationTime == 0 || clickType != ClickType.PICKUP_ALL) return;
        if (packets.get() != null && !packets.get().isEmpty()) {
            for (var iterator = packets.get().int2ObjectEntrySet().fastIterator(); iterator.hasNext(); ) {
                var e = iterator.next();
                ItemMoveAnimation.queueAnimation(e.getIntKey(), e.getValue());
                ItemMoveAnimation.updateVirtualStack(-1, cursor.get(), 1);
            }
        }
    }

    @Inject(method = "doClick",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/entity/player/Player;drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;", ordinal = 3))
    public void throwItem(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci,
                          @Local(ordinal = 0) ItemStack throwing) {
        if (NEAConfig.appearAnimationTime == 0) return;
        IItemLocation slot = IItemLocation.of(nea$getActualSlots().get(slotId));
        if (slot.nea$getStack().isEmpty()) {
            // only animate when shift is held (throw hole stack) or only one item is left
            ItemPickupThrowAnimation.animate(slot.nea$getX(), slot.nea$getY(), throwing, false);
        }
    }

    @ModifyArg(method = "doClick",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/entity/player/Player;drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;"))
    public ItemStack animateThrow(ItemStack itemStackIn, @Local(ordinal = 0, argsOnly = true) int slot) {
        if (NEAConfig.appearAnimationTime > 0 && slot == -999) {
            ItemPickupThrowAnimation.animate(NEA.getMouseX() - 8, NEA.getMouseY() - 8, itemStackIn.copy(), true);
        }
        return itemStackIn;
    }
}
