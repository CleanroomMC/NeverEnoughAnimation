package com.cleanroommc.neverenoughanimations;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ItemMoveAnimation {

    // all itemstack that are currently being animated
    private static final Int2ObjectOpenHashMap<List<ItemMovePacket>> movingItemsBySource = new Int2ObjectOpenHashMap<>();
    private static GuiContainer lastGui = null;
    // when a stack is moving the stack is actually in the target slot all the time, so we need to trick the player
    // into thinking there is nothing
    private static final ObjectArrayList<ItemStack> virtualStacks = new ObjectArrayList<>(256);
    // we need to keep track on how many packets are modifying the virtual stack, so we know when we no longer need it
    private static final IntArrayList virtualStacksUser = new IntArrayList(256);
    private static long lastAnimation = 0;

    @ApiStatus.Internal
    public static void onGuiTick(GuiScreen current) {
        if (NEAConfig.moveAnimationTime == 0) return;
        if (!(current instanceof GuiContainer)) {
            if (lastGui != null) {
                lastGui = null;
                movingItemsBySource.clear();
            }
            return;
        }
        if (current != lastGui) {
            lastGui = (GuiContainer) current;
            movingItemsBySource.clear();
            virtualStacks.clear();
            virtualStacksUser.clear();
        }
    }

    public static ItemStack getVirtualStack(GuiContainer container, Slot slot) {
        return container == lastGui &&
                virtualStacks.size() > slot.slotNumber &&
                virtualStacksUser.getInt(slot.slotNumber) > 0 ?
                virtualStacks.get(slot.slotNumber) : null;
    }

    @ApiStatus.Internal
    public static void updateVirtualStack(int target, ItemStack stack, int op) {
        if (NEAConfig.moveAnimationTime == 0) return;
        while (target >= virtualStacks.size()) {
            // ensure size and default values
            virtualStacks.add(null);
            virtualStacksUser.add(0);
        }
        int users = virtualStacksUser.getInt(target) + op;
        if (users <= 0) {
            // no users left -> set to null
            stack = null;
            users = 0;
        }
        virtualStacks.set(target, stack);
        virtualStacksUser.set(target, users);
    }

    /**
     * Collects all slot where the clicked itemstack could potentially end up
     */
    @ApiStatus.Internal
    public static Pair<List<Slot>, List<ItemStack>> getCandidates(Slot in, List<Slot> allSlots) {
        if (NEAConfig.moveAnimationTime == 0 || Minecraft.getSystemTime() - lastAnimation <= 10) return null;
        List<Slot> slots = new ArrayList<>(allSlots.size());
        List<ItemStack> stacks = new ArrayList<>(allSlots.size());
        ItemStack item = in.getStack();
        for (Slot slot : allSlots) {
            if (in == slot) continue;
            ItemStack other = slot.getStack();
            if (other.isEmpty()) {
                slots.add(slot);
                stacks.add(ItemStack.EMPTY);
            } else if (ItemHandlerHelper.canItemStacksStack(item, other)) {
                slots.add(slot);
                stacks.add(other.copy());
            }
        }
        return Pair.of(slots, stacks);
    }

    /**
     * Checks where the itemstack has landed and creates necessary packets.
     */
    @ApiStatus.Internal
    public static void handleMove(Slot source, ItemStack oldSource, Pair<List<Slot>, List<ItemStack>> candidates) {
        List<Slot> slots = candidates.getLeft();
        List<ItemStack> stacks = candidates.getRight();
        // total amount of moved items
        int total = oldSource.getCount() - source.getStack().getCount();
        if (total <= 0) return;
        List<ItemMovePacket> packets = new ArrayList<>();
        Int2ObjectArrayMap<ItemStack> stagedVirtualStacks = new Int2ObjectArrayMap<>();
        boolean error = false;
        long time = Minecraft.getSystemTime();
        for (int i = 0; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            if (slot == source) continue;
            ItemStack oldStack = stacks.get(i);
            ItemStack newStack = slot.getStack();
            if (oldStack.isEmpty()) {
                // was empty
                if (!newStack.isEmpty()) {
                    // now it's not empty -> found
                    ItemMovePacket packet = new ItemMovePacket(time, source, slot, newStack.copy());
                    packets.add(packet);
                    total -= newStack.getCount();
                    stagedVirtualStacks.put(slot.slotNumber, oldStack);
                }
            } else if (ItemHandlerHelper.canItemStacksStack(newStack, oldStack)) {
                // the stackable check is not really necessary but is still here for safety
                if (oldStack.getCount() < newStack.getCount()) {
                    // stackable and amount changed -> found
                    ItemStack movingStack = newStack.copy();
                    movingStack.shrink(oldStack.getCount());
                    ItemMovePacket packet = new ItemMovePacket(time, source, slot, movingStack);
                    packets.add(packet);
                    total -= movingStack.getCount();
                    stagedVirtualStacks.put(slot.slotNumber, oldStack);
                } else if (oldStack.getCount() > newStack.getCount()) {
                    // what
                    NEA.LOGGER.error("After shift clicking a target slot ({}) now has less items than before!", slot.slotNumber);
                    error = true;
                }
            } else {
                // what
                NEA.LOGGER.error("After shift clicking a target slot ({}) now has a different item than before!", slot.slotNumber);
                error = true;
            }
            if (total <= 0) break;
        }
        if (total < 0) {
            NEA.LOGGER.error("The original stack had {} items, but {} items where moved!", oldSource.getCount(), oldSource.getCount() - total);
        }
        if (error || packets.isEmpty()) return;
        if (packets.size() == 1) {
            movingItemsBySource.put(source.slotNumber, Collections.singletonList(packets.get(0)));
        } else {
            movingItemsBySource.put(source.slotNumber, packets);
        }
        for (var iterator = stagedVirtualStacks.int2ObjectEntrySet().fastIterator(); iterator.hasNext(); ) {
            var e = iterator.next();
            updateVirtualStack(e.getIntKey(), e.getValue(), 1);
        }
        lastAnimation = Minecraft.getSystemTime();
    }

    /**
     * Render all animated item stacks.
     */
    public static void drawAnimations() {
        drawAnimations(Minecraft.getMinecraft().getRenderItem(), Minecraft.getMinecraft().fontRenderer);
    }

    /**
     * Render all animated item stacks.
     */
    public static void drawAnimations(RenderItem itemRender, FontRenderer fontRenderer) {
        for (var iter = movingItemsBySource.values().iterator(); iter.hasNext(); ) {
            List<ItemMovePacket> packets = iter.next();
            for (Iterator<ItemMovePacket> iterator = packets.iterator(); iterator.hasNext(); ) {
                ItemMovePacket packet = iterator.next();
                if (!packet.checkEnd()) {
                    if (packets.size() == 1) {
                        iter.remove();
                        break;
                    }
                    iterator.remove();
                    continue;
                }
                float val = packet.value();
                int x = packet.getDrawX(val);
                int y = packet.getDrawY(val);
                GlStateManager.translate(0, 0, 32f);
                FontRenderer font = packet.getMovingStack().getItem().getFontRenderer(packet.getMovingStack());
                if (font == null) font = fontRenderer;
                itemRender.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().player, packet.getMovingStack(), x, y);
                itemRender.renderItemOverlayIntoGUI(font, packet.getMovingStack(), x, y, null);
            }
            if (packets.isEmpty()) iter.remove();
        }
    }
}
