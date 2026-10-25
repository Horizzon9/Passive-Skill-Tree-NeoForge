package daripher.skilltree.mixin.itemproduction;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import daripher.itemproduction.ItemProductionLib;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SmithingMenu.class, priority = 900)
public abstract class SmithingMenuProductionMixin extends ItemCombinerMenu {
  @SuppressWarnings("DataFlowIssue")
  private SmithingMenuProductionMixin() {
    super(null, 0, null, null);
  }

  @Inject(
      method = "createResult",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V",
              shift = At.Shift.BEFORE,
              ordinal = 1))
  private void itemProduced(
      CallbackInfo callbackInfo, @Local(ordinal = 0) LocalRef<ItemStack> stack) {
    if (inputSlots.getItem(1).getItem() == stack.get().getItem()) return;
    stack.set(ItemProductionLib.itemProduced(stack.get(), player));
  }
}
