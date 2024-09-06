package com.cleanroommc.neverenoughanimations;

import com.cleanroommc.neverenoughanimations.animations.*;
import com.cleanroommc.neverenoughanimations.config.ModConfigMagic;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

@Mod(value = NEA.MODID, dist = Dist.CLIENT)
public class NEA {

    public static final String MODID = Tags.MODID;

    public static final Logger LOGGER = LogManager.getLogger(MODID);
    private static int mouseX, mouseY;

    public NEA(IEventBus eventBus, ModContainer container) {
        eventBus.addListener(this::commonSetup);
        eventBus.addListener(this::onLoadConfig);

        NeoForge.EVENT_BUS.register(this);

        container.registerConfig(ModConfig.Type.CLIENT, ModConfigMagic.create(NEAConfig.class));
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, screen) -> new ConfigurationScreen(container, screen));
    }

    private void commonSetup(final FMLCommonSetupEvent event) {}

    private void onLoadConfig(final ModConfigEvent event) {
        if (event.getConfig().getModId().equals(MODID)) {
            ModConfigMagic.load();
        }
    }

    @SubscribeEvent
    public void onGuiTickPre(ClientTickEvent.Pre event) {
        OpeningAnimation.checkGuiToClose();
        HotbarAnimation.preTick();
    }

    @SubscribeEvent
    public void onGuiTickPost(ClientTickEvent.Post event) {
        OpeningAnimation.checkGuiToClose();
        ItemHoverAnimation.onGuiTick();
        HotbarAnimation.postTick();
    }

    @SubscribeEvent
    public void onGuiOpen(ScreenEvent.Opening event) {
        ItemHoverAnimation.onGuiOpen(event);
        ItemMoveAnimation.onGuiOpen(event);
        ItemPickupThrowAnimation.onGuiOpen(event);
    }

    @SubscribeEvent
    public void onGuiClose(ScreenEvent.Closing event) {
        ItemHoverAnimation.onGuiClose();
        ItemMoveAnimation.onGuiClose(event.getScreen());
        ItemPickupThrowAnimation.onGuiClose();
    }

    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    public void onGuiDrawPre(ScreenEvent.Render.Pre event) {
        mouseX = event.getMouseX();
        mouseY = event.getMouseY();
        event.getGuiGraphics().pose().pushPose();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onGuiDrawPost(ScreenEvent.Render.Post event) {
        event.getGuiGraphics().pose().popPose();
        if (event.getScreen() instanceof AbstractContainerScreen<?> container) {
            OpeningAnimation.getValue(container); // make sure screens don't get stuck in case they don't render the scale
        }
    }

    public static int getMouseX() {
        return mouseX;
    }

    public static int getMouseY() {
        return mouseY;
    }

    public static void drawScreenDebug(GuiGraphics graphics, AbstractContainerScreen<?> container, int mouseX, int mouseY) {
        if (FMLLoader.isProduction() || container.getClass().getName().contains("modularui")) return;
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();

        int screenH = container.height;
        int color = new java.awt.Color(180, 40, 115).getRGB();
        int lineY = screenH - 13;
        Font fr = Minecraft.getInstance().font;
        graphics.drawString(fr, "Mouse Pos: " + mouseX + ", " + mouseY, 5, lineY, color);
        lineY -= 11;
        graphics.drawString(fr, "Rel. Mouse Pos: " + (mouseX - container.getGuiLeft()) + ", " + (mouseY - container.getGuiTop()), 5, lineY,
                            color);
        IItemLocation slot = IItemLocation.of(container.getSlotUnderMouse());
        if (slot != null) {
            lineY -= 11;
            graphics.drawString(fr, "Pos: " + slot.nea$getX() + ", " + slot.nea$getY(), 5, lineY, color);
            lineY -= 11;
            graphics.drawString(fr, "Class: " + slot.getClass().getSimpleName(), 5, lineY, color);
            lineY -= 11;
            graphics.drawString(fr, "Slot Number: " + slot.nea$getSlotNumber(), 5, lineY, color);
            lineY -= 11;
        }
        // dot at mouse pos
        graphics.fill(mouseX, mouseY, mouseX + 1, mouseY + 1, new Color(10, 230, 10, (int) (0.8 * 155)).getRGB());

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableDepthTest();
    }

    public static long time() {
        return System.nanoTime() / 1_000_000L;
    }

    public static int withAlpha(int argb, float alpha) {
        return withAlpha(argb, (int) (alpha * 255));
    }

    public static int withAlpha(int argb, int alpha) {
        argb &= ~(0xFF << 24);
        return argb | alpha << 24;
    }

    public static float getAlphaF(int argb) {
        return getAlpha(argb) / 255f;
    }

    public static int getAlpha(int argb) {
        return argb >> 24 & 255;
    }

    public static void drawItem(ItemStack stack, GuiGraphics graphics, Font font, int x, int y) {
        var font1 = IClientItemExtensions.of(stack).getFont(stack, IClientItemExtensions.FontContext.ITEM_COUNT);
        if (font1 != null) font = font1;
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(font, stack, x, y);
    }
}
