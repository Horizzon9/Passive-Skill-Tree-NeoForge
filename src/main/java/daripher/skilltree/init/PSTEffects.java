package daripher.skilltree.init;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.effect.CritDamageBonusEffect;
import daripher.skilltree.effect.DamageBonusEffect;
import daripher.skilltree.effect.LifeRegenerationBonusEffect;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class PSTEffects {
  public static final DeferredRegister<MobEffect> REGISTRY =
      DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, SkillTreeMod.MOD_ID);

  public static final DeferredHolder<MobEffect, MobEffect> DAMAGE_BONUS =
      REGISTRY.register("damage_bonus", DamageBonusEffect::new);
  public static final DeferredHolder<MobEffect, MobEffect> CRIT_DAMAGE_BONUS =
      REGISTRY.register("crit_damage_bonus", CritDamageBonusEffect::new);
  public static final DeferredHolder<MobEffect, MobEffect> LIFE_REGENERATION_BONUS =
      REGISTRY.register("life_regeneration_bonus", LifeRegenerationBonusEffect::new);
}
