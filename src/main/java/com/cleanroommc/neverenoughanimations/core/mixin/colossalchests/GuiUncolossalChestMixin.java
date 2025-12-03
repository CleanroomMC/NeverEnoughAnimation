package com.cleanroommc.neverenoughanimations.core.mixin.colossalchests;

import com.cleanroommc.neverenoughanimations.NEA;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import org.cyclops.colossalchests.client.gui.container.GuiUncolossalChest;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiUncolossalChest.class)
public class GuiUncolossalChestMixin extends GuiHopper {

    public GuiUncolossalChestMixin(InventoryPlayer playerInv, IInventory hopperInv) {
        super(playerInv, hopperInv);
    }

    @Override
    public void onGuiClosed() {
        // do not call onClose on inventory since it's already called by container
        NEA.onContainerClosed(mc.player);
    }
}
