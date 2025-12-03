package com.cleanroommc.neverenoughanimations.core.mixin.rustic;

import com.cleanroommc.neverenoughanimations.NEA;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import rustic.client.gui.GuiCabinetDouble;

@Mixin(GuiCabinetDouble.class)
public abstract class GuiCabinetDoubleMixin extends GuiContainer {

    public GuiCabinetDoubleMixin(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Override
    public void onGuiClosed() {
        // do not call onClose on inventory since it's already called by container
        NEA.onContainerClosed(mc.player);
    }
}
