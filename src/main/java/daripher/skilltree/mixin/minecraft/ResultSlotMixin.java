package daripher.skilltree.mixin.minecraft;

import daripher.itemproduction.ItemProductionLib;
import daripher.skilltree.recipe.RecipePlayerHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin extends Slot {
  private @Shadow @Final Player player;
  private @Unique ItemStack skilltree$processedStack = ItemStack.EMPTY;

  @SuppressWarnings("DataFlowIssue")
  public ResultSlotMixin() {
    super(null, 0, 0, 0);
  }

  @Override
  public ItemStack getItem() {
    ItemStack stack = super.getItem();
    if (stack.isEmpty() || stack == skilltree$processedStack) return stack;
    if (player == null || player.level().isClientSide) return stack;
    ItemStack processed = ItemProductionLib.itemProduced(stack.copy(), player);
    skilltree$processedStack = processed;
    container.setItem(getContainerSlot(), processed);
    return processed;
  }

  @Inject(method = "onTake", at = @At("HEAD"))
  private void setCraftingPlayer(Player player, ItemStack stack, CallbackInfo callbackInfo) {
    RecipePlayerHolder.set(player);
  }

  @Inject(method = "onTake", at = @At("RETURN"))
  private void clearCraftingPlayer(Player player, ItemStack stack, CallbackInfo callbackInfo) {
    RecipePlayerHolder.clear();
  }
}
