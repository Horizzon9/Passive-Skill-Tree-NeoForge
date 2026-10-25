package daripher.skilltree.recipe;

import daripher.skilltree.skill.bonus.SkillBonusHandler;
import daripher.skilltree.skill.bonus.player.RecipeUnlockBonus;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public interface SkillRequiringRecipe {
  Map<Recipe<?>, ResourceLocation> RECIPE_IDS = new WeakHashMap<>();

  default boolean isUncraftable(Recipe<?> recipe) {
    return isUncraftable(RecipePlayerHolder.get(), recipe);
  }

  default boolean isUncraftable(@Nullable Player player, Recipe<?> recipe) {
    if (player == null) return true;
    ResourceLocation id = getRecipeId(player, recipe);
    if (id == null) return true;
    return SkillBonusHandler.getSkillBonuses(player, RecipeUnlockBonus.class).stream()
        .noneMatch(bonus -> id.equals(bonus.getRecipeId()));
  }

  private static @Nullable ResourceLocation getRecipeId(Player player, Recipe<?> recipe) {
    synchronized (RECIPE_IDS) {
      ResourceLocation cached = RECIPE_IDS.get(recipe);
      if (cached != null) return cached;
      ResourceLocation id =
          player.level().getRecipeManager().getRecipes().stream()
              .filter(holder -> holder.value() == recipe)
              .map(RecipeHolder::id)
              .findFirst()
              .orElse(null);
      if (id != null) RECIPE_IDS.put(recipe, id);
      return id;
    }
  }
}
