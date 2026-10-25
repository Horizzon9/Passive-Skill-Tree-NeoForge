package daripher.skilltree.init;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.item.gem.bonus.GemBonusProvider;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.condition.damage.DamageCondition;
import daripher.skilltree.skill.bonus.condition.enchantment.EnchantmentCondition;
import daripher.skilltree.skill.bonus.condition.item.ItemCondition;
import daripher.skilltree.skill.bonus.condition.living.LivingCondition;
import daripher.skilltree.skill.bonus.event.SkillEventListener;
import daripher.skilltree.skill.bonus.item.ItemBonus;
import daripher.skilltree.skill.bonus.multiplier.LivingMultiplier;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = SkillTreeMod.MOD_ID)
public class PSTRegistries {
  public static final Registry<SkillBonus.Serializer> SKILL_BONUSES_REGISTRY =
      create(PSTSkillBonuses.REGISTRY_ID);
  public static final Supplier<Registry<SkillBonus.Serializer>> SKILL_BONUSES =
      () -> SKILL_BONUSES_REGISTRY;
  public static final Registry<LivingMultiplier.Serializer> LIVING_MULTIPLIERS_REGISTRY =
      create(PSTLivingMultipliers.REGISTRY_ID);
  public static final Supplier<Registry<LivingMultiplier.Serializer>> LIVING_MULTIPLIERS =
      () -> LIVING_MULTIPLIERS_REGISTRY;
  public static final Registry<LivingCondition.Serializer> LIVING_CONDITIONS_REGISTRY =
      create(PSTLivingConditions.REGISTRY_ID);
  public static final Supplier<Registry<LivingCondition.Serializer>> LIVING_CONDITIONS =
      () -> LIVING_CONDITIONS_REGISTRY;
  public static final Registry<DamageCondition.Serializer> DAMAGE_CONDITIONS_REGISTRY =
      create(PSTDamageConditions.REGISTRY_ID);
  public static final Supplier<Registry<DamageCondition.Serializer>> DAMAGE_CONDITIONS =
      () -> DAMAGE_CONDITIONS_REGISTRY;
  public static final Registry<ItemCondition.Serializer> ITEM_CONDITIONS_REGISTRY =
      create(PSTItemConditions.REGISTRY_ID);
  public static final Supplier<Registry<ItemCondition.Serializer>> ITEM_CONDITIONS =
      () -> ITEM_CONDITIONS_REGISTRY;
  public static final Registry<ItemBonus.Serializer> ITEM_BONUSES_REGISTRY =
      create(PSTItemBonuses.REGISTRY_ID);
  public static final Supplier<Registry<ItemBonus.Serializer>> ITEM_BONUSES =
      () -> ITEM_BONUSES_REGISTRY;
  public static final Registry<EnchantmentCondition.Serializer> ENCHANTMENT_CONDITIONS_REGISTRY =
      create(PSTEnchantmentConditions.REGISTRY_ID);
  public static final Supplier<Registry<EnchantmentCondition.Serializer>> ENCHANTMENT_CONDITIONS =
      () -> ENCHANTMENT_CONDITIONS_REGISTRY;
  public static final Registry<GemBonusProvider.Serializer> GEM_BONUSES_REGISTRY =
      create(PSTGemBonuses.REGISTRY_ID);
  public static final Supplier<Registry<GemBonusProvider.Serializer>> GEM_BONUSES =
      () -> GEM_BONUSES_REGISTRY;
  public static final Registry<SkillEventListener.Serializer> EVENT_LISTENERS_REGISTRY =
      create(PSTEventListeners.REGISTRY_ID);
  public static final Supplier<Registry<SkillEventListener.Serializer>> EVENT_LISTENERS =
      () -> EVENT_LISTENERS_REGISTRY;

  @SubscribeEvent
  public static void registerRegistries(NewRegistryEvent event) {
    event.register(SKILL_BONUSES_REGISTRY);
    event.register(LIVING_MULTIPLIERS_REGISTRY);
    event.register(LIVING_CONDITIONS_REGISTRY);
    event.register(DAMAGE_CONDITIONS_REGISTRY);
    event.register(ITEM_CONDITIONS_REGISTRY);
    event.register(ITEM_BONUSES_REGISTRY);
    event.register(ENCHANTMENT_CONDITIONS_REGISTRY);
    event.register(GEM_BONUSES_REGISTRY);
    event.register(EVENT_LISTENERS_REGISTRY);
  }

  private static <T> Registry<T> create(ResourceLocation id) {
    return new RegistryBuilder<T>(ResourceKey.createRegistryKey(id)).create();
  }
}
