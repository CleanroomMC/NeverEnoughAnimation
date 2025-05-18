package com.cleanroommc.neverenoughanimations;

import com.gtnewhorizon.gtnhlib.config.SimpleGuiFactory;
import net.minecraft.client.gui.GuiScreen;

@SuppressWarnings("unused")
public class NEAGuiConfigFactory implements SimpleGuiFactory {

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return NEAGuiConfig.class;
    }
}
