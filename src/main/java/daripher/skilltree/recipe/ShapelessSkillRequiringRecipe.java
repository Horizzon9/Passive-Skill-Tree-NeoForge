package daripher.skilltree.recipe;

import com.mojang.serialization.MapCodec;
import daripher.skilltree.init.PSTRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ShapelessSkillRequiringRecipe extends ShapelessRecipe implements SkillRequiringRecipe {
  public ShapelessSkillRequiringRecipe(
      String group,
      CraftingBookCategory category,
      ItemStack result,
      net.minecraft.core.NonNullList<net.minecraft.world.item.crafting.Ingredient> ingredients) {
    super(group, category, result, ingredients);
  }

  @Override
  public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
    if (isUncraftable(this)) return false;
    return super.matches(input, level);
  }

  @Override
  public @NotNull ItemStack assemble(
      @NotNull CraftingInput input, @NotNull HolderLookup.Provider registries) {
    if (isUncraftable(this)) return ItemStack.EMPTY;
    return super.assemble(input, registries);
  }

  @Override
  public @NotNull RecipeSerializer<?> getSerializer() {
    return PSTRecipeSerializers.SHAPELESS_CRAFTING.get();
  }

  public static class Serializer implements RecipeSerializer<ShapelessSkillRequiringRecipe> {
    public static final MapCodec<ShapelessSkillRequiringRecipe> CODEC =
        RecipeSerializer.SHAPELESS_RECIPE.codec().xmap(
            recipe ->
                new ShapelessSkillRequiringRecipe(
                    recipe.getGroup(),
                    recipe.category(),
                    recipe.getResultItem(null),
                    recipe.getIngredients()),
            recipe -> recipe);

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessSkillRequiringRecipe>
        STREAM_CODEC =
            RecipeSerializer.SHAPELESS_RECIPE.streamCodec().map(
                recipe ->
                    new ShapelessSkillRequiringRecipe(
                        recipe.getGroup(),
                        recipe.category(),
                        recipe.getResultItem(null),
                        recipe.getIngredients()),
                recipe -> recipe);

    @Override
    public @NotNull MapCodec<ShapelessSkillRequiringRecipe> codec() {
      return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, ShapelessSkillRequiringRecipe>
        streamCodec() {
      return STREAM_CODEC;
    }
  }
}