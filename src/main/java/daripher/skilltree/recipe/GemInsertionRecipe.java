package daripher.skilltree.recipe;

import com.mojang.serialization.MapCodec;
import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.init.PSTItems;
import daripher.skilltree.init.PSTRecipeSerializers;
import daripher.skilltree.item.ItemHelper;
import daripher.skilltree.item.gem.GemItem;
import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class GemInsertionRecipe extends SmithingTransformRecipe {
  public GemInsertionRecipe() {
    super(Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.EMPTY, ItemStack.EMPTY);
  }

  @Override
  public boolean matches(@NotNull SmithingRecipeInput input, @NotNull Level level) {
    if (SkillTreeMod.apotheosisEnabled()) return false;
    return canCraftIn(input);
  }

  @Override
  public @NotNull ItemStack assemble(
      @NotNull SmithingRecipeInput input, @NotNull HolderLookup.Provider registries) {
    if (SkillTreeMod.apotheosisEnabled() || !canCraftIn(input)) return ItemStack.EMPTY;
    ItemStack base = input.base();
    ItemStack ingredient = input.addition();
    ItemStack result = base.copy();
    result.setCount(1);
    Player player = RecipePlayerHolder.get();
    Objects.requireNonNull(player);
    GemItem.insertGem(player, result, ingredient);
    return result;
  }

  private boolean canCraftIn(@NotNull SmithingRecipeInput input) {
    Player player = RecipePlayerHolder.get();
    if (player == null) return false;
    ItemStack base = input.base();
    ItemStack ingredient = input.addition();
    if (ingredient.getItem() != PSTItems.GEM.get()) return false;
    return GemItem.canInsertGem(player, base, ingredient);
  }

  @Override
  public boolean isBaseIngredient(@NotNull ItemStack stack) {
    return ItemHelper.canInsertGem(stack);
  }

  @Override
  public boolean isAdditionIngredient(@NotNull ItemStack stack) {
    return stack.getItem() instanceof GemItem;
  }

  @Override
  public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider registries) {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean isSpecial() {
    return true;
  }

  @Override
  public @NotNull RecipeSerializer<?> getSerializer() {
    return PSTRecipeSerializers.GEM_INSERTION.get();
  }

  @Override
  public boolean isIncomplete() {
    return false;
  }

  public static class Serializer implements RecipeSerializer<GemInsertionRecipe> {
    public static final MapCodec<GemInsertionRecipe> CODEC = MapCodec.unit(GemInsertionRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, GemInsertionRecipe> STREAM_CODEC =
        StreamCodec.of((buf, recipe) -> {}, buf -> new GemInsertionRecipe());

    @Override
    public @NotNull MapCodec<GemInsertionRecipe> codec() {
      return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, GemInsertionRecipe> streamCodec() {
      return STREAM_CODEC;
    }
  }
}