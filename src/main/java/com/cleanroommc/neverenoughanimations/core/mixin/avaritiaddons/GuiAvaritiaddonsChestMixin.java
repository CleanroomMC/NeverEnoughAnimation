package com.cleanroommc.neverenoughanimations.core.mixin.avaritiaddons;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import wanion.avaritiaddons.block.chest.GuiAvaritiaddonsChest;
import wanion.avaritiaddons.block.chest.TileEntityAvaritiaddonsChest;
import wanion.lib.client.gui.WGuiContainer;
import wanion.lib.common.WContainer;

@Mixin(GuiAvaritiaddonsChest.class)
public class GuiAvaritiaddonsChestMixin<C extends TileEntityAvaritiaddonsChest> extends WGuiContainer<C> {


    protected GuiAvaritiaddonsChestMixin(@NotNull WContainer<C> wContainer, @NotNull ResourceLocation guiTextureLocation) {
        super(wContainer, guiTextureLocation);
    }

    @Override
    public void onGuiClosed() {
        // do not call onClose on inventory since it's already called by container
        InventoryPlayer inventoryplayer = mc.player.inventory;
        if (!inventoryplayer.getItemStack().isEmpty()) {
            mc.player.dropItem(inventoryplayer.getItemStack(), false);
            inventoryplayer.setItemStack(ItemStack.EMPTY);
        }
    }
}
