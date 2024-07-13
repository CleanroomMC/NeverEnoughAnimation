package com.cleanroommc.neverenoughanimations;

import com.cleanroommc.neverenoughanimations.animations.ItemHoverAnimation;
import com.cleanroommc.neverenoughanimations.animations.ItemMoveAnimation;
import com.cleanroommc.neverenoughanimations.animations.OpeningAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME,
        acceptedMinecraftVersions = "[1.12.2]", clientSideOnly = true, dependencies = "required:mixinbooter@[8.8,);")
public class NEA {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onGuiTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        ItemHoverAnimation.onGuiTick();
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent event) {
        if (event.getModID().equals(Tags.MODID)) {
            NEAConfig.blacklistCache.clear();
            ConfigManager.sync(Tags.MODID, Config.Type.INSTANCE);
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (OpeningAnimation.onGuiOpen(event)) return;
        ItemHoverAnimation.onGuiOpen(event);
        ItemMoveAnimation.onGuiOpen(event);
    }

    public static void drawScreenDebug(GuiContainer container, int mouseX, int mouseY) {
        if (!FMLLaunchHandler.isDeobfuscatedEnvironment() || container.getClass().getName().contains("modularui")) return;
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();

        int screenH = container.height;
        int color = new java.awt.Color(180, 40, 115).getRGB();
        int lineY = screenH - 13;
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        container.drawString(fr, "Mouse Pos: " + mouseX + ", " + mouseY, 5, lineY, color);
        lineY -= 11;
        container.drawString(fr, "Rel. Mouse Pos: " + (mouseX - container.getGuiLeft()) + ", " + (mouseY - container.getGuiTop()), 5, lineY, color);
        IItemLocation slot = IItemLocation.of(container.getSlotUnderMouse());
        if (slot != null) {
            lineY -= 11;
            container.drawString(fr, "Pos: " + slot.nea$getX() + ", " + slot.nea$getY(), 5, lineY, color);
            lineY -= 11;
            container.drawString(fr, "Class: " + slot.getClass().getSimpleName(), 5, lineY, color);
            lineY -= 11;
            container.drawString(fr, "Slot Number: " + slot.nea$getSlotNumber(), 5, lineY, color);
            lineY -= 11;
        }
        // dot at mouse pos
        Gui.drawRect(mouseX, mouseY, mouseX + 1, mouseY + 1, new Color(10, 230, 10, (int) (0.8 * 155)).getRGB());

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableStandardItemLighting();
    }
}
