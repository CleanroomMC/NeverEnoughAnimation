package com.cleanroommc.neverenoughanimations;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig;

public class NEAGuiConfig extends SimpleGuiConfig {

    public NEAGuiConfig(GuiScreen parent) throws ConfigException {
        super(parent, Tags.MODID, Tags.MODNAME, NEAConfig.class);
    }
}
