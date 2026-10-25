package daripher.skilltree.mixin.minecraft;

import daripher.skilltree.recipe.RecipePlayerHolder;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public class CraftingMenuMixin {
  @Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"))
  private static void setCraftingPlayer(
      AbstractContainerMenu menu,
      Level level,
      Player player,
      CraftingContainer craftSlots,
      ResultContainer resultSlots,
      @Nullable RecipeHolder<?> recipe,
      CallbackInfo callbackInfo) {
    RecipePlayerHolder.set(player);
  }

  @Inject(method = "slotChangedCraftingGrid", at = @At("RETURN"))
  private static void clearCraftingPlayer(
      AbstractContainerMenu menu,
      Level level,
      Player player,
      CraftingContainer craftSlots,
      ResultContainer resultSlots,
      @Nullable RecipeHolder<?> recipe,
      CallbackInfo callbackInfo) {
    RecipePlayerHolder.clear();
  }
}
