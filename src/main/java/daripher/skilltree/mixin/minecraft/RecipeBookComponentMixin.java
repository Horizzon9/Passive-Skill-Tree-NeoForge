package daripher.skilltree.mixin.minecraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import daripher.skilltree.mixin.RecipeCollectionAccessor;
import daripher.skilltree.recipe.SkillRequiringRecipe;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RecipeBookComponent.class)
public class RecipeBookComponentMixin {
  @ModifyExpressionValue(
      method = "updateCollections",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lcom/google/common/collect/Lists;newArrayList(Ljava/lang/Iterable;)Ljava/util/ArrayList;"))
  private ArrayList<RecipeCollection> removeUncraftableRecipes(
      ArrayList<RecipeCollection> original) {
    original.forEach(this::removeUncraftableRecipes);
    return original;
  }

  private void removeUncraftableRecipes(RecipeCollection recipeCollection) {
    RecipeCollectionAccessor accessor = (RecipeCollectionAccessor) recipeCollection;
    accessor.getCraftable().removeIf(this::isUncraftable);
  }

  private boolean isUncraftable(RecipeHolder<?> recipe) {
    if (!(recipe.value() instanceof SkillRequiringRecipe skillRecipe)) return false;
    return skillRecipe.isUncraftable(Minecraft.getInstance().player, recipe.value());
  }
}
