package com.cleanroommc.neverenoughanimations;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig;
import net.minecraft.client.gui.GuiScreen;

public class NEAGuiConfig extends SimpleGuiConfig {

    public NEAGuiConfig(GuiScreen parent) throws ConfigException {
        super(parent, Tags.MODID, Tags.MODNAME, NEAConfig.class);
    }
}
