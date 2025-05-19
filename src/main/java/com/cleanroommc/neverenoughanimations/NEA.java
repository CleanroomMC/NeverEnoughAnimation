package com.cleanroommc.neverenoughanimations;

import com.cleanroommc.neverenoughanimations.animations.ItemHoverAnimation;
import com.cleanroommc.neverenoughanimations.animations.ItemMoveAnimation;
import com.cleanroommc.neverenoughanimations.animations.ItemPickupThrowAnimation;
import com.cleanroommc.neverenoughanimations.animations.OpeningAnimation;
import com.cleanroommc.neverenoughanimations.api.IAnimatedScreen;
import com.cleanroommc.neverenoughanimations.api.IItemLocation;
import com.cleanroommc.neverenoughanimations.util.GlStateManager;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.Timer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Mouse;

import java.awt.*;

import static codechicken.lib.gui.GuiDraw.renderEngine;

@Mod(modid = Tags.MODID,
     version = Tags.VERSION,
     name = Tags.MODNAME,
     acceptedMinecraftVersions = "[1.7.10,)",
     dependencies = "required-after:gtnhmixins@[2.0.1,);",
     guiFactory = "com.cleanroommc.neverenoughanimations.NEAGuiConfigFactory")
public class NEA {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);
    private static boolean itemBordersLoaded = false, jeiLoaded = false, heiLoaded = false;

    private static GuiScreen currentDrawnScreen = null;
    private static float openAnimationValue = 1f;
    private static int mouseX, mouseY;

    public static final boolean isDevEnv = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

    public static final Timer timer60Tps = new Timer(60f);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        itemBordersLoaded = Loader.isModLoaded("itemborders");
        jeiLoaded = Loader.isModLoaded("jei");
        if (jeiLoaded) {
            ModContainer mod = Loader.instance().getIndexedModList().get("jei");
            heiLoaded = "Had Enough Items".equals(mod.getName());
        }
        try {
            ConfigurationManager.registerConfig(NEAConfig.class);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    public static void onFrameTick() {
        OpeningAnimation.checkGuiToClose();
    }


    // doesn't work for some reason
    /*@SubscribeEvent
    public void onGuiTick(TickEvent event) {
        //OpeningAnimation.checkGuiToClose();
        if (event.phase == TickEvent.Phase.END) return;

    }*/

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (OpeningAnimation.onGuiOpen(event)) return;
        ItemHoverAnimation.onGuiOpen(event);
        ItemMoveAnimation.onGuiOpen(event);
        ItemPickupThrowAnimation.onGuiOpen(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onGuiDrawPre(GuiScreenEvent.DrawScreenEvent.Pre event) {
        mouseX = event.mouseX;
        mouseY = event.mouseY;
        currentDrawnScreen = event.gui;
        ItemHoverAnimation.onGuiTick();
        // we are caching this value for the current gui since its potentially requested very often
        openAnimationValue = OpeningAnimation.getValue(currentDrawnScreen);
        if (NEAConfig.moveAnimationTime > 0 && event.gui instanceof IAnimatedScreen) {
            GlStateManager.pushMatrix();
            OpeningAnimation.currentlyScaling = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiDrawPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (OpeningAnimation.currentlyScaling && event.gui instanceof IAnimatedScreen screen) {
            GlStateManager.popMatrix();
            OpeningAnimation.getScale(screen); // make sure screens don't get stuck in case they don't render the scale
            OpeningAnimation.currentlyScaling = false;
        }
        currentDrawnScreen = null;
        openAnimationValue = 1f;
    }

    /*
    @SubscribeEvent
    public void drawDebugInfo(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (event.getGui() instanceof GuiContainer container) {
            drawScreenDebug(container, event.getMouseX(), event.getMouseY());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiBackgroundDrawn(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (NEAConfig.moveAnimationTime > 0) {
            OpeningAnimation.handleScale(event.getGui(), true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void mouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (OpeningAnimation.isAnimatingClose(event.getGui())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void mouseInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (OpeningAnimation.isAnimatingClose(event.getGui())) {
            event.setCanceled(true);
        }
    }*/

    public static int getMouseX() {
        return mouseX;
    }

    public static int getMouseY() {
        return mouseY;
    }

    public static @Nullable GuiScreen getCurrentDrawnScreen() {
        return currentDrawnScreen;
    }

    public static float getCurrentOpenAnimationValue() {
        // only works while rendering gui
        return openAnimationValue;
    }

    public static boolean isCurrentGuiAnimating() {
        // only works while rendering gui
        return currentDrawnScreen != null && openAnimationValue < 1f;
    }

    public static void drawScreenDebug(GuiContainer container, int mouseX, int mouseY) {
        /*if (!isDevEnv || container.getClass().getName().contains("modularui")) return;
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();

        int screenH = container.height;
        int color = new java.awt.Color(180, 40, 115).getRGB();
        int lineY = screenH - 13;
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        container.drawString(fr, "Mouse Pos: " + mouseX + ", " + mouseY, 5, lineY, color);
        lineY -= 11;
        container.drawString(fr, "Rel. Mouse Pos: " + (mouseX - container.getGuiLeft()) + ", " + (mouseY - container.getGuiTop()), 5, lineY,
                             color);
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
        RenderHelper.enableStandardItemLighting();*/
    }

    public static boolean isItemBordersLoaded() {
        return itemBordersLoaded;
    }

    public static boolean isJeiLoaded() {
        return jeiLoaded;
    }

    public static boolean isHeiLoaded() {
        return heiLoaded;
    }

    public static long time() {
        return System.nanoTime() / 1_000_000L;
    }

    public static int getAlpha(int argb) {
        return argb >> 24 & 255;
    }

    public static int withAlpha(int argb, int alpha) {
        argb &= ~(0xFF << 24);
        return argb | alpha << 24;
    }

    public static int withAlpha(int argb, float alpha) {
        return withAlpha(argb, (int) (alpha * 255));
    }
}
