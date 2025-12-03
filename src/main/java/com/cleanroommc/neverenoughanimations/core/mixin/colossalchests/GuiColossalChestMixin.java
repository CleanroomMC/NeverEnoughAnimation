package com.cleanroommc.neverenoughanimations.core.mixin.colossalchests;

import com.cleanroommc.neverenoughanimations.NEA;
import org.cyclops.colossalchests.client.gui.container.GuiColossalChest;
import org.cyclops.cyclopscore.client.gui.container.ScrollingGuiContainer;
import org.cyclops.cyclopscore.inventory.container.ScrollingInventoryContainer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiColossalChest.class)
public abstract class GuiColossalChestMixin extends ScrollingGuiContainer {

    public GuiColossalChestMixin(ScrollingInventoryContainer<?> container) {
        super(container);
    }

    @Override
    public void onGuiClosed() {
        // do not call onClose on inventory since it's already called by container
        NEA.onContainerClosed(mc.player);
    }
}
