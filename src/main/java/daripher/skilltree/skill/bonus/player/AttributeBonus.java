package daripher.skilltree.skill.bonus.player;

import com.google.gson.*;
import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.condition.living.LivingCondition;
import daripher.skilltree.skill.bonus.condition.living.NoneLivingCondition;
import daripher.skilltree.skill.bonus.multiplier.LivingMultiplier;
import daripher.skilltree.skill.bonus.multiplier.NoneLivingMultiplier;
import java.util.*;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotAttribute;

public final class AttributeBonus implements SkillBonus<AttributeBonus>, SkillBonus.Ticking {
  private Holder<Attribute> attribute;
  private AttributeModifier modifier;
  private @Nonnull LivingMultiplier playerMultiplier = NoneLivingMultiplier.INSTANCE;
  private @Nonnull LivingCondition playerCondition = NoneLivingCondition.INSTANCE;

  public AttributeBonus(Holder<Attribute> attribute, AttributeModifier modifier) {
    this.attribute = attribute;
    this.modifier = modifier;
  }

  public AttributeBonus(
      Holder<Attribute> attribute,
      String name,
      float amount,
      AttributeModifier.Operation operation) {
    this(attribute, new AttributeModifier(randomModifierId(), amount, operation));
  }

  public static ResourceLocation randomModifierId() {
    return ResourceLocation.fromNamespaceAndPath("skilltree", UUID.randomUUID().toString());
  }

  @Override
  public void onSkillLearned(ServerPlayer player, boolean firstTime) {
    if (playerCondition != NoneLivingCondition.INSTANCE
        || playerMultiplier != NoneLivingMultiplier.INSTANCE) {
      return;
    }
    if (attribute.value() instanceof SlotAttribute wrapper) {
      if (firstTime)
        CuriosApi.getCuriosInventory(player)
            .ifPresent(
                inv ->
                    inv.getStacksHandler(wrapper.getIdentifier())
                        .ifPresent(handler -> handler.grow((int) modifier.amount())));
      return;
    }
    AttributeInstance instance = player.getAttribute(attribute);
    if (instance == null) {
      SkillTreeMod.errorMissingAttribute(attribute);
      return;
    }
    if (!instance.hasModifier(modifier.id())) {
      applyAttributeModifier(instance, modifier, player);
    }
  }

  @Override
  public void onSkillRemoved(ServerPlayer player) {
    if (attribute.value() instanceof SlotAttribute wrapper) {
      CuriosApi.getCuriosInventory(player)
          .ifPresent(
              inv ->
                  inv.getStacksHandler(wrapper.getIdentifier())
                      .ifPresent(handler -> handler.shrink((int) modifier.amount())));
      return;
    }
    AttributeInstance instance = player.getAttribute(attribute);
    if (instance == null) {
      SkillTreeMod.errorMissingAttribute(attribute);
      return;
    }
    instance.removeModifier(modifier.id());
  }

  @Override
  public void tick(ServerPlayer player) {
    if (playerCondition == NoneLivingCondition.INSTANCE
        && playerMultiplier == NoneLivingMultiplier.INSTANCE) {
      return;
    }
    if (playerCondition != NoneLivingCondition.INSTANCE) {
      if (!playerCondition.met(player)) {
        onSkillRemoved(player);
        return;
      }
    }
    if (playerMultiplier != NoneLivingMultiplier.INSTANCE
        && playerMultiplier.getValue(player) == 0) {
      onSkillRemoved(player);
      return;
    }
    applyDynamicAttributeBonus(player);
  }

  private void applyDynamicAttributeBonus(ServerPlayer player) {
    AttributeInstance instance = player.getAttribute(attribute);
    if (instance == null) return;
    AttributeModifier oldModifier = instance.getModifier(modifier.id());
    double value = modifier.amount();
    value *= playerMultiplier.getValue(player);
    if (oldModifier != null) {
      if (oldModifier.amount() == value) return;
    }
    AttributeModifier dynamicModifier =
        new AttributeModifier(modifier.id(), value, modifier.operation());
    applyAttributeModifier(instance, dynamicModifier, player);
  }

  private void applyAttributeModifier(
      AttributeInstance instance, AttributeModifier modifier, Player player) {
    float healthPercentage = player.getHealth() / player.getMaxHealth();
    if (instance.getModifier(modifier.id()) != null) {
      instance.removeModifier(modifier.id());
    }
    instance.addTransientModifier(modifier);
    if (attribute.equals(Attributes.MAX_HEALTH)) {
      player.setHealth(player.getMaxHealth() * healthPercentage);
    }
  }

  @Override
  public SkillBonus.Serializer getSerializer() {
    return PSTSkillBonuses.ATTRIBUTE.get();
  }

  @Override
  public AttributeBonus copy() {
    AttributeModifier modifier =
        new AttributeModifier(
            randomModifierId(), this.modifier.amount(), this.modifier.operation());
    AttributeBonus bonus = new AttributeBonus(attribute, modifier);
    bonus.playerMultiplier = this.playerMultiplier;
    bonus.playerCondition = this.playerCondition;
    return bonus;
  }

  @Override
  public AttributeBonus multiply(double multiplier) {
    modifier =
        new AttributeModifier(
            modifier.id(), modifier.amount() * multiplier, modifier.operation());
    return this;
  }

  @Override
  public boolean canMerge(SkillBonus<?> other) {
    if (!(other instanceof AttributeBonus otherBonus)) return false;
    if (otherBonus.attribute != this.attribute) return false;
    if (!Objects.equals(otherBonus.playerMultiplier, this.playerMultiplier)) return false;
    if (!Objects.equals(otherBonus.playerCondition, this.playerCondition)) return false;
    return otherBonus.modifier.operation() == this.modifier.operation();
  }

  @Override
  public SkillBonus<AttributeBonus> merge(SkillBonus<?> other) {
    if (!(other instanceof AttributeBonus otherBonus)) {
      throw new IllegalArgumentException();
    }
    AttributeModifier mergedModifier =
        new AttributeModifier(
            randomModifierId(),
            this.modifier.amount() + otherBonus.modifier.amount(),
            this.modifier.operation());
    AttributeBonus mergedBonus = new AttributeBonus(this.attribute, mergedModifier);
    mergedBonus.playerMultiplier = this.playerMultiplier;
    mergedBonus.playerCondition = this.playerCondition;
    return mergedBonus;
  }

  @Override
  public MutableComponent getTooltip() {
    float visibleAmount = (float) modifier.amount();
    if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE
        && attribute.equals(Attributes.KNOCKBACK_RESISTANCE)) {
      visibleAmount *= 10;
    }
    MutableComponent tooltip =
        TooltipHelper.getSkillBonusTooltip(
            attribute.value().getDescriptionId(), visibleAmount, modifier.operation());
    tooltip = playerMultiplier.getTooltip(tooltip, Target.PLAYER);
    tooltip = playerCondition.getTooltip(tooltip, "you");
    return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(isPositive()));
  }

  @Override
  public void gatherInfo(Consumer<MutableComponent> consumer) {
    SkillBonus.super.gatherInfo(consumer);
    TooltipHelper.consumeTranslated(attribute.value().getDescriptionId() + ".info", consumer);
  }

  @Override
  public boolean isPositive() {
    return modifier.amount() > 0;
  }

  @Override
  public void addEditorWidgets(
      SkillTreeEditor editor, int index, Consumer<AttributeBonus> consumer) {
    editor.addLabel(0, 0, "Attribute", ChatFormatting.GOLD);
    editor.increaseHeight(19);
    editor
        .addSelectionMenu(0, 0, 200, attribute.value())
        .setResponder(
            attribute ->
                selectAttribute(consumer, BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute)));
    editor.increaseHeight(19);
    editor.addLabel(0, 0, "Amount", ChatFormatting.GOLD);
    editor.addLabel(55, 0, "Operation", ChatFormatting.GOLD);
    editor.increaseHeight(19);
    editor
        .addNumericTextField(0, 0, 50, 14, modifier.amount())
        .setNumericResponder(value -> selectAmount(consumer, value));
    editor
        .addOperationSelection(55, 0, 145, modifier.operation())
        .setResponder(operation -> selectOperation(consumer, operation));
    editor.increaseHeight(19);
    editor.addLabel(0, 0, "Player Condition", ChatFormatting.GOLD);
    editor.increaseHeight(19);
    editor
        .addSelectionMenu(0, 0, 200, playerCondition)
        .setResponder(condition -> selectPlayerCondition(editor, consumer, condition))
        .setMenuInitFunc(() -> addPlayerConditionWidgets(editor, consumer));
    editor.increaseHeight(19);
    editor.addLabel(0, 0, "Player Multiplier", ChatFormatting.GOLD);
    editor.increaseHeight(19);
    editor
        .addSelectionMenu(0, 0, 200, playerMultiplier)
        .setResponder(multiplier -> selectPlayerMultiplier(editor, consumer, multiplier))
        .setMenuInitFunc(() -> addPlayerMultiplierWidgets(editor, consumer));
    editor.increaseHeight(19);
  }

  private void selectPlayerMultiplier(
      SkillTreeEditor editor, Consumer<AttributeBonus> consumer, LivingMultiplier multiplier) {
    setMultiplier(multiplier);
    consumer.accept(this.copy());
    editor.rebuildWidgets();
  }

  private void selectPlayerCondition(
      SkillTreeEditor editor, Consumer<AttributeBonus> consumer, LivingCondition condition) {
    setCondition(condition);
    consumer.accept(this.copy());
    editor.rebuildWidgets();
  }

  private void selectOperation(
      Consumer<AttributeBonus> consumer, AttributeModifier.Operation operation) {
    setOperation(operation);
    consumer.accept(this.copy());
  }

  private void selectAmount(Consumer<AttributeBonus> consumer, Double value) {
    setAmount(value);
    consumer.accept(this.copy());
  }

  private void selectAttribute(Consumer<AttributeBonus> consumer, Holder<Attribute> attribute) {
    setAttribute(attribute);
    consumer.accept(this.copy());
  }

  private void addPlayerConditionWidgets(
      SkillTreeEditor editor, Consumer<AttributeBonus> consumer) {
    playerCondition.addEditorWidgets(
        editor,
        c -> {
          setCondition(c);
          consumer.accept(this.copy());
        });
  }

  private void addPlayerMultiplierWidgets(
      SkillTreeEditor editor, Consumer<AttributeBonus> consumer) {
    playerMultiplier.addEditorWidgets(
        editor,
        m -> {
          setMultiplier(m);
          consumer.accept(this.copy());
        });
  }

  public Holder<Attribute> getAttribute() {
    return attribute;
  }

  public AttributeModifier getModifier() {
    return modifier;
  }

  public void setAttribute(Holder<Attribute> attribute) {
    this.attribute = attribute;
  }

  public void setAmount(double amount) {
    this.modifier = new AttributeModifier(modifier.id(), amount, modifier.operation());
  }

  public void setUUID(UUID id) {
    this.modifier =
        new AttributeModifier(
            ResourceLocation.fromNamespaceAndPath("skilltree", id.toString()),
            modifier.amount(),
            modifier.operation());
  }

  public void setOperation(AttributeModifier.Operation operation) {
    this.modifier = new AttributeModifier(modifier.id(), modifier.amount(), operation);
  }

  public SkillBonus<?> setCondition(LivingCondition condition) {
    this.playerCondition = condition;
    return this;
  }

  public SkillBonus<?> setMultiplier(LivingMultiplier multiplier) {
    this.playerMultiplier = multiplier;
    return this;
  }

  public boolean hasCondition() {
    return playerCondition != NoneLivingCondition.INSTANCE;
  }

  public boolean hasMultiplier() {
    return playerMultiplier != NoneLivingMultiplier.INSTANCE;
  }

  public static class Serializer implements SkillBonus.Serializer {
    @Override
    public AttributeBonus deserialize(JsonObject json) throws JsonParseException {
      Holder<Attribute> attribute = SerializationHelper.deserializeAttribute(json);
      AttributeModifier modifier = SerializationHelper.deserializeAttributeModifier(json);
      AttributeBonus bonus = new AttributeBonus(attribute, modifier);
      bonus.playerMultiplier =
          SerializationHelper.deserializeLivingMultiplier(json, "player_multiplier");
      bonus.playerCondition =
          SerializationHelper.deserializeLivingCondition(json, "player_condition");
      return bonus;
    }

    @Override
    public void serialize(JsonObject json, SkillBonus<?> bonus) {
      if (!(bonus instanceof AttributeBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      SerializationHelper.serializeAttribute(json, aBonus.attribute);
      SerializationHelper.serializeAttributeModifier(json, aBonus.modifier);
      SerializationHelper.serializeLivingMultiplier(
          json, aBonus.playerMultiplier, "player_multiplier");
      SerializationHelper.serializeLivingCondition(
          json, aBonus.playerCondition, "player_condition");
    }

    @Override
    public AttributeBonus deserialize(CompoundTag tag) {
      Holder<Attribute> attribute = SerializationHelper.deserializeAttribute(tag);
      AttributeModifier modifier = SerializationHelper.deserializeAttributeModifier(tag);
      AttributeBonus bonus = new AttributeBonus(attribute, modifier);
      bonus.playerMultiplier =
          SerializationHelper.deserializeLivingMultiplier(tag, "player_multiplier");
      bonus.playerCondition =
          SerializationHelper.deserializeLivingCondition(tag, "player_condition");
      return bonus;
    }

    @Override
    public CompoundTag serialize(SkillBonus<?> bonus) {
      if (!(bonus instanceof AttributeBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      CompoundTag tag = new CompoundTag();
      SerializationHelper.serializeAttribute(tag, aBonus.attribute);
      SerializationHelper.serializeAttributeModifier(tag, aBonus.modifier);
      SerializationHelper.serializeLivingMultiplier(
          tag, aBonus.playerMultiplier, "player_multiplier");
      SerializationHelper.serializeLivingCondition(tag, aBonus.playerCondition, "player_condition");
      return tag;
    }

    @Override
    public AttributeBonus deserialize(FriendlyByteBuf buf) {
      Holder<Attribute> attribute = NetworkHelper.readAttribute(buf);
      AttributeModifier modifier = NetworkHelper.readAttributeModifier(buf);
      AttributeBonus bonus = new AttributeBonus(attribute, modifier);
      bonus.playerMultiplier = NetworkHelper.readLivingMultiplier(buf);
      bonus.playerCondition = NetworkHelper.readLivingCondition(buf);
      return bonus;
    }

    @Override
    public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
      if (!(bonus instanceof AttributeBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      NetworkHelper.writeAttribute(buf, aBonus.attribute);
      NetworkHelper.writeAttributeModifier(buf, aBonus.modifier);
      NetworkHelper.writeLivingMultiplier(buf, aBonus.playerMultiplier);
      NetworkHelper.writeLivingCondition(buf, aBonus.playerCondition);
    }

    @Override
    public SkillBonus<?> createDefaultInstance() {
      return new AttributeBonus(
          Attributes.ARMOR, new AttributeModifier(randomModifierId(), 1, AttributeModifier.Operation.ADD_VALUE));
    }
  }
}