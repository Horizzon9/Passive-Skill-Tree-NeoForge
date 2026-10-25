package daripher.skilltree.mixin.minecraft;

import daripher.skilltree.recipe.RecipePlayerHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public class ResultSlotMixin {
  @Inject(method = "onTake", at = @At("HEAD"))
  private void setCraftingPlayer(Player player, ItemStack stack, CallbackInfo callbackInfo) {
    RecipePlayerHolder.set(player);
  }

  @Inject(method = "onTake", at = @At("RETURN"))
  private void clearCraftingPlayer(Player player, ItemStack stack, CallbackInfo callbackInfo) {
    RecipePlayerHolder.clear();
  }
}
