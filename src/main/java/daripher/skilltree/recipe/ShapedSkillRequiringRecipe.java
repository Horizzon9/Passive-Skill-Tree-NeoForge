package daripher.skilltree.recipe;

import com.mojang.serialization.MapCodec;
import daripher.skilltree.init.PSTRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ShapedSkillRequiringRecipe extends ShapedRecipe implements SkillRequiringRecipe {
  public ShapedSkillRequiringRecipe(
      String group,
      CraftingBookCategory category,
      ShapedRecipePattern pattern,
      ItemStack result,
      boolean showNotification) {
    super(group, category, pattern, result, showNotification);
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
    return PSTRecipeSerializers.SHAPED_CRAFTING.get();
  }

  public static class Serializer implements RecipeSerializer<ShapedSkillRequiringRecipe> {
    public static final MapCodec<ShapedSkillRequiringRecipe> CODEC =
        RecipeSerializer.SHAPED_RECIPE.codec().xmap(
            recipe ->
                new ShapedSkillRequiringRecipe(
                    recipe.getGroup(),
                    recipe.category(),
                    recipe.pattern,
                    recipe.getResultItem(null),
                    recipe.showNotification()),
            recipe -> recipe);

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedSkillRequiringRecipe>
        STREAM_CODEC =
            RecipeSerializer.SHAPED_RECIPE.streamCodec().map(
                recipe ->
                    new ShapedSkillRequiringRecipe(
                        recipe.getGroup(),
                        recipe.category(),
                        recipe.pattern,
                        recipe.getResultItem(null),
                        recipe.showNotification()),
                recipe -> recipe);

    @Override
    public @NotNull MapCodec<ShapedSkillRequiringRecipe> codec() {
      return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, ShapedSkillRequiringRecipe> streamCodec() {
      return STREAM_CODEC;
    }
  }
}