package com.cleanroommc.neverenoughanimations.api;

import net.minecraft.client.gui.GuiScreen;

/**
 * Implementing this on your {@link net.minecraft.client.gui.GuiScreen GuiScreen} enables the opening/closing animation.
 * It is automatically implemented for every {@link net.minecraft.client.gui.inventory.GuiContainer GuiContainer}. If you implemented it
 * manually on your {@link net.minecraft.client.gui.GuiScreen GuiScreen} you will need to call
 * {@link com.cleanroommc.neverenoughanimations.animations.OpeningAnimation#handleScale(GuiScreen, boolean) OpeningAnimation#handleScale(GuiScreen, boolean)}
 * yourself right after you rendered any background which covers the whole screen (since you don't want it to scale) and right before you
 * draw the screen.
 */
public interface IAnimatedScreen {

    /**
     * X position of the main panel. (0 if it covers the whole screen)
     * @return x pos
     */
    int nea$getX();

    /**
     * Y position of the main panel. (0 if it covers the whole screen)
     * @return y pos
     */
    int nea$getY();

    /**
     * Width of the main panel. (screen width if it covers the whole screen)
     * @return width
     */
    int nea$getWidth();

    /**
     * Height of the main panel. (screen height if it covers the whole screen)
     * @return height
     */
    int nea$getHeight();

}
