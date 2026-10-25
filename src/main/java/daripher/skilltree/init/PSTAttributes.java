package daripher.skilltree.init;

import daripher.skilltree.SkillTreeMod;
import java.util.Collection;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

@EventBusSubscriber(modid = SkillTreeMod.MOD_ID)
public class PSTAttributes {
  public static final DeferredRegister<Attribute> REGISTRY =
      DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, SkillTreeMod.MOD_ID);

  public static final DeferredHolder<Attribute, Attribute> EXP_PER_MINUTE = create("exp_per_minute", 100d);
  public static final DeferredHolder<Attribute, Attribute> EVASION = create("evasion", 90d);
  public static final DeferredHolder<Attribute, Attribute> REGENERATION = create("regeneration", 100d);
  public static final DeferredHolder<Attribute, Attribute> BLOCKING = create("blocking", 90d);
  public static final DeferredHolder<Attribute, Attribute> STEALTH = create("stealth", 90d);

  private static DeferredHolder<Attribute, Attribute> create(String name, double maxValue) {
    String descriptionId = "attribute.name.%s.%s".formatted(SkillTreeMod.MOD_ID, name);
    return REGISTRY.register(
        name, () -> new RangedAttribute(descriptionId, 0d, 0d, maxValue).setSyncable(true));
  }

  @SubscribeEvent
  public static void attachAttributes(EntityAttributeModificationEvent event) {
    REGISTRY.getEntries().stream()
        .forEach(attribute -> event.add(EntityType.PLAYER, attribute));
  }

  public static Collection<Attribute> attributeList() {
    AttributeSupplier supplier = DefaultAttributes.getSupplier(EntityType.PLAYER);
    return BuiltInRegistries.ATTRIBUTE.holders()
        .filter(supplier::hasAttribute)
        .map(Holder::value)
        .map(Attribute.class::cast)
        .toList();
  }

  public static String getName(Attribute attribute) {
    ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
    Objects.requireNonNull(id);
    return id.toString();
  }
}
