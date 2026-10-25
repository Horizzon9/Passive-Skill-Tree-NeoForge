package daripher.skilltree.effect;

import daripher.skilltree.skill.bonus.SkillBonus;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.jetbrains.annotations.NotNull;

public abstract class SkillBonusEffect extends MobEffect {
  private final SkillBonus<?> bonus;
  private final Map<AttributeMap, ServerPlayer> appliedTo = new HashMap<>();

  public SkillBonusEffect(MobEffectCategory category, int color, SkillBonus<?> bonus) {
    super(category, color);
    this.bonus = bonus;
  }

  @Override
  public void onEffectStarted(@NotNull LivingEntity livingEntity, int amplifier) {
    super.onEffectStarted(livingEntity, amplifier);
    if (livingEntity instanceof ServerPlayer player
        && appliedTo.put(livingEntity.getAttributes(), player) == null) {
      bonus.onSkillLearned(player, true);
    }
  }

  @Override
  public void removeAttributeModifiers(@NotNull AttributeMap attributeMap) {
    super.removeAttributeModifiers(attributeMap);
    ServerPlayer player = appliedTo.remove(attributeMap);
    if (player != null) {
      bonus.onSkillRemoved(player);
    }
  }

  @Override
  public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
    return bonus instanceof SkillBonus.Ticking;
  }

  @Override
  public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
    if (entity instanceof ServerPlayer player && bonus instanceof SkillBonus.Ticking ticking) {
      ticking.tick(player);
    }
    return true;
  }

  public SkillBonus<?> getBonus() {
    return bonus;
  }
}