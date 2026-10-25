package daripher.skilltree.item;

import com.google.common.collect.ImmutableList;
import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.config.Config;
import daripher.skilltree.entity.player.PlayerHelper;
import daripher.skilltree.init.PSTComponents;
import daripher.skilltree.init.PSTRegistries;
import daripher.skilltree.init.PSTTags;
import daripher.skilltree.item.gem.GemItem;
import daripher.skilltree.item.quiver.QuiverItem;
import daripher.skilltree.potion.PotionHelper;
import daripher.skilltree.skill.bonus.condition.item.EquipmentCondition;
import daripher.skilltree.skill.bonus.item.ItemBonus;
import daripher.skilltree.skill.bonus.item.ItemDurabilityBonus;
import daripher.skilltree.skill.bonus.item.ItemSocketsBonus;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

public class ItemHelper {
  public static boolean canInsertGem(ItemStack stack) {
    return !SkillTreeMod.apotheosisEnabled() && hasSockets(stack);
  }

  public static boolean hasSockets(ItemStack stack) {
    List<? extends String> blacklist = Config.socket_blacklist;
    if (blacklist.contains("*:*")) return false;
    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
    if (itemId == null) return false;
    if (blacklist.contains(itemId.toString())) return false;
    String namespace = itemId.getNamespace();
    if (blacklist.contains(namespace + ":*")) return false;
    return EquipmentCondition.isEquipment(stack) || stack.is(PSTTags.JEWELRY);
  }

  public static int getFirstEmptySocket(ItemStack stack, Player player) {
    int sockets = ItemHelper.getMaximumSockets(stack, player);
    int socket = 0;
    for (int i = 0; i < sockets; i++) {
      socket = i;
      if (!GemItem.hasGem(stack, socket)) break;
    }
    return socket;
  }

  public static int getGemsAmount(ItemStack stack) {
    return GemItem.getGems(stack).size();
  }

  public static void refreshGemsAmount(ItemStack stack) {}

  public static void setPoisons(ItemStack result, ItemStack poisonStack) {
    List<MobEffectInstance> effects = PotionHelper.getMobEffects(poisonStack);
    result.set(PSTComponents.POISONS.get(), new ArrayList<>(effects));
  }

  public static boolean hasPoisons(ItemStack stack) {
    return stack.has(PSTComponents.POISONS.get());
  }

  public static List<MobEffectInstance> getPoisons(ItemStack stack) {
    List<MobEffectInstance> poisons = stack.get(PSTComponents.POISONS.get());
    return poisons == null ? ImmutableList.of() : poisons;
  }

  public static int getDefaultSockets(ItemStack stack) {
    if (EquipmentCondition.isHelmet(stack)) return Config.default_helmet_sockets;
    if (EquipmentCondition.isChestplate(stack)) return Config.default_chestplate_sockets;
    if (EquipmentCondition.isLeggings(stack)) return Config.default_leggings_sockets;
    if (EquipmentCondition.isBoots(stack)) return Config.default_boots_sockets;
    if (EquipmentCondition.isWeapon(stack)) return Config.default_weapon_sockets;
    if (EquipmentCondition.isShield(stack)) return Config.default_shield_sockets;
    if (stack.is(PSTTags.RINGS)) return Config.default_ring_sockets;
    if (stack.is(PSTTags.NECKLACES)) return Config.default_necklace_sockets;
    return 0;
  }

  public static int getAdditionalSockets(ItemStack stack) {
    int sockets = 0;
    for (ItemBonus<?> itemBonus : getItemBonusesExcludingGems(stack)) {
      if (itemBonus instanceof ItemSocketsBonus socketsBonus) {
        sockets += socketsBonus.getAmount();
      }
    }
    if (stack.getItem() instanceof HasAdditionalSockets bonus) {
      sockets += bonus.getAdditionalSockets();
    }
    return sockets;
  }

  public static boolean isQuiver(ItemStack stack) {
    return !stack.isEmpty() && stack.getItem() instanceof QuiverItem;
  }

  public static int getMaximumSockets(ItemStack stack, @Nullable Player player) {
    if (SkillTreeMod.apotheosisEnabled()) return 0;
    int sockets = ItemHelper.getDefaultSockets(stack) + ItemHelper.getAdditionalSockets(stack);
    if (player != null) {
      sockets += PlayerHelper.getPlayerSockets(stack, player);
    }
    return sockets;
  }

  public static List<? extends ItemBonus<?>> getItemBonusesExcludingGems(ItemStack stack) {
    List<CompoundTag> tags = stack.get(PSTComponents.SKILL_BONUSES.get());
    if (tags == null) return ImmutableList.of();
    List<ItemBonus<?>> list = new ArrayList<>();
    for (CompoundTag compoundTag : tags) {
      ItemBonus<?> itemBonus = deserializeBonus(compoundTag);
      if (itemBonus != null) list.add(itemBonus);
    }
    return list;
  }

  public static List<? extends ItemBonus<?>> getItemBonuses(ItemStack stack) {
    List<ItemBonus<?>> bonuses = new ArrayList<>();
    bonuses.addAll(getItemBonusesExcludingGems(stack));
    bonuses.addAll(GemItem.getGemBonuses(stack));
    return bonuses;
  }

  public static <T extends ItemBonus<?>> List<T> getItemBonuses(ItemStack stack, Class<T> aClass) {
    List<T> list = new ArrayList<>();
    for (ItemBonus<?> itemBonus : getItemBonuses(stack)) {
      if (aClass.isInstance(itemBonus)) {
        T cast = aClass.cast(itemBonus);
        list.add(cast);
      }
    }
    return list;
  }

  public static void addItemBonus(ItemStack stack, ItemBonus<?> bonus) {
    List<CompoundTag> bonusesTag = new ArrayList<>();
    bonusesTag.add(serializeBonus(bonus));
    for (ItemBonus<?> bonus2 : ItemHelper.getItemBonuses(stack)) {
      ItemBonus<? extends ItemBonus<?>> itemBonus = mergeIfPossible(bonus, bonus2, bonusesTag);
      bonusesTag.add(serializeBonus(itemBonus));
    }
    stack.set(PSTComponents.SKILL_BONUSES.get(), bonusesTag);
  }

  public static void removeItemBonus(ItemStack stack, ItemBonus<?> bonus) {
    List<CompoundTag> bonusesTag = new ArrayList<>();
    for (ItemBonus<?> itemBonus : ItemHelper.getItemBonuses(stack)) {
      if (!bonus.equals(itemBonus)) {
        bonusesTag.add(serializeBonus(itemBonus));
      }
    }
    stack.set(PSTComponents.SKILL_BONUSES.get(), bonusesTag);
  }

  public static void removeItemBonuses(ItemStack stack) {
    stack.remove(PSTComponents.SKILL_BONUSES.get());
  }

  public static void refreshDurabilityBonuses(ItemStack stack) {
    stack.remove(PSTComponents.DURABILITY_BONUSES.get());
    List<CompoundTag> durabilityTags = new ArrayList<>();
    for (ItemDurabilityBonus bonus : getItemBonuses(stack, ItemDurabilityBonus.class)) {
      durabilityTags.add(serializeBonus(bonus));
    }
    if (durabilityTags.isEmpty()) return;
    stack.set(PSTComponents.DURABILITY_BONUSES.get(), durabilityTags);
  }

  public static List<ItemDurabilityBonus> getDurabilityBonuses(ItemStack stack) {
    List<CompoundTag> tags = stack.get(PSTComponents.DURABILITY_BONUSES.get());
    if (tags == null) return ImmutableList.of();
    List<ItemDurabilityBonus> list = new ArrayList<>();
    for (CompoundTag compoundTag : tags) {
      ItemBonus<?> itemBonus = deserializeBonus(compoundTag);
      if (itemBonus instanceof ItemDurabilityBonus bonus) {
        list.add(bonus);
      }
    }
    return list;
  }

  private static ItemBonus<? extends ItemBonus<?>> mergeIfPossible(
      ItemBonus<?> bonus1, ItemBonus<?> bonus2, List<CompoundTag> bonusesTag) {
    if (bonus1.canMerge(bonus2)) {
      bonusesTag.remove(0);
      return bonus1.merge(bonus2);
    }
    return bonus2;
  }

  private static CompoundTag serializeBonus(ItemBonus<? extends ItemBonus<?>> bonus) {
    ItemBonus.Serializer serializer = bonus.getSerializer();
    CompoundTag bonusTag = serializer.serialize(bonus);
    ResourceLocation id = PSTRegistries.ITEM_BONUSES.get().getKey(serializer);
    bonusTag.putString("type", Objects.requireNonNull(id).toString());
    return bonusTag;
  }

  private static ItemBonus<?> deserializeBonus(CompoundTag tag) {
    if (!tag.contains("type")) return null;
    ResourceLocation id = ResourceLocation.parse(tag.getString("type"));
    ItemBonus.Serializer serializer = PSTRegistries.ITEM_BONUSES.get().get(id);
    if (serializer == null) return null;
    try {
      return serializer.deserialize(tag);
    } catch (Exception e) {
      SkillTreeMod.LOGGER.error("Unexpected error", e);
      return null;
    }
  }
}