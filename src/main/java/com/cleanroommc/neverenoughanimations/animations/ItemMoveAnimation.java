package com.cleanroommc.neverenoughanimations.animations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import com.cleanroommc.neverenoughanimations.api.IItemLocation;
import com.cleanroommc.neverenoughanimations.util.GlStateManager;
import com.cleanroommc.neverenoughanimations.util.Platform;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

@SideOnly(Side.CLIENT)
public class ItemMoveAnimation {

    public static final ItemStack NULL_MARKER = new ItemStack(Items.diamond);

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
    public static void onGuiOpen(GuiOpenEvent event) {
        if (NEAConfig.moveAnimationTime == 0) return;
        if (!(event.gui instanceof GuiContainer)) {
            if (lastGui != null) {
                lastGui = null;
                movingItemsBySource.clear();
            }
            return;
        }
        if (!NEAConfig.isBlacklisted(event.gui)) {
            lastGui = (GuiContainer) event.gui;
            movingItemsBySource.clear();
            virtualStacks.clear();
            virtualStacksUser.clear();
        }
    }

    public static ItemStack getVirtualStack(GuiContainer container, Slot slot) {
        return getVirtualStack(container, IItemLocation.of(slot));
    }

    public static ItemStack getVirtualStack(GuiContainer container, IItemLocation slot) {
        return container == lastGui && !NEAConfig.isBlacklisted(container)
            && virtualStacks.size() > slot.nea$getSlotNumber() + 1
            && virtualStacksUser.getInt(slot.nea$getSlotNumber() + 1) > 0
                ? virtualStacks.get(slot.nea$getSlotNumber() + 1)
                : NULL_MARKER;
    }

    @ApiStatus.Internal
    public static void updateVirtualStack(int target, ItemStack stack, int op) {
        if (NEAConfig.moveAnimationTime == 0) return;
        while (target + 1 >= virtualStacks.size()) {
            // ensure size and default values
            virtualStacks.add(NULL_MARKER);
            virtualStacksUser.add(0);
        }
        int users = virtualStacksUser.getInt(target + 1) + op;
        if (users <= 0) {
            // no users left -> set to null
            stack = NULL_MARKER;
            users = 0;
        }
        virtualStacks.set(target + 1, stack);
        virtualStacksUser.set(target + 1, users);
    }

    public static void queueAnimation(int slot, ItemMovePacket packet) {
        List<ItemMovePacket> packets = movingItemsBySource.get(slot + 1);
        if (packets == null) {
            packets = new ArrayList<>();
            movingItemsBySource.put(slot + 1, packets);
        }
        packets.add(packet);
    }

    public static void queueAnimation(int slot, List<ItemMovePacket> packet) {
        List<ItemMovePacket> packets = movingItemsBySource.get(slot + 1);
        if (packets == null) {
            packets = new ArrayList<>();
            movingItemsBySource.put(slot + 1, packets);
        }
        packets.addAll(packet);
    }

    /**
     * Collects all slot where the clicked itemstack could potentially end up
     */
    @ApiStatus.Internal
    public static Pair<List<Slot>, List<ItemStack>> getCandidates(Slot in, List<Slot> allSlots) {
        if (NEAConfig.moveAnimationTime == 0 || NEAConfig.isBlacklisted(Minecraft.getMinecraft().currentScreen)
            || NEA.time() - lastAnimation <= 10) {
            return null;
        }
        List<Slot> slots = new ArrayList<>(allSlots.size());
        List<ItemStack> stacks = new ArrayList<>(allSlots.size());
        ItemStack item = IItemLocation.of(in)
            .nea$getStack();
        for (Slot slot : allSlots) {
            if (in == slot) continue;
            ItemStack other = slot.getStack();
            if (Platform.isStackEmpty(other)) {
                slots.add(slot);
                stacks.add(Platform.EMPTY_STACK);
            } else if (Platform.canItemStacksStack(item, other)) {
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
        IItemLocation sourceLoc = IItemLocation.of(source);
        List<Slot> slots = candidates.getLeft();
        List<ItemStack> stacks = candidates.getRight();
        // total amount of moved items
        int total = Platform.getCount(oldSource) - Platform.getCount(source.getStack());
        if (total <= 0) return;
        List<ItemMovePacket> packets = new ArrayList<>();
        Int2ObjectArrayMap<ItemStack> stagedVirtualStacks = new Int2ObjectArrayMap<>();
        boolean error = false;
        long time = NEA.time();
        for (int i = 0; i < slots.size(); i++) {
            IItemLocation slot = IItemLocation.of(slots.get(i));
            if (slot == sourceLoc) continue;
            ItemStack oldStack = stacks.get(i);
            ItemStack newStack = slot.nea$getStack();
            if (Platform.isStackEmpty(oldStack)) {
                // was empty
                if (!Platform.isStackEmpty(newStack)) {
                    // now it's not empty -> found
                    ItemMovePacket packet = new ItemMovePacket(time, sourceLoc, slot, newStack.copy());
                    packets.add(packet);
                    total -= Platform.getCount(newStack);
                    stagedVirtualStacks.put(slot.nea$getSlotNumber(), oldStack);
                }
            } else if (Platform.canItemStacksStack(newStack, oldStack)) {
                // the stackable check is not really necessary but is still here for safety
                if (Platform.getCount(oldStack) < Platform.getCount(newStack)) {
                    // stackable and amount changed -> found
                    ItemStack movingStack = newStack.copy();
                    Platform.shrink(movingStack, Platform.getCount(oldStack));
                    ItemMovePacket packet = new ItemMovePacket(time, sourceLoc, slot, movingStack);
                    packets.add(packet);
                    total -= Platform.getCount(movingStack);
                    stagedVirtualStacks.put(slot.nea$getSlotNumber(), oldStack);
                } else if (Platform.getCount(oldStack) > Platform.getCount(newStack)) {
                    // what
                    NEA.LOGGER.error(
                        "After shift clicking a target slot ({}) now has less items than before!",
                        slot.nea$getSlotNumber());
                    error = true;
                }
            } else {
                // what
                NEA.LOGGER.error(
                    "After shift clicking a target slot ({}) now has a different item than before!",
                    slot.nea$getSlotNumber());
                error = true;
            }
            if (total <= 0) break;
        }
        if (total < 0) {
            NEA.LOGGER.error(
                "The original stack had {} items, but {} items where moved!",
                Platform.getCount(oldSource),
                Platform.getCount(oldSource) - total);
        }
        if (error || packets.isEmpty()) return;
        queueAnimation(sourceLoc.nea$getSlotNumber(), packets);
        for (var iterator = stagedVirtualStacks.int2ObjectEntrySet()
            .fastIterator(); iterator.hasNext();) {
            var e = iterator.next();
            updateVirtualStack(e.getIntKey(), e.getValue(), 1);
        }
        lastAnimation = NEA.time();
    }

    /**
     * Render all animated item stacks.
     */
    public static void drawAnimations() {
        drawAnimations(RenderItem.getInstance(), Minecraft.getMinecraft().fontRenderer);
    }

    /**
     * Render all animated item stacks.
     */
    public static void drawAnimations(RenderItem itemRender, FontRenderer fontRenderer) {
        for (var iter = movingItemsBySource.values()
            .iterator(); iter.hasNext();) {
            List<ItemMovePacket> packets = iter.next();
            for (Iterator<ItemMovePacket> iterator = packets.iterator(); iterator.hasNext();) {
                ItemMovePacket packet = iterator.next();
                boolean end = false;
                float val = packet.value();
                if (val >= 1f) {
                    val = 1f;
                    end = true;
                }
                int x = packet.getDrawX(val);
                int y = packet.getDrawY(val);
                GlStateManager.translate(0, 0, 32f);
                Platform.drawItem(itemRender, packet.getMovingStack(), x, y, fontRenderer);
                if (end) {
                    ItemMoveAnimation.updateVirtualStack(
                        packet.getTarget()
                            .nea$getSlotNumber(),
                        packet.getTargetStack(),
                        -1);
                    if (packets.size() == 1) {
                        iter.remove();
                        break;
                    }
                    iterator.remove();
                }
            }
            if (packets.isEmpty()) iter.remove();
        }
    }
}
