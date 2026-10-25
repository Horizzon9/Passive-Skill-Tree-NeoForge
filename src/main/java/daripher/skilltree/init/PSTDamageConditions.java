package daripher.skilltree.init;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.skill.bonus.condition.damage.*;

import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class PSTDamageConditions {
  public static final ResourceLocation REGISTRY_ID =
      ResourceLocation.fromNamespaceAndPath(SkillTreeMod.MOD_ID, "damage_conditions");
  public static final DeferredRegister<DamageCondition.Serializer> REGISTRY =
      DeferredRegister.create(REGISTRY_ID, SkillTreeMod.MOD_ID);

  public static final DeferredHolder<DamageCondition.Serializer, DamageCondition.Serializer> NONE =
      REGISTRY.register("none", NoneDamageCondition.Serializer::new);
  public static final DeferredHolder<DamageCondition.Serializer, DamageCondition.Serializer> PROJECTILE =
      REGISTRY.register("projectile", ProjectileDamageCondition.Serializer::new);
  public static final DeferredHolder<DamageCondition.Serializer, DamageCondition.Serializer> MELEE =
      REGISTRY.register("melee", MeleeDamageCondition.Serializer::new);
  public static final DeferredHolder<DamageCondition.Serializer, DamageCondition.Serializer> MAGIC =
      REGISTRY.register("magic", MagicDamageCondition.Serializer::new);

  public static List<DamageCondition> conditionsList() {
    return PSTRegistries.DAMAGE_CONDITIONS.get().stream()
        .map(DamageCondition.Serializer::createDefaultInstance)
        .toList();
  }

  public static String getName(DamageCondition condition) {
    ResourceLocation id = PSTRegistries.DAMAGE_CONDITIONS.get().getKey(condition.getSerializer());
    return TooltipHelper.idToName(Objects.requireNonNull(id).getPath());
  }
}
