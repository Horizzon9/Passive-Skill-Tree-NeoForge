package daripher.skilltree.data.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.init.PSTRegistries;
import daripher.skilltree.item.gem.bonus.GemBonusProvider;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.condition.damage.DamageCondition;
import daripher.skilltree.skill.bonus.condition.damage.NoneDamageCondition;
import daripher.skilltree.skill.bonus.condition.enchantment.EnchantmentCondition;
import daripher.skilltree.skill.bonus.condition.enchantment.NoneEnchantmentCondition;
import daripher.skilltree.skill.bonus.condition.item.*;
import daripher.skilltree.skill.bonus.condition.living.LivingCondition;
import daripher.skilltree.skill.bonus.condition.living.NoneLivingCondition;
import daripher.skilltree.skill.bonus.event.SkillEventListener;
import daripher.skilltree.skill.bonus.item.ItemBonus;
import daripher.skilltree.skill.bonus.multiplier.LivingMultiplier;
import daripher.skilltree.skill.bonus.multiplier.NoneLivingMultiplier;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotAttribute;

public class SerializationHelper {
  public static SkillBonus<?> deserializeSkillBonus(JsonObject json) {
    JsonObject bonusJson = json.getAsJsonObject("skill_bonus");
    String type = bonusJson.get("type").getAsString();
    ResourceLocation serializerId = ResourceLocation.parse(type);
    SkillBonus.Serializer serializer = PSTRegistries.SKILL_BONUSES.get().get(serializerId);
    return Objects.requireNonNull(serializer).deserialize(bonusJson);
  }

  public static void serializeSkillBonus(JsonObject json, SkillBonus<?> bonus) {
    ResourceLocation serializerId = PSTRegistries.SKILL_BONUSES.get().getKey(bonus.getSerializer());
    JsonObject bonusJson = new JsonObject();
    bonus.getSerializer().serialize(bonusJson, bonus);
    bonusJson.addProperty("type", Objects.requireNonNull(serializerId).toString());
    json.add("skill_bonus", bonusJson);
  }

  public static ItemBonus<?> deserializeItemBonus(JsonObject json) {
    JsonObject bonusJson = json.getAsJsonObject("item_bonus");
    String type = bonusJson.get("type").getAsString();
    ResourceLocation serializerId = ResourceLocation.parse(type);
    ItemBonus.Serializer serializer = PSTRegistries.ITEM_BONUSES.get().get(serializerId);
    return Objects.requireNonNull(serializer).deserialize(bonusJson);
  }

  public static void serializeItemBonus(JsonObject json, ItemBonus<?> bonus) {
    ResourceLocation serializerId = PSTRegistries.ITEM_BONUSES.get().getKey(bonus.getSerializer());
    JsonObject bonusJson = new JsonObject();
    bonus.getSerializer().serialize(bonusJson, bonus);
    bonusJson.addProperty("type", Objects.requireNonNull(serializerId).toString());
    json.add("item_bonus", bonusJson);
  }

  @NotNull
  public static Holder<Attribute> deserializeAttribute(JsonObject json) {
    ResourceLocation attributeId = ResourceLocation.parse(json.get("attribute").getAsString());
    Holder<Attribute> attribute;
    if (attributeId.getNamespace().equals("curios")) {
      attribute = SlotAttribute.getOrCreate(attributeId.getPath());
    } else {
      attribute = BuiltInRegistries.ATTRIBUTE.getHolder(attributeId).orElse(null);
    }
    if (attribute == null) {
      throw new RuntimeException("Attribute " + attributeId + " doesn't exist!");
    }
    return attribute;
  }

  public static void serializeAttribute(JsonObject json, Holder<Attribute> attribute) {
    ResourceLocation attributeId;
    if (attribute.value() instanceof SlotAttribute wrapper) {
      attributeId = ResourceLocation.fromNamespaceAndPath("curios", wrapper.getIdentifier());
    } else {
      attributeId = BuiltInRegistries.ATTRIBUTE.getKey(attribute.value());
    }
    assert attributeId != null;
    json.addProperty("attribute", attributeId.toString());
  }

  @NotNull
  public static AttributeModifier deserializeAttributeModifier(JsonObject json) {
    double amount = json.get("amount").getAsDouble();
    AttributeModifier.Operation operation = deserializeOperation(json);
    return new AttributeModifier(deserializeModifierId(json.get("id").getAsString()), amount, operation);
  }

  public static void serializeAttributeModifier(JsonObject json, AttributeModifier modifier) {
    json.addProperty("id", serializeModifierId(modifier.id()));
    json.addProperty("name", "Skill");
    json.addProperty("amount", modifier.amount());
    serializeOperation(json, modifier.operation());
  }

  public static ResourceLocation deserializeModifierId(String id) {
    if (id.contains(":")) return ResourceLocation.parse(id);
    return ResourceLocation.fromNamespaceAndPath("skilltree", id.toLowerCase(java.util.Locale.ROOT));
  }

  public static String serializeModifierId(ResourceLocation id) {
    return id.getPath();
  }

  @NotNull
  public static AttributeModifier.Operation deserializeOperation(JsonObject json) {
    return AttributeModifier.Operation.BY_ID.apply(json.get("operation").getAsInt());
  }

  public static void serializeOperation(JsonObject json, AttributeModifier.Operation operation) {
    json.addProperty("operation", operation.id());
  }

  public static @Nonnull LivingMultiplier deserializeLivingMultiplier(
      JsonObject json, String name) {
    if (!json.has(name)) return NoneLivingMultiplier.INSTANCE;
    JsonObject multiplierJson = json.getAsJsonObject(name);
    ResourceLocation serializerId = ResourceLocation.parse(multiplierJson.get("type").getAsString());
    LivingMultiplier.Serializer serializer =
        PSTRegistries.LIVING_MULTIPLIERS.get().get(serializerId);
    return Objects.requireNonNull(serializer).deserialize(multiplierJson);
  }

  public static void serializeLivingMultiplier(
      JsonObject json, @Nonnull LivingMultiplier multiplier, String name) {
    JsonObject multiplierJson = new JsonObject();
    LivingMultiplier.Serializer serializer = multiplier.getSerializer();
    serializer.serialize(multiplierJson, multiplier);
    ResourceLocation serializerId = PSTRegistries.LIVING_MULTIPLIERS.get().getKey(serializer);
    assert serializerId != null;
    multiplierJson.addProperty("type", serializerId.toString());
    json.add(name, multiplierJson);
  }

  public static @Nonnull LivingCondition deserializeLivingCondition(JsonObject json, String name) {
    if (!json.has(name)) return NoneLivingCondition.INSTANCE;
    JsonObject conditionJson = json.getAsJsonObject(name);
    ResourceLocation serializerId = ResourceLocation.parse(conditionJson.get("type").getAsString());
    LivingCondition.Serializer serializer =
        PSTRegistries.LIVING_CONDITIONS.get().get(serializerId);
    String errorMessage = "Unknown living condition: " + serializerId;
    return deserializeObject(serializer, conditionJson, errorMessage);
  }

  public static void serializeLivingCondition(
      JsonObject json, @Nonnull LivingCondition condition, String name) {
    JsonObject conditionJson = new JsonObject();
    LivingCondition.Serializer serializer = condition.getSerializer();
    serializer.serialize(conditionJson, condition);
    ResourceLocation serializerId = PSTRegistries.LIVING_CONDITIONS.get().getKey(serializer);
    assert serializerId != null;
    conditionJson.addProperty("type", serializerId.toString());
    json.add(name, conditionJson);
  }

  @Nonnull
  public static DamageCondition deserializeDamageCondition(JsonObject json) {
    String name = "damage_condition";
    if (!json.has(name)) return NoneDamageCondition.INSTANCE;
    JsonObject conditionJson = json.getAsJsonObject(name);
    ResourceLocation serializerId = ResourceLocation.parse(conditionJson.get("type").getAsString());
    DamageCondition.Serializer serializer =
        PSTRegistries.DAMAGE_CONDITIONS.get().get(serializerId);
    String errorMessage = "Unknown damage condition: " + serializerId;
    return deserializeObject(serializer, conditionJson, errorMessage);
  }

  public static void serializeDamageCondition(JsonObject json, @Nonnull DamageCondition condition) {
    JsonObject conditionJson = new JsonObject();
    DamageCondition.Serializer serializer = condition.getSerializer();
    serializer.serialize(conditionJson, condition);
    ResourceLocation serializerId = PSTRegistries.DAMAGE_CONDITIONS.get().getKey(serializer);
    conditionJson.addProperty("type", Objects.requireNonNull(serializerId).toString());
    json.add("damage_condition", conditionJson);
  }

  public static @Nonnull ItemCondition deserializeItemCondition(JsonObject json) {
    String name = "item_condition";
    if (!json.has(name)) return NoneItemCondition.INSTANCE;
    JsonObject conditionJson = json.getAsJsonObject(name);
    ResourceLocation serializerId = ResourceLocation.parse(conditionJson.get("type").getAsString());
    ItemCondition.Serializer serializer =
        PSTRegistries.ITEM_CONDITIONS.get().get(serializerId);
    String errorMessage = "Unknown item condition: " + serializerId;
    return deserializeObject(serializer, conditionJson, errorMessage);
  }

  public static void serializeItemCondition(JsonObject json, @Nonnull ItemCondition condition) {
    JsonObject conditionJson = new JsonObject();
    ItemCondition.Serializer serializer = condition.getSerializer();
    serializer.serialize(conditionJson, condition);
    ResourceLocation serializerId = PSTRegistries.ITEM_CONDITIONS.get().getKey(serializer);
    conditionJson.addProperty("type", Objects.requireNonNull(serializerId).toString());
    json.add("item_condition", conditionJson);
  }

  public static @Nonnull SkillEventListener deserializeEventListener(JsonObject json) {
    JsonObject eventJson = json.getAsJsonObject("event_listener");
    ResourceLocation serializerId = ResourceLocation.parse(eventJson.get("type").getAsString());
    SkillEventListener.Serializer serializer =
        PSTRegistries.EVENT_LISTENERS.get().get(serializerId);
    String errorMessage = "Unknown event listener: " + serializerId;
    return deserializeObject(serializer, eventJson, errorMessage);
  }

  public static void serializeEventListener(
      JsonObject json, @Nonnull SkillEventListener condition) {
    JsonObject conditionJson = new JsonObject();
    SkillEventListener.Serializer serializer = condition.getSerializer();
    serializer.serialize(conditionJson, condition);
    ResourceLocation serializerId = PSTRegistries.EVENT_LISTENERS.get().getKey(serializer);
    conditionJson.addProperty("type", Objects.requireNonNull(serializerId).toString());
    json.add("event_listener", conditionJson);
  }

  public static @Nullable Holder<MobEffect> deserializeEffect(JsonObject json) {
    if (!json.has("effect")) return null;
    ResourceLocation effectId = ResourceLocation.parse(json.get("effect").getAsString());
    return BuiltInRegistries.MOB_EFFECT.getHolder(effectId).orElse(null);
  }

  public static void serializeEffect(JsonObject json, Holder<MobEffect> effect) {
    ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(effect.value());
    json.addProperty("effect", Objects.requireNonNull(effectId).toString());
  }

  public static @Nullable PotionCondition.Type deserializePotionType(JsonObject json) {
    return PotionCondition.Type.byName(json.get("potion_type").getAsString());
  }

  public static void serializePotionType(JsonObject json, PotionCondition.Type type) {
    json.addProperty("potion_type", type.getName());
  }

  public static MobEffectInstance deserializeEffectInstance(JsonObject json) {
    Holder<MobEffect> effect = deserializeEffect(json);
    int duration = json.get("duration").getAsInt();
    int amplifier = json.get("amplifier").getAsInt();
    return new MobEffectInstance(Objects.requireNonNull(effect), duration, amplifier);
  }

  public static void serializeEffectInstance(JsonObject json, MobEffectInstance effect) {
    serializeEffect(json, effect.getEffect());
    json.addProperty("duration", effect.getDuration());
    json.addProperty("amplifier", effect.getAmplifier());
  }

  @Nonnull
  public static EnchantmentCondition deserializeEnchantmentCondition(JsonObject json) {
    String name = "enchantment_condition";
    if (!json.has(name)) return NoneEnchantmentCondition.INSTANCE;
    JsonObject conditionJson = json.getAsJsonObject(name);
    ResourceLocation serializerId = ResourceLocation.parse(conditionJson.get("type").getAsString());
    EnchantmentCondition.Serializer serializer =
        PSTRegistries.ENCHANTMENT_CONDITIONS.get().get(serializerId);
    String errorMessage = "Unknown enchantment condition: " + serializerId;
    return deserializeObject(serializer, conditionJson, errorMessage);
  }

  public static void serializeEnchantmentCondition(
      JsonObject json, @Nonnull EnchantmentCondition condition) {
    JsonObject conditionJson = new JsonObject();
    EnchantmentCondition.Serializer serializer = condition.getSerializer();
    serializer.serialize(conditionJson, condition);
    ResourceLocation serializerId = PSTRegistries.ENCHANTMENT_CONDITIONS.get().getKey(serializer);
    conditionJson.addProperty("type", Objects.requireNonNull(serializerId).toString());
    json.add("enchantment_condition", conditionJson);
  }

  @Nonnull
  public static <T> List<T> deserializeObjects(
      JsonObject json, String elementName, Function<JsonObject, T> deserializer) {
    return StreamSupport.stream(json.getAsJsonArray(elementName).spliterator(), true)
        .map(JsonObject.class::cast)
        .map(deserializer)
        .toList();
  }

  public static <T> void serializeObjects(
      JsonObject json, String elementName, List<T> objects, BiConsumer<JsonObject, T> serializer) {
    JsonArray objectsJson = new JsonArray();
    objects.forEach(
        object -> {
          JsonObject objectJson = new JsonObject();
          serializer.accept(objectJson, object);
          objectsJson.add(objectJson);
        });
    json.add(elementName, objectsJson);
  }

  public static GemBonusProvider deserializeGemBonusProvider(JsonObject json) {
    JsonObject providerJson = json.getAsJsonObject("bonus_provider");
    String type = providerJson.get("type").getAsString();
    ResourceLocation serializerId = ResourceLocation.parse(type);
    GemBonusProvider.Serializer serializer = PSTRegistries.GEM_BONUSES.get().get(serializerId);
    return Objects.requireNonNull(serializer).deserialize(providerJson);
  }

  public static void serializeGemBonusProvider(JsonObject json, GemBonusProvider provider) {
    ResourceLocation serializerId =
        PSTRegistries.GEM_BONUSES.get().getKey(provider.getSerializer());
    JsonObject bonusJson = new JsonObject();
    provider.getSerializer().serialize(bonusJson, provider);
    bonusJson.addProperty("type", Objects.requireNonNull(serializerId).toString());
    json.add("bonus_provider", bonusJson);
  }

  @Nullable
  public static Holder<Attribute> deserializeAttribute(CompoundTag tag) {
    ResourceLocation attributeId = ResourceLocation.parse(tag.getString("attribute"));
    Holder<Attribute> attribute;
    if (attributeId.getNamespace().equals("curios")) {
      attribute = SlotAttribute.getOrCreate(attributeId.getPath());
    } else {
      attribute = BuiltInRegistries.ATTRIBUTE.getHolder(attributeId).orElse(null);
    }
    if (attribute == null) {
      SkillTreeMod.LOGGER.error("Attribute {} doesn't exist!", attributeId);
    }
    return attribute;
  }

  public static void serializeAttribute(CompoundTag tag, Holder<Attribute> attribute) {
    ResourceLocation attributeId;
    if (attribute.value() instanceof SlotAttribute wrapper) {
      attributeId = ResourceLocation.fromNamespaceAndPath("curios", wrapper.getIdentifier());
    } else {
      attributeId = BuiltInRegistries.ATTRIBUTE.getKey(attribute.value());
    }
    assert attributeId != null;
    tag.putString("attribute", attributeId.toString());
  }

  @NotNull
  public static AttributeModifier deserializeAttributeModifier(CompoundTag tag) {
    double amount = tag.getDouble("amount");
    AttributeModifier.Operation operation = deserializeOperation(tag);
    return new AttributeModifier(deserializeModifierId(tag.getString("id")), amount, operation);
  }

  public static void serializeAttributeModifier(CompoundTag tag, AttributeModifier modifier) {
    tag.putString("id", serializeModifierId(modifier.id()));
    tag.putString("name", "Skill");
    tag.putDouble("amount", modifier.amount());
    serializeOperation(tag, modifier.operation());
  }

  @NotNull
  public static AttributeModifier.Operation deserializeOperation(CompoundTag tag) {
    return AttributeModifier.Operation.BY_ID.apply(tag.getInt("operation"));
  }

  public static void serializeOperation(CompoundTag tag, AttributeModifier.Operation operation) {
    tag.putInt("operation", operation.id());
  }

  public static @Nonnull LivingMultiplier deserializeLivingMultiplier(
      CompoundTag tag, String name) {
    if (!tag.contains(name)) return NoneLivingMultiplier.INSTANCE;
    CompoundTag multiplierTag = tag.getCompound(name);
    ResourceLocation serializerId = ResourceLocation.parse(multiplierTag.getString("type"));
    LivingMultiplier.Serializer serializer =
        PSTRegistries.LIVING_MULTIPLIERS.get().get(serializerId);
    return Objects.requireNonNull(serializer).deserialize(multiplierTag);
  }

  public static void serializeLivingMultiplier(
      CompoundTag tag, @Nonnull LivingMultiplier multiplier, String name) {
    LivingMultiplier.Serializer serializer = multiplier.getSerializer();
    CompoundTag multiplierTag = serializer.serialize(multiplier);
    ResourceLocation serializerId = PSTRegistries.LIVING_MULTIPLIERS.get().getKey(serializer);
    multiplierTag.putString("type", Objects.requireNonNull(serializerId).toString());
    tag.put(name, multiplierTag);
  }

  public static @Nonnull LivingCondition deserializeLivingCondition(CompoundTag tag, String name) {
    CompoundTag conditionTag = tag.getCompound(name);
    ResourceLocation serializerId = ResourceLocation.parse(conditionTag.getString("type"));
    LivingCondition.Serializer serializer =
        PSTRegistries.LIVING_CONDITIONS.get().get(serializerId);
    return Objects.requireNonNull(serializer).deserialize(conditionTag);
  }

  public static void serializeLivingCondition(
      CompoundTag tag, @Nonnull LivingCondition condition, String name) {
    LivingCondition.Serializer serializer = condition.getSerializer();
    CompoundTag conditionTag = serializer.serialize(condition);
    ResourceLocation serializerId = PSTRegistries.LIVING_CONDITIONS.get().getKey(serializer);
    assert serializerId != null;
    conditionTag.putString("type", serializerId.toString());
    tag.put(name, conditionTag);
  }

  public static @Nonnull DamageCondition deserializeDamageCondition(CompoundTag tag) {
    CompoundTag conditionTag = tag.getCompound("damage_condition");
    ResourceLocation serializerId = ResourceLocation.parse(conditionTag.getString("type"));
    DamageCondition.Serializer serializer =
        PSTRegistries.DAMAGE_CONDITIONS.get().get(serializerId);
    return Objects.requireNonNull(serializer).deserialize(conditionTag);
  }

  public static void serializeDamageCondition(CompoundTag tag, @Nonnull DamageCondition condition) {
    DamageCondition.Serializer serializer = condition.getSerializer();
    CompoundTag conditionTag = serializer.serialize(condition);
    ResourceLocation serializerId = PSTRegistries.DAMAGE_CONDITIONS.get().getKey(serializer);
    conditionTag.putString("type", Objects.requireNonNull(serializerId).toString());
    tag.put("damage_condition", conditionTag);
  }

  public static @Nonnull ItemCondition deserializeItemCondition(CompoundTag tag) {
    CompoundTag conditionTag = tag.getCompound("item_condition");
    ResourceLocation serializerId = ResourceLocation.parse(conditionTag.getString("type"));
    ItemCondition.Serializer serializer =
        PSTRegistries.ITEM_CONDITIONS.get().get(serializerId);
    return Objects.requireNonNull(serializer).deserialize(conditionTag);
  }

  public static void serializeItemCondition(CompoundTag tag, @Nonnull ItemCondition condition) {
    ItemCondition.Serializer serializer = condition.getSerializer();
    CompoundTag conditionTag = serializer.serialize(condition);
    ResourceLocation serializerId = PSTRegistries.ITEM_CONDITIONS.get().getKey(serializer);
    conditionTag.putString("type", Objects.requireNonNull(serializerId).toString());
    tag.put("item_condition", conditionTag);
  }

  public static @Nonnull SkillEventListener deserializeEventListener(CompoundTag tag) {
    CompoundTag conditionTag = tag.getCompound("event_listener");
    ResourceLocation serializerId = ResourceLocation.parse(conditionTag.getString("type"));
    SkillEventListener.Serializer serializer =
        PSTRegistries.EVENT_LISTENERS.get().get(serializerId);
    return Objects.requireNonNull(serializer).deserialize(conditionTag);
  }

  public static void serializeEventListener(
      CompoundTag tag, @Nonnull SkillEventListener condition) {
    SkillEventListener.Serializer serializer = condition.getSerializer();
    CompoundTag conditionTag = serializer.serialize(condition);
    ResourceLocation serializerId = PSTRegistries.EVENT_LISTENERS.get().getKey(serializer);
    conditionTag.putString("type", Objects.requireNonNull(serializerId).toString());
    tag.put("event_listener", conditionTag);
  }

  @Nullable
  public static Holder<MobEffect> deserializeEffect(CompoundTag tag) {
    if (!tag.contains("effect")) return null;
    ResourceLocation effectId = ResourceLocation.parse(tag.getString("effect"));
    return BuiltInRegistries.MOB_EFFECT.getHolder(effectId).orElse(null);
  }

  public static void serializeEffect(CompoundTag tag, Holder<MobEffect> effect) {
    ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(effect.value());
    tag.putString("effect", Objects.requireNonNull(effectId).toString());
  }

  public static SkillBonus<?> deserializeSkillBonus(CompoundTag tag) {
    CompoundTag bonusTag = tag.getCompound("skill_bonus");
    String type = bonusTag.getString("type");
    ResourceLocation serializerId = ResourceLocation.parse(type);
    SkillBonus.Serializer serializer = PSTRegistries.SKILL_BONUSES.get().get(serializerId);
    return Objects.requireNonNull(serializer).deserialize(bonusTag);
  }

  public static void serializeSkillBonus(CompoundTag tag, SkillBonus<?> bonus) {
    ResourceLocation serializerId = PSTRegistries.SKILL_BONUSES.get().getKey(bonus.getSerializer());
    CompoundTag bonusTag = bonus.getSerializer().serialize(bonus);
    bonusTag.putString("type", Objects.requireNonNull(serializerId).toString());
    tag.put("skill_bonus", bonusTag);
  }

  public static ItemBonus<?> deserializeItemBonus(CompoundTag tag) {
    CompoundTag bonusTag = tag.getCompound("item_bonus");
    String type = bonusTag.getString("type");
    ResourceLocation serializerId = ResourceLocation.parse(type);
    ItemBonus.Serializer serializer = PSTRegistries.ITEM_BONUSES.get().get(serializerId);
    return Objects.requireNonNull(serializer).deserialize(bonusTag);
  }

  public static void serializeItemBonus(CompoundTag tag, ItemBonus<?> bonus) {
    ResourceLocation serializerId = PSTRegistries.ITEM_BONUSES.get().getKey(bonus.getSerializer());
    CompoundTag bonusTag = bonus.getSerializer().serialize(bonus);
    bonusTag.putString("type", Objects.requireNonNull(serializerId).toString());
    tag.put("item_bonus", bonusTag);
  }

  public static PotionCondition.Type deserializePotionType(CompoundTag tag) {
    return PotionCondition.Type.byName(tag.getString("potion_type"));
  }

  public static void serializePotionType(CompoundTag tag, PotionCondition.Type type) {
    tag.putString("category", type.getName());
  }

  public static MobEffectInstance deserializeEffectInstance(CompoundTag tag) {
    Holder<MobEffect> effect = deserializeEffect(tag);
    int duration = tag.getInt("duration");
    int amplifier = tag.getInt("amplifier");
    return new MobEffectInstance(Objects.requireNonNull(effect), duration, amplifier);
  }

  public static void serializeEffectInstance(CompoundTag tag, MobEffectInstance effect) {
    serializeEffect(tag, effect.getEffect());
    tag.putInt("duration", effect.getDuration());
    tag.putInt("amplifier", effect.getAmplifier());
  }

  public static @Nonnull EnchantmentCondition deserializeEnchantmentCondition(CompoundTag tag) {
    CompoundTag conditionTag = tag.getCompound("enchantment_condition");
    ResourceLocation serializerId = ResourceLocation.parse(conditionTag.getString("type"));
    EnchantmentCondition.Serializer serializer =
        PSTRegistries.ENCHANTMENT_CONDITIONS.get().get(serializerId);
    return Objects.requireNonNull(serializer).deserialize(conditionTag);
  }

  public static void serializeEnchantmentCondition(
      CompoundTag tag, @Nonnull EnchantmentCondition condition) {
    EnchantmentCondition.Serializer serializer = condition.getSerializer();
    CompoundTag conditionTag = serializer.serialize(condition);
    ResourceLocation serializerId = PSTRegistries.ENCHANTMENT_CONDITIONS.get().getKey(serializer);
    conditionTag.putString("type", Objects.requireNonNull(serializerId).toString());
    tag.put("enchantment_condition", conditionTag);
  }

  @Nonnull
  public static <T> List<T> deserializeObjects(
      CompoundTag tag, String elementName, Function<CompoundTag, T> deserializer) {
    return tag.getList(elementName, CompoundTag.TAG_COMPOUND).stream()
        .map(CompoundTag.class::cast)
        .map(deserializer)
        .toList();
  }

  public static <T> void serializeObjects(
      CompoundTag tag, String elementName, List<T> objects, BiConsumer<CompoundTag, T> serializer) {
    ListTag objectsTag = new ListTag();
    objects.forEach(
        o -> {
          CompoundTag objectTag = new CompoundTag();
          serializer.accept(objectTag, o);
          objectsTag.add(objectTag);
        });
    tag.put(elementName, objectsTag);
  }

  public static GemBonusProvider deserializeGemBonusProvider(CompoundTag tag) {
    CompoundTag bonusTag = tag.getCompound("bonus_provider");
    String type = bonusTag.getString("type");
    ResourceLocation serializerId = ResourceLocation.parse(type);
    GemBonusProvider.Serializer serializer = PSTRegistries.GEM_BONUSES.get().get(serializerId);
    return Objects.requireNonNull(serializer).deserialize(bonusTag);
  }

  public static void serializeGemBonusProvider(CompoundTag tag, GemBonusProvider provider) {
    ResourceLocation serializerId =
        PSTRegistries.GEM_BONUSES.get().getKey(provider.getSerializer());
    CompoundTag bonusTag = provider.getSerializer().serialize(provider);
    bonusTag.putString("type", Objects.requireNonNull(serializerId).toString());
    tag.put("bonus_provider", bonusTag);
  }

  private static <T> T deserializeObject(
      Serializer<T> serializer, JsonObject jsonObject, String errorMessage) {
    return Objects.requireNonNull(serializer, errorMessage).deserialize(jsonObject);
  }

  public static JsonElement getElement(JsonObject json, String name) {
    JsonElement element = json.get(name);
    return Objects.requireNonNull(element, "Element not found: " + name);
  }
}
