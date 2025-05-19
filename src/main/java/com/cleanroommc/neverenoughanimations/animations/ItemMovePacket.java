package com.cleanroommc.neverenoughanimations.animations;

import com.cleanroommc.neverenoughanimations.api.IItemLocation;
import com.cleanroommc.neverenoughanimations.NEA;
import com.cleanroommc.neverenoughanimations.NEAConfig;
import com.cleanroommc.neverenoughanimations.util.Platform;
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
        this.targetStack = Platform.copyStack(target.nea$getStack());
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
        return Math.min(1f, (NEA.time() - this.time) / (float) NEAConfig.moveAnimationTime);
    }

    public int getDrawX(float value) {
        return (int) NEAConfig.moveAnimationCurve.interpolate(source.nea$getX(), target.nea$getX(), value);
    }

    public int getDrawY(float value) {
        return (int) NEAConfig.moveAnimationCurve.interpolate(source.nea$getY(), target.nea$getY(), value);
    }
}
