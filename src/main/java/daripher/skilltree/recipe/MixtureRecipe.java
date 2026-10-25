package daripher.skilltree.recipe;

import daripher.skilltree.init.PSTRecipeSerializers;
import daripher.skilltree.potion.PotionHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MixtureRecipe extends CustomRecipe implements SkillRequiringRecipe {
  public MixtureRecipe(CraftingBookCategory category) {
    super(category);
  }

  @Override
  public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
    if (isUncraftable(this)) return false;
    ItemStack potionStack1 = ItemStack.EMPTY;
    ItemStack potionStack2 = ItemStack.EMPTY;
    int potionsCount = 0;
    for (int slot = 0; slot < input.size(); slot++) {
      ItemStack stackInSlot = input.getItem(slot);
      if (stackInSlot.isEmpty()) continue;
      if (PotionHelper.isPotion(stackInSlot) && !PotionHelper.isMixture(stackInSlot)) {
        potionsCount++;
        if (potionStack1.isEmpty()) {
          potionStack1 = stackInSlot;
        } else {
          potionStack2 = stackInSlot;
        }
      }
    }
    if (PotionHelper.getMobEffects(potionStack1).isEmpty()
        || PotionHelper.getMobEffects(potionStack2).isEmpty()) {
      return false;
    }
    return potionsCount == 2 && potionStack1.getItem() == potionStack2.getItem();
  }

  @Override
  public @NotNull ItemStack assemble(
      @NotNull CraftingInput input, @NotNull HolderLookup.Provider registries) {
    if (isUncraftable(this)) return ItemStack.EMPTY;
    ItemStack potionStack1 = ItemStack.EMPTY;
    ItemStack potionStack2 = ItemStack.EMPTY;
    for (int slot = 0; slot < input.size(); slot++) {
      ItemStack stackInSlot = input.getItem(slot);
      if (stackInSlot.isEmpty()) continue;
      if (PotionHelper.isPotion(stackInSlot) && !PotionHelper.isMixture(stackInSlot)) {
        if (potionStack1.isEmpty()) {
          potionStack1 = stackInSlot;
        } else {
          potionStack2 = stackInSlot;
        }
      }
    }
    return PotionHelper.mixPotions(potionStack1, potionStack2);
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return width * height >= 2;
  }

  @Override
  public @NotNull RecipeSerializer<?> getSerializer() {
    return PSTRecipeSerializers.POTION_MIXING.get();
  }

  public static class Serializer implements RecipeSerializer<MixtureRecipe> {
    private final RecipeSerializer<MixtureRecipe> delegate =
        new SimpleCraftingRecipeSerializer<>(MixtureRecipe::new);

    @Override
    public @NotNull com.mojang.serialization.MapCodec<MixtureRecipe> codec() {
      return delegate.codec();
    }

    @Override
    public @NotNull net.minecraft.network.codec.StreamCodec<
            RegistryFriendlyByteBuf, MixtureRecipe>
        streamCodec() {
      return delegate.streamCodec();
    }
  }
}