package daripher.skilltree.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.compat.apotheosis.ApotheosisCompatibility;
import daripher.skilltree.data.reloader.GemTypesReloader;
import daripher.skilltree.init.PSTRecipeSerializers;
import daripher.skilltree.item.gem.GemItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class GemUpgradeRecipe
    implements net.minecraft.world.item.crafting.CraftingRecipe {
  private final ResourceLocation ingredient;
  private final ResourceLocation result;

  public GemUpgradeRecipe(ResourceLocation ingredient, ResourceLocation result) {
    this.ingredient = ingredient;
    this.result = result;
  }

  @Override
  public @NotNull String getGroup() {
    return "gem_upgrades";
  }

  @Override
  public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider registries) {
    if (SkillTreeMod.apotheosisEnabled()) {
      return ApotheosisCompatibility.INSTANCE.getGemStack(result);
    }
    return GemItem.getDefaultGemStack(GemTypesReloader.getGemTypeById(result));
  }

  @Override
  public @NotNull NonNullList<Ingredient> getIngredients() {
    Ingredient ingredient = getIngredient();
    return NonNullList.withSize(9, ingredient);
  }

  private Ingredient getIngredient() {
    ItemStack gemStack;
    if (SkillTreeMod.apotheosisEnabled()) {
      gemStack = ApotheosisCompatibility.INSTANCE.getGemStack(this.ingredient);
    } else {
      gemStack = GemItem.getDefaultGemStack(GemTypesReloader.getGemTypeById(this.ingredient));
    }
    return Ingredient.of(gemStack);
  }

  @Override
  public boolean matches(CraftingInput input, @NotNull Level level) {
    int gemsCount = 0;
    for (int j = 0; j < input.size(); j++) {
      ItemStack stack = input.getItem(j);
      if (!isIngredient(stack)) return false;
      gemsCount++;
    }
    return gemsCount == 9;
  }

  private boolean isIngredient(ItemStack stack) {
    if (SkillTreeMod.apotheosisEnabled()) {
      return ingredient.equals(ApotheosisCompatibility.INSTANCE.getGemId(stack));
    }
    return GemTypesReloader.getGemTypeById(ingredient) == GemItem.getGemType(stack);
  }

  @Override
  public @NotNull ItemStack assemble(
      CraftingInput input, @NotNull HolderLookup.Provider registries) {
    return getResultItem(registries).copy();
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return width * height >= 9;
  }

  @Override
  public @NotNull CraftingBookCategory category() {
    return CraftingBookCategory.MISC;
  }

  @Override
  public @NotNull RecipeSerializer<?> getSerializer() {
    return PSTRecipeSerializers.GEM_UPGRADE.get();
  }

  public static class Serializer implements RecipeSerializer<GemUpgradeRecipe> {
    public static final MapCodec<GemUpgradeRecipe> CODEC =
        RecordCodecBuilder.mapCodec(
            instance ->
                instance
                    .group(
                        Codec.STRING
                            .fieldOf("ingredient")
                            .forGetter(recipe -> recipe.ingredient.toString()),
                        Codec.STRING.fieldOf("result").forGetter(recipe -> recipe.result.toString()))
                    .apply(
                        instance,
                        (ingredient, result) ->
                            new GemUpgradeRecipe(
                                ResourceLocation.parse(ingredient), ResourceLocation.parse(result))));

    public static final StreamCodec<RegistryFriendlyByteBuf, GemUpgradeRecipe> STREAM_CODEC =
        StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            recipe -> recipe.ingredient,
            ResourceLocation.STREAM_CODEC,
            recipe -> recipe.result,
            GemUpgradeRecipe::new);

    @Override
    public @NotNull MapCodec<GemUpgradeRecipe> codec() {
      return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, GemUpgradeRecipe> streamCodec() {
      return STREAM_CODEC;
    }
  }
}