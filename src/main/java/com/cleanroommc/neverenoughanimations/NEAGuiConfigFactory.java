package com.cleanroommc.neverenoughanimations;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizon.gtnhlib.config.SimpleGuiFactory;

@SuppressWarnings("unused")
public class NEAGuiConfigFactory implements SimpleGuiFactory {

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return NEAGuiConfig.class;
    }
}
