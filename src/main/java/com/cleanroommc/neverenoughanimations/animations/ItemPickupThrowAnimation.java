package com.cleanroommc.neverenoughanimations.animations;

import com.cleanroommc.neverenoughanimations.IItemLocation;
import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

public class ItemPickupThrowAnimation {

    private static final Object2LongOpenHashMap<IItemLocation> animated = new Object2LongOpenHashMap<>();
    private static final List<IItemLocation> removalAnimation = new ArrayList<>();
    private static AbstractContainerScreen<?> lastGui;

    @ApiStatus.Internal
    public static void onGuiOpen(ScreenEvent.Opening event) {
        if (NEAConfig.hoverAnimationTime > 0) {
            if (!(event.getNewScreen() instanceof AbstractContainerScreen<?>)) {
                onGuiClose();
                return;
            }
            if (!NEAConfig.isBlacklisted(event.getNewScreen())) {
                lastGui = (AbstractContainerScreen<?>) event.getNewScreen();
                animated.clear();
            }
        }
    }

    public static void onGuiClose() {
        lastGui = null;
        animated.clear();
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

    public static float getValue(AbstractContainerScreen<?> container, Slot slot) {
        return getValue(container, IItemLocation.of(slot));
    }

    public static float getValue(AbstractContainerScreen<?> container, IItemLocation slot) {
        if (lastGui != container || !animated.containsKey(slot)) return 1f;
        long time = animated.getLong(slot);
        float val = (NEA.time() - time) / (float) NEAConfig.appearAnimationTime;
        if (val >= 1f) {
            animated.removeLong(slot);
            return 1f;
        }
        return NEAConfig.appearAnimationCurve.interpolate(0f, 1f, val);
    }

    public static void drawIndependentAnimations(AbstractContainerScreen<?> container, GuiGraphics graphics, Font font) {
        var pose = graphics.pose();
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
            pose.translate(x, y, 32f);
            if (value <= 1f) {
                pose.pushPose();
                pose.translate(8, 8, 0);
                pose.scale(value, value, 1);
                pose.translate(-8, -8, 0);
            } else if (!animated.containsKey(slot)) {
                removalAnimation.remove(i);
                i--;
                n--;
                pose.translate(-x, -y, 0);
                continue;
            }
            NEA.drawItem(slot.nea$getStack(), graphics, font, 0, 0);
            if (value <= 1f) {
                pose.popPose();
            }
            pose.translate(-x, -y, 0);
        }
    }
}
