package daripher.skilltree.mixin.minecraft;

import daripher.skilltree.item.ItemHelper;
import daripher.skilltree.skill.bonus.item.FoodEffectBonus;
import daripher.skilltree.skill.bonus.item.FoodSaturationBonus;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import java.util.ArrayList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Item.class)
public abstract class ItemMixin implements IItemExtension {
  @Redirect(
      method = "getBarWidth",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/item/Item;getMaxDamage(Lnet/minecraft/world/item/ItemStack;)I"))
  private int applyBonusDurability(Item item, ItemStack stack) {
    return stack.getMaxDamage();
  }

  @Override
  public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
    FoodProperties properties = IItemExtension.super.getFoodProperties(stack, entity);
    if (properties == null) return null;
    float restorationMultiplier = 1f;
    restorationMultiplier +=
        ItemHelper.getItemBonuses(stack, FoodSaturationBonus.class).stream()
            .map(FoodSaturationBonus::getMultiplier)
            .reduce(Float::sum)
            .orElse(0f);
    List<MobEffectInstance> bonusEffects =
        ItemHelper.getItemBonuses(stack, FoodEffectBonus.class).stream()
            .map(FoodEffectBonus::getEffectInstance)
            .map(MobEffectInstance::new)
            .toList();
    if (restorationMultiplier == 1f && bonusEffects.isEmpty()) return properties;
    List<FoodProperties.PossibleEffect> effects = new ArrayList<>(properties.effects());
    bonusEffects.forEach(effect -> effects.add(new FoodProperties.PossibleEffect(() -> effect, 1f)));
    return new FoodProperties(
        (int) (properties.nutrition() * restorationMultiplier),
        properties.saturation() * restorationMultiplier,
        properties.canAlwaysEat(),
        properties.eatSeconds(),
        properties.usingConvertsTo(),
        effects);
  }
}
