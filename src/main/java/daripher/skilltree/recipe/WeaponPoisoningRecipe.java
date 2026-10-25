package daripher.skilltree.recipe;

import daripher.skilltree.init.PSTRecipeSerializers;
import daripher.skilltree.item.ItemHelper;
import daripher.skilltree.potion.PotionHelper;
import daripher.skilltree.skill.bonus.condition.item.EquipmentCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class WeaponPoisoningRecipe extends CustomRecipe implements SkillRequiringRecipe {
  public WeaponPoisoningRecipe(CraftingBookCategory category) {
    super(category);
  }

  @Override
  public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
    if (isUncraftable(this)) return false;
    int weaponsCount = 0;
    int poisonsCount = 0;
    for (int slot = 0; slot < input.size(); slot++) {
      ItemStack stackInSlot = input.getItem(slot);
      if (stackInSlot.isEmpty()) continue;
      if (EquipmentCondition.isMeleeWeapon(stackInSlot)) {
        weaponsCount++;
        continue;
      }
      if (isPoison(stackInSlot)) poisonsCount++;
    }
    return weaponsCount == 1 && poisonsCount == 1;
  }

  @Override
  public @NotNull ItemStack assemble(
      @NotNull CraftingInput input, @NotNull HolderLookup.Provider registries) {
    if (isUncraftable(this)) return ItemStack.EMPTY;
    ItemStack weaponStack = ItemStack.EMPTY;
    ItemStack poisonStack = ItemStack.EMPTY;
    for (int slot = 0; slot < input.size(); slot++) {
      ItemStack stackInSlot = input.getItem(slot);
      if (stackInSlot.isEmpty()) continue;
      if (EquipmentCondition.isMeleeWeapon(stackInSlot)) weaponStack = stackInSlot;
      if (isPoison(stackInSlot)) poisonStack = stackInSlot;
    }
    ItemStack result = weaponStack.copy();
    ItemHelper.setPoisons(result, poisonStack);
    return result;
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return width * height >= 2;
  }

  @Override
  public @NotNull RecipeSerializer<?> getSerializer() {
    return PSTRecipeSerializers.WEAPON_POISONING.get();
  }

  private boolean isPoison(ItemStack stack) {
    return PotionHelper.getMobEffects(stack).stream()
        .anyMatch(
            effect -> {
              MobEffectInstance instance = effect;
              return instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL;
            });
  }

  public static class Serializer implements RecipeSerializer<WeaponPoisoningRecipe> {
    private final RecipeSerializer<WeaponPoisoningRecipe> delegate =
        new SimpleCraftingRecipeSerializer<>(WeaponPoisoningRecipe::new);

    @Override
    public @NotNull com.mojang.serialization.MapCodec<WeaponPoisoningRecipe> codec() {
      return delegate.codec();
    }

    @Override
    public @NotNull net.minecraft.network.codec.StreamCodec<
            RegistryFriendlyByteBuf, WeaponPoisoningRecipe>
        streamCodec() {
      return delegate.streamCodec();
    }
  }
}