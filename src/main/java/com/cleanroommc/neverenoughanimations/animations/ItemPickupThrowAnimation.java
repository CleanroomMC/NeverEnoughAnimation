package com.cleanroommc.neverenoughanimations.animations;

import com.cleanroommc.neverenoughanimations.api.IItemLocation;
import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

public class ItemPickupThrowAnimation {

    private static final Object2LongOpenHashMap<IItemLocation> animated = new Object2LongOpenHashMap<>();
    private static final List<IItemLocation> removalAnimation = new ArrayList<>();
    private static GuiContainer lastGui;

    @ApiStatus.Internal
    public static void onGuiOpen(GuiOpenEvent event) {
        if (NEAConfig.hoverAnimationTime > 0) {
            if (!(event.getGui() instanceof GuiContainer)) {
                if (lastGui != null) {
                    lastGui = null;
                    animated.clear();
                }
                return;
            }
            if (!NEAConfig.isBlacklisted(event.getGui())) {
                lastGui = (GuiContainer) event.getGui();
                animated.clear();
            }
        }
    }

    public static void animate(Slot slot) {
        if (lastGui == null) return;
        animate(IItemLocation.of(slot));
    }

    public static void animate(IItemLocation slot) {
        if (lastGui == null) return;
        animated.put(slot, NEA.time());
        if (slot.nea$getSlotNumber() < 0) removalAnimation.add(slot);
    }

    public static void animate(int x, int y, ItemStack stack, boolean absolutePos) {
        if (lastGui == null) return;
        if (absolutePos) {
            x -= lastGui.getGuiLeft();
            y -= lastGui.getGuiTop();
        }
        animate(new IItemLocation.Impl(x, y, stack));
    }

    public static float getValue(GuiContainer container, Slot slot) {
        return getValue(container, IItemLocation.of(slot));
    }

    public static float getValue(GuiContainer container, IItemLocation slot) {
        if (lastGui != container || !animated.containsKey(slot)) return 1f;
        long time = animated.getLong(slot);
        float val = (NEA.time() - time) / (float) NEAConfig.appearAnimationTime;
        if (val >= 1f) {
            animated.removeLong(slot);
            return 1f;
        }
        return NEAConfig.appearAnimationCurve.interpolate(0f, 1f, val);
    }

    public static void drawIndependentAnimations(GuiContainer container, RenderItem itemRender, FontRenderer fontRenderer) {
        for (int i = 0, n = removalAnimation.size(); i < n; i++) {
            IItemLocation slot = removalAnimation.get(i);
            int x = slot.nea$getX();
            int y = slot.nea$getY();
            float value = 1f - getValue(container, slot);
            if (value <= 0f) {
                removalAnimation.remove(i);
                i--;
                n--;
                continue;
            }
            GlStateManager.translate(x, y, 0);
            if (value <= 1f) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(8, 8, 0);
                GlStateManager.scale(value, value, 1);
                GlStateManager.translate(-8, -8, 0);
            } else if (!animated.containsKey(slot)) {
                removalAnimation.remove(i);
                i--;
                n--;
                GlStateManager.translate(-x, -y, 0);
                continue;
            }
            GlStateManager.translate(0, 0, 32f);
            FontRenderer font = slot.nea$getStack().getItem().getFontRenderer(slot.nea$getStack());
            if (font == null) font = fontRenderer;
            itemRender.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().player, slot.nea$getStack(), 0, 0);
            itemRender.renderItemOverlayIntoGUI(font, slot.nea$getStack(), 0, 0, null);
            if (value <= 1f) {
                GlStateManager.popMatrix();
            }
            GlStateManager.translate(-x, -y, 0);
        }
    }
}
