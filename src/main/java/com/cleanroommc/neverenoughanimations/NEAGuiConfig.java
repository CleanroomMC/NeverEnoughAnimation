package com.cleanroommc.neverenoughanimations;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig;

@SideOnly(Side.CLIENT)
public class NEAGuiConfig extends SimpleGuiConfig {

    public NEAGuiConfig(GuiScreen parent) throws ConfigException {
        super(parent, Tags.MODID, Tags.MODNAME, NEAConfig.class);
    }
}
