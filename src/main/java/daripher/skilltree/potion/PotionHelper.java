package daripher.skilltree.potion;

import daripher.skilltree.config.Config;
import daripher.skilltree.init.PSTComponents;
import daripher.skilltree.item.ItemHelper;
import daripher.skilltree.skill.bonus.item.PotionAmplificationBonus;
import daripher.skilltree.skill.bonus.item.PotionDurationBonus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;

public class PotionHelper {
  public static boolean isMixture(ItemStack stack) {
    return Boolean.TRUE.equals(stack.get(PSTComponents.MIXED_POTION.get()));
  }

  public static boolean isPotion(ItemStack stack) {
    return stack.getItem() instanceof PotionItem;
  }

  public static List<MobEffectInstance> getMobEffects(ItemStack stack) {
    PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    List<MobEffectInstance> effects = new ArrayList<>();
    contents.getAllEffects().forEach(effects::add);
    return effects;
  }

  public static PotionContents applyEffectBonuses(ItemStack stack, PotionContents contents) {
    if (contents == null) return null;
    float durationMultiplier = getDurationMultiplier(stack);
    int amplifierBonus = getAmplifierBonus(stack);
    if (durationMultiplier == 1f && amplifierBonus == 0) return contents;
    List<MobEffectInstance> effects = new ArrayList<>();
    contents
        .getAllEffects()
        .forEach(
            effect -> {
              int duration =
                  effect.getEffect().value().isInstantenous()
                      ? effect.getDuration()
                      : (int) (effect.getDuration() * durationMultiplier);
              int amplifier = effect.getAmplifier() + amplifierBonus;
              effects.add(new MobEffectInstance(effect.getEffect(), duration, amplifier));
            });
    return new PotionContents(Optional.empty(), Optional.of(contents.getColor()), effects);
  }

  public static int getAmplifierBonus(ItemStack stack) {
    return ItemHelper.getItemBonuses(stack, PotionAmplificationBonus.class).stream()
        .map(PotionAmplificationBonus::getChance)
        .reduce(Float::sum)
        .map(Float::intValue)
        .orElse(0);
  }

  public static float getDurationMultiplier(ItemStack stack) {
    float multiplier = 1f;
    multiplier += ItemHelper.getItemBonuses(stack, PotionDurationBonus.class).stream()
        .map(PotionDurationBonus::getMultiplier)
        .reduce(Float::sum)
        .orElse(0f);
    return multiplier;
  }

  public static void setPotionColor(ItemStack itemStack, int color) {
    PotionContents contents = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    itemStack.set(
        DataComponents.POTION_CONTENTS,
        new PotionContents(contents.potion(), Optional.of(color), contents.customEffects()));
  }

  public static ItemStack mixPotions(ItemStack potion1, ItemStack potion2) {
    List<ItemStack> potions = Arrays.asList(potion1, potion2);
    ItemStack result = new ItemStack(potion1.getItem(), 2);
    List<MobEffectInstance> effects =
        potions.stream()
            .sorted(PotionHelper::comparePotions)
            .map(PotionHelper::getMobEffects)
            .map(PotionHelper::applyMixtureMultipliers)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    int color = getMobEffects(potions.get(0)).isEmpty() ? 0xffffff : PotionContents.getColor(getMobEffects(potions.get(0)));
    result.set(
        DataComponents.POTION_CONTENTS,
        new PotionContents(Optional.empty(), Optional.of(color), effects));
    result.set(PSTComponents.MIXED_POTION.get(), true);
    return result;
  }

  private static List<MobEffectInstance> applyMixtureMultipliers(List<MobEffectInstance> effects) {
    effects.replaceAll(
        effect -> {
          int duration = (int) (effect.getDuration() * Config.mixture_effects_duration);
          int amplifier = (int) (effect.getAmplifier() * Config.mixture_effects_strength);
          return new MobEffectInstance(effect.getEffect(), duration, amplifier);
        });
    return effects;
  }

  private static int comparePotions(ItemStack potionStack1, ItemStack potionStack2) {
    return potionStack1.getDescriptionId().compareTo(potionStack2.getDescriptionId());
  }
}