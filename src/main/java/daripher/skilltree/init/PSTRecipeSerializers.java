package daripher.skilltree.init;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.recipe.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class PSTRecipeSerializers {
  public static final DeferredRegister<RecipeSerializer<?>> REGISTRY =
      DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, SkillTreeMod.MOD_ID);

  public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> GEM_INSERTION =
      REGISTRY.register("gem_insertion", GemInsertionRecipe.Serializer::new);
  public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> WEAPON_POISONING =
      REGISTRY.register("weapon_poisoning", WeaponPoisoningRecipe.Serializer::new);
  public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> POTION_MIXING =
      REGISTRY.register("potion_mixing", MixtureRecipe.Serializer::new);
  public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> SHAPED_CRAFTING =
      REGISTRY.register("crafting_shaped", ShapedSkillRequiringRecipe.Serializer::new);
  public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> SHAPELESS_CRAFTING =
      REGISTRY.register("crafting_shapeless", ShapelessSkillRequiringRecipe.Serializer::new);
  public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> QUIVER_FILLING =
      REGISTRY.register("quiver_filling", QuiverFillingRecipe.Serializer::new);
  public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> GEM_UPGRADE =
      REGISTRY.register("gem_upgrade", GemUpgradeRecipe.Serializer::new);
}
