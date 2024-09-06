package com.cleanroommc.neverenoughanimations.animations;

import com.cleanroommc.neverenoughanimations.IItemLocation;
import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ItemMoveAnimation {

    // all itemstack that are currently being animated
    private static final Int2ObjectOpenHashMap<List<ItemMovePacket>> movingItemsBySource = new Int2ObjectOpenHashMap<>();
    private static AbstractContainerScreen<?> lastGui = null;
    // when a stack is moving the stack is actually in the target slot all the time, so we need to trick the player
    // into thinking there is nothing
    private static final ObjectArrayList<ItemStack> virtualStacks = new ObjectArrayList<>(256);
    // we need to keep track on how many packets are modifying the virtual stack, so we know when we no longer need it
    private static final IntArrayList virtualStacksUser = new IntArrayList(256);
    private static long lastAnimation = 0;

    @ApiStatus.Internal
    public static void onGuiOpen(ScreenEvent.Opening event) {
        if (NEAConfig.moveAnimationTime == 0) return;
        if (!(event.getNewScreen() instanceof AbstractContainerScreen<?>)) {
            onGuiClose(lastGui);
            return;
        }
        if (!NEAConfig.isBlacklisted(event.getNewScreen())) {
            lastGui = (AbstractContainerScreen<?>) event.getNewScreen();
            movingItemsBySource.clear();
            virtualStacks.clear();
            virtualStacksUser.clear();
        }
    }

    public static void onGuiClose(Screen screen) {
        if (lastGui == screen) {
            lastGui = null;
            movingItemsBySource.clear();
        }
    }

    public static ItemStack getVirtualStack(AbstractContainerScreen<?> container, Slot slot) {
        return getVirtualStack(container, IItemLocation.of(slot));
    }

    public static ItemStack getVirtualStack(AbstractContainerScreen<?> container, IItemLocation slot) {
        return container == lastGui &&
                       !NEAConfig.isBlacklisted(container) &&
                       virtualStacks.size() > slot.nea$getSlotNumber() + 1 &&
                       virtualStacksUser.getInt(slot.nea$getSlotNumber() + 1) > 0 ? virtualStacks.get(slot.nea$getSlotNumber() + 1) : null;
    }

    @ApiStatus.Internal
    public static void updateVirtualStack(int target, ItemStack stack, int op) {
        if (NEAConfig.moveAnimationTime == 0) return;
        while (target + 1 >= virtualStacks.size()) {
            // ensure size and default values
            virtualStacks.add(null);
            virtualStacksUser.add(0);
        }
        int users = virtualStacksUser.getInt(target + 1) + op;
        if (users <= 0) {
            // no users left -> set to null
            stack = null;
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
        if (NEAConfig.moveAnimationTime == 0 ||
                NEAConfig.isBlacklisted(Minecraft.getInstance().screen) ||
                NEA.time() - lastAnimation <= 10) {
            return null;
        }
        List<Slot> slots = new ArrayList<>(allSlots.size());
        List<ItemStack> stacks = new ArrayList<>(allSlots.size());
        ItemStack item = IItemLocation.of(in).nea$getStack();
        for (Slot slot : allSlots) {
            if (in == slot) continue;
            ItemStack other = slot.getItem();
            if (other.isEmpty()) {
                slots.add(slot);
                stacks.add(ItemStack.EMPTY);
            } else if (ItemStack.isSameItemSameComponents(item, other)) {
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
        int total = oldSource.getCount() - source.getItem().getCount();
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
            if (oldStack.isEmpty()) {
                // was empty
                if (!newStack.isEmpty()) {
                    // now it's not empty -> found
                    ItemMovePacket packet = new ItemMovePacket(time, sourceLoc, slot, newStack.copy());
                    packets.add(packet);
                    total -= newStack.getCount();
                    stagedVirtualStacks.put(slot.nea$getSlotNumber(), oldStack);
                }
            } else if (ItemStack.isSameItemSameComponents(newStack, oldStack)) {
                // the stackable check is not really necessary but is still here for safety
                if (oldStack.getCount() < newStack.getCount()) {
                    // stackable and amount changed -> found
                    ItemStack movingStack = newStack.copy();
                    movingStack.shrink(oldStack.getCount());
                    ItemMovePacket packet = new ItemMovePacket(time, sourceLoc, slot, movingStack);
                    packets.add(packet);
                    total -= movingStack.getCount();
                    stagedVirtualStacks.put(slot.nea$getSlotNumber(), oldStack);
                } else if (oldStack.getCount() > newStack.getCount()) {
                    // what
                    NEA.LOGGER.error("After shift clicking a target slot ({}) now has less items than before!", slot.nea$getSlotNumber());
                    error = true;
                }
            } else {
                // what
                NEA.LOGGER.error("After shift clicking a target slot ({}) now has a different item than before!", slot.nea$getSlotNumber());
                error = true;
            }
            if (total <= 0) break;
        }
        if (total < 0) {
            NEA.LOGGER.error("The original stack had {} items, but {} items where moved!", oldSource.getCount(),
                             oldSource.getCount() - total);
        }
        if (error || packets.isEmpty()) return;
        queueAnimation(sourceLoc.nea$getSlotNumber(), packets);
        for (var iterator = stagedVirtualStacks.int2ObjectEntrySet().fastIterator(); iterator.hasNext(); ) {
            var e = iterator.next();
            updateVirtualStack(e.getIntKey(), e.getValue(), 1);
        }
        lastAnimation = NEA.time();
    }

    /**
     * Render all animated item stacks.
     */
    /*public static void drawAnimations() {
        drawAnimations(Minecraft.getInstance().getItemRenderer(), Minecraft.getInstance().font);
    }*/

    /**
     * Render all animated item stacks.
     */
    public static void drawAnimations(GuiGraphics graphics, Font font) {
        for (var iter = movingItemsBySource.values().iterator(); iter.hasNext(); ) {
            List<ItemMovePacket> packets = iter.next();
            for (Iterator<ItemMovePacket> iterator = packets.iterator(); iterator.hasNext(); ) {
                ItemMovePacket packet = iterator.next();
                boolean end = false;
                float val = packet.value();
                if (val >= 1f) {
                    val = 1f;
                    end = true;
                }
                int x = packet.getDrawX(val);
                int y = packet.getDrawY(val);
                graphics.pose().translate(0, 0, 32f);
                NEA.drawItem(packet.getMovingStack(), graphics, font, x, y);
                if (end) {
                    ItemMoveAnimation.updateVirtualStack(packet.getTarget().nea$getSlotNumber(), packet.getTargetStack(), -1);
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
