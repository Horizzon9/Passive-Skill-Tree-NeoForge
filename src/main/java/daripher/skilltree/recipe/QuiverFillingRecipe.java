package daripher.skilltree.recipe;

import daripher.skilltree.init.PSTRecipeSerializers;
import daripher.skilltree.item.ItemHelper;
import daripher.skilltree.item.quiver.QuiverItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class QuiverFillingRecipe extends CustomRecipe {
  public QuiverFillingRecipe(CraftingBookCategory category) {
    super(category);
  }

  @Override
  public boolean matches(CraftingInput input, @NotNull Level level) {
    ItemStack quiver = ItemStack.EMPTY;
    ItemStack arrows = ItemStack.EMPTY;
    int itemsCount = 0;
    for (int slot = 0; slot < input.size(); slot++) {
      ItemStack stack = input.getItem(slot);
      if (stack.isEmpty()) continue;
      itemsCount++;
      if (ItemHelper.isQuiver(stack)) quiver = stack;
      if (stack.is(ItemTags.ARROWS)) arrows = stack;
    }
    if (itemsCount != 2) return false;
    if (quiver.isEmpty() || arrows.isEmpty()) return false;
    if (QuiverItem.isFull(quiver)) return false;
    return !QuiverItem.containsArrows(quiver)
        || ItemStack.isSameItemSameComponents(QuiverItem.getArrows(quiver), arrows);
  }

  @Override
  public @NotNull ItemStack assemble(CraftingInput input, @NotNull HolderLookup.Provider registries) {
    ItemStack quiver = ItemStack.EMPTY;
    ItemStack arrows = ItemStack.EMPTY;
    for (int slot = 0; slot < input.size(); slot++) {
      ItemStack stack = input.getItem(slot);
      if (stack.isEmpty()) continue;
      if (ItemHelper.isQuiver(stack)) quiver = stack;
      if (stack.is(ItemTags.ARROWS)) arrows = stack;
    }
    ItemStack result = quiver.copy();
    int capacity = QuiverItem.getCapacity(result);
    int arrowsTaken = Math.min(capacity - QuiverItem.getArrowsCount(result), arrows.getCount());
    QuiverItem.addArrows(result, arrows, arrowsTaken);
    return result;
  }

  @Override
  public @NotNull NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
    NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
    ItemStack quiver = ItemStack.EMPTY;
    for (int slot = 0; slot < input.size(); slot++) {
      ItemStack stack = input.getItem(slot);
      if (stack.isEmpty()) continue;
      if (ItemHelper.isQuiver(stack)) {
        quiver = stack;
        continue;
      }
      if (stack.is(ItemTags.ARROWS)) {
        int capacity = QuiverItem.getCapacity(quiver);
        int arrowsTaken = Math.min(capacity - QuiverItem.getArrowsCount(quiver), stack.getCount());
        if (arrowsTaken < stack.getCount()) {
          ItemStack leftover = stack.copy();
          leftover.setCount(stack.getCount() - arrowsTaken);
          remaining.set(slot, leftover);
        }
      } else if (stack.hasCraftingRemainingItem()) {
        remaining.set(slot, stack.getCraftingRemainingItem());
      }
    }
    return remaining;
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return width * height >= 2;
  }

  @Override
  public @NotNull RecipeSerializer<?> getSerializer() {
    return PSTRecipeSerializers.QUIVER_FILLING.get();
  }

  public static class Serializer implements RecipeSerializer<QuiverFillingRecipe> {
    private final RecipeSerializer<QuiverFillingRecipe> delegate =
        new SimpleCraftingRecipeSerializer<>(QuiverFillingRecipe::new);

    @Override
    public @NotNull com.mojang.serialization.MapCodec<QuiverFillingRecipe> codec() {
      return delegate.codec();
    }

    @Override
    public @NotNull net.minecraft.network.codec.StreamCodec<
            RegistryFriendlyByteBuf, QuiverFillingRecipe>
        streamCodec() {
      return delegate.streamCodec();
    }
  }
}