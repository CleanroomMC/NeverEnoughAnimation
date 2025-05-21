package com.cleanroommc.neverenoughanimations.core.mixin.late.mousetweaks;
/*
 * @Mixin(value = Main.class, remap = false)
 * public abstract class MainMixin {
 * @Shadow private static IGuiScreenHandler handler;
 * @Shadow
 * private static Slot findWheelApplicableSlot(List<Slot> slots, Slot selectedSlot, boolean pushItems) {
 * return null;
 * }
 * @Redirect(method = "handleWheel", at = @At(value = "INVOKE", target =
 * "Lyalter/mousetweaks/Main;findWheelApplicableSlot(Ljava/util/List;Lnet/minecraft/inventory/Slot;Z)Lnet/minecraft/inventory/Slot;"
 * ))
 * private static Slot handleWheelPre(List<Slot> slots, Slot selectedSlot, boolean pushItems,
 * @Local(name = "originalStack") ItemStack stack,
 * @Share("candidates") LocalRef<Pair<List<Slot>, List<ItemStack>>> candidates,
 * @Share("sourceStack") LocalRef<ItemStack> sourceStack,
 * @Share("sourceSlot") LocalRef<Slot> sourceSlot) {
 * Slot applicableSlot = findWheelApplicableSlot(slots, selectedSlot, pushItems);
 * if (applicableSlot == null) return null;
 * if (pushItems) {
 * sourceStack.set(stack);
 * sourceSlot.set(selectedSlot);
 * candidates.set(ItemMoveAnimation.getCandidates(selectedSlot, handler.getSlots()));
 * } else {
 * sourceStack.set(applicableSlot.getStack().copy());
 * sourceSlot.set(applicableSlot);
 * candidates.set(ItemMoveAnimation.getCandidates(applicableSlot, handler.getSlots()));
 * }
 * return applicableSlot;
 * }
 * @Inject(method = "handleWheel", at = @At("RETURN"))
 * private static void handleWheelPost(Slot selectedSlot, CallbackInfo ci,
 * @Share("candidates") LocalRef<Pair<List<Slot>, List<ItemStack>>> candidates,
 * @Share("sourceStack") LocalRef<ItemStack> sourceStack,
 * @Share("sourceSlot") LocalRef<Slot> sourceSlot) {
 * if (candidates.get() != null) {
 * ItemMoveAnimation.handleMove(sourceSlot.get(), sourceStack.get(), candidates.get());
 * }
 * }
 * }
 */
