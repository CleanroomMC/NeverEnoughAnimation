package com.cleanroommc.neverenoughanimations.animations;

import com.cleanroommc.neverenoughanimations.IItemLocation;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class ItemMovePacket {

    private final long time;
    private final IItemLocation source, target;
    private final ItemStack movingStack;
    private final ItemStack targetStack;

    public ItemMovePacket(long time, IItemLocation source, IItemLocation target, ItemStack movingStack) {
        this.time = time;
        this.source = source;
        this.target = target;
        this.targetStack = target.nea$getStack().copy();
        this.movingStack = movingStack;
    }

    public long getTime() {
        return time;
    }

    public ItemStack getMovingStack() {
        return movingStack;
    }

    public ItemStack getTargetStack() {
        return targetStack;
    }

    public IItemLocation getSource() {
        return source;
    }

    public IItemLocation getTarget() {
        return target;
    }

    public float value() {
        return Math.min(1f, (Minecraft.getSystemTime() - this.time) / (float) NEAConfig.moveAnimationTime);
    }

    public int getDrawX(float value) {
        return (int) NEAConfig.moveAnimationCurve.interpolate(source.nea$getX(), target.nea$getX(), value);
    }

    public int getDrawY(float value) {
        return (int) NEAConfig.moveAnimationCurve.interpolate(source.nea$getY(), target.nea$getY(), value);
    }
}
