package com.cleanroommc.neverenoughanimations.core.mixin.draconicevolution;

import com.brandon3055.brandonscore.client.gui.modulargui_old.ModularGuiContainer;
import com.brandon3055.draconicevolution.client.gui.GuiDraconiumChest;
import com.brandon3055.draconicevolution.inventory.ContainerDraconiumChest;
import com.cleanroommc.neverenoughanimations.NEA;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiDraconiumChest.class)
public abstract class GuiDraconiumChestMixin extends ModularGuiContainer<ContainerDraconiumChest> {

    protected GuiDraconiumChestMixin(ContainerDraconiumChest container) {
        super(container);
    }

    @Override
    public void onGuiClosed() {
        // do not call onClose on inventory since it's already called by container
        NEA.onContainerClosed(mc.player);
    }
}
