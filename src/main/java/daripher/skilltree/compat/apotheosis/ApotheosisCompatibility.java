package daripher.skilltree.compat.apotheosis;

import com.google.common.collect.ImmutableList;
import daripher.skilltree.skill.bonus.item.ItemBonus;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public enum ApotheosisCompatibility {
  INSTANCE;

  public void register() {}

  public List<? extends ItemBonus<?>> getGemBonuses(ItemStack stack) {
    return ImmutableList.of();
  }

  public List<ItemStack> getGemStacks(ItemStack stack) {
    return List.of();
  }

  public void createGemStack(
      Consumer<ItemStack> consumer, LootContext context, ResourceLocation gemTypeId) {}

  public ItemStack getGemStack(ResourceLocation gemTypeId) {
    return ItemStack.EMPTY;
  }

  public @Nullable ResourceLocation getGemId(ItemStack stack) {
    return null;
  }

  public boolean adventureModuleEnabled() {
    return false;
  }
}