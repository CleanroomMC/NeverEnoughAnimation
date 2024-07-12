package com.cleanroommc.neverenoughanimations.animations;

import com.cleanroommc.neverenoughanimations.NEAConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ItemMovePacket {


    private final long time;
    private final Slot source, target;
    private final ItemStack movingStack;
    private final ItemStack targetStack;
    private boolean end = false;

    public ItemMovePacket(long time, Slot source, Slot target, ItemStack movingStack) {
        this.time = time;
        this.source = source;
        this.target = target;
        this.targetStack = target.getStack().copy();
        this.movingStack = movingStack;
    }

    public long getTime() {
        return time;
    }

    public ItemStack getMovingStack() {
        return movingStack;
    }

    public Slot getSource() {
        return source;
    }

    public Slot getTarget() {
        return target;
    }

    public boolean checkEnd() {
        if (this.end) return false;
        long dif = Minecraft.getSystemTime() - this.time;
        if (dif > NEAConfig.moveAnimationTime) {
            // end
            ItemMoveAnimation.updateVirtualStack(this.target.slotNumber, this.targetStack, -1);
            this.end = true;
            return false;
        }
        return true;
    }

    public float value() {
        return Math.min(1f, (Minecraft.getSystemTime() - this.time) / (float) NEAConfig.moveAnimationTime);
    }

    public int getDrawX(float value) {
        return (int) NEAConfig.moveAnimationCurve.interpolate(source.xPos, target.xPos, value);
    }

    public int getDrawY(float value) {
        return (int) NEAConfig.moveAnimationCurve.interpolate(source.yPos, target.yPos, value);
    }
}
