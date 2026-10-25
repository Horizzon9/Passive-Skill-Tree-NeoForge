package daripher.skilltree.attribute.event;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.config.Config;
import daripher.skilltree.entity.player.PlayerHelper;
import daripher.skilltree.init.PSTAttributes;
import daripher.skilltree.item.ItemHelper;
import daripher.skilltree.mixin.AbstractArrowAccessor;
import daripher.skilltree.mixin.LivingEntityAccessor;
import daripher.skilltree.skill.bonus.EventListenerBonus;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.SkillBonusHandler;
import daripher.skilltree.skill.bonus.condition.item.EquipmentCondition;
import daripher.skilltree.skill.bonus.event.EvasionEventListener;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = SkillTreeMod.MOD_ID)
public class AttributeEvents {
  @SubscribeEvent
  public static void applyLifeRegenerationBonus(PlayerTickEvent.Pre event) {
    Player player = event.getEntity();
    if (player.level().isClientSide) return;
    if (player.getFoodData().getFoodLevel() == 0) return;
    float lifeRegeneration = (float) player.getAttributeValue(PSTAttributes.REGENERATION);
    if (player.getHealth() != player.getMaxHealth() && player.tickCount % 20 == 0) {
      player.heal(lifeRegeneration);
      player.getFoodData().addExhaustion(lifeRegeneration / 5);
    }
  }

  @SubscribeEvent
  public static void applyEvasionBonus(LivingIncomingDamageEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
    double evasion = player.getAttributeValue(PSTAttributes.EVASION);
    double evasionChance = (evasion * 0.05) / (1 + evasion * 0.05) * 0.8;
    if (!(player.getRandom().nextFloat() < evasionChance)) return;
    player
        .level()
        .playSound(null, player, SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.5F, 1.5F);
    event.setCanceled(true);
    for (EventListenerBonus<?> bonus :
        SkillBonusHandler.getSkillBonuses(player, EventListenerBonus.class)) {
      if (!(bonus.getEventListener() instanceof EvasionEventListener listener)) continue;
      SkillBonus<? extends EventListenerBonus<?>> copy = bonus.copy();
      listener.onEvent(player, attacker, (EventListenerBonus<?>) copy);
    }
  }

  @SubscribeEvent
  public static void applyBlockingBonus(LivingIncomingDamageEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    float damage = event.getAmount();
    if (damage <= 0) return;
    DamageSource damageSource = event.getSource();
    if (damageSource.is(DamageTypeTags.BYPASSES_SHIELD)) return;
    Entity attacker = damageSource.getDirectEntity();
    if (attacker instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0) return;
    ItemStack shield = player.getOffhandItem();
    if (!shield.is(Tags.Items.TOOLS_SHIELD)) return;
    double blocking = player.getAttributeValue(PSTAttributes.BLOCKING);
    double blockChance = (blocking * 0.05) / (1 + blocking * 0.05) * 0.8;
    if (player.getRandom().nextFloat() >= blockChance) return;
    event.setCanceled(true);
    player.level().broadcastEntityEvent(player, (byte) 29);
    PlayerHelper.hurtShield(player, shield, damage);
    if (damageSource.is(DamageTypeTags.IS_PROJECTILE)) return;
    if (!(attacker instanceof LivingEntity livingAttacker)) return;
    LivingEntityAccessor entityAccessor = (LivingEntityAccessor) player;
    entityAccessor.invokeBlockUsingShield(livingAttacker);
  }

  @SubscribeEvent
  public static void applyPoisonedWeaponEffects(LivingIncomingDamageEvent event) {
    if (!(event.getSource().getDirectEntity() instanceof Player player)) return;
    ItemStack weapon = player.getMainHandItem();
    if (!ItemHelper.hasPoisons(weapon)) return;
    List<MobEffectInstance> poisons = ItemHelper.getPoisons(weapon);
    poisons.forEach(event.getEntity()::addEffect);
  }

  @SubscribeEvent
  public static void applyPoisonedThrownTridentEffects(LivingIncomingDamageEvent event) {
    DamageSource damageSource = event.getSource();
    if (!(damageSource.getDirectEntity() instanceof ThrownTrident trident)) return;
    AbstractArrowAccessor arrowAccessor = (AbstractArrowAccessor) trident;
    ItemStack weapon = arrowAccessor.invokeGetPickupItem();
    if (weapon == null) return;
    if (!ItemHelper.hasPoisons(weapon)) return;
    List<MobEffectInstance> poisons = ItemHelper.getPoisons(weapon);
    LivingEntity target = event.getEntity();
    for (MobEffectInstance poison : poisons) {
      MobEffectInstance effectInstance = new MobEffectInstance(poison);
      target.addEffect(effectInstance);
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGH)
  public static void addPoisonedWeaponTooltips(ItemTooltipEvent event) {
    ItemStack weapon = event.getItemStack();
    if (!ItemHelper.hasPoisons(weapon)) return;
    List<Component> tooltips = event.getToolTip();
    tooltips.add(Component.empty());
    tooltips.add(Component.translatable("weapon.poisoned").withStyle(ChatFormatting.DARK_PURPLE));
    for (MobEffectInstance poison : ItemHelper.getPoisons(weapon)) {
      Component tooltip = TooltipHelper.getEffectInstanceTooltip(poison);
      tooltips.add(tooltip);
    }
  }

  @SubscribeEvent
  public static void applyExperiencePerMinuteBonus(PlayerTickEvent.Pre event) {
    Player player = event.getEntity();
    if (player.level().isClientSide) return;
    float bonus = (float) (player.getAttributeValue(PSTAttributes.EXP_PER_MINUTE));
    int frequency = Math.max((int) (1200 / bonus), 1);
    if (player.tickCount % frequency == 0) {
      ExperienceOrb expOrb =
          new ExperienceOrb(player.level(), player.getX(), player.getY(), player.getZ(), 1);
      player.level().addFreshEntity(expOrb);
    }
  }

  @SubscribeEvent
  public static void applyStealthBonus(LivingChangeTargetEvent event) {
    if (!(event.getNewAboutToBeSetTarget() instanceof Player player)) return;
    double stealth = player.getAttributeValue(PSTAttributes.STEALTH) / 100d;
    if (stealth == 0) return;
    LivingEntity attacker = event.getEntity();
    double followRange = attacker.getAttributeValue(Attributes.FOLLOW_RANGE);
    if (attacker.distanceTo(player) > followRange * (1 - stealth)) {
      event.setCanceled(true);
    }
  }

  @SubscribeEvent
  public static void applyGrindstoneExpPenalty(GrindstoneEvent.OnTakeItem event) {
    event.setXp((int) (event.getXp() * Config.grindstone_exp_multiplier));
  }

  @SubscribeEvent
  public static void applyRangedWeaponAttackSpeedBonus(LivingEntityUseItemEvent.Tick event) {
    if (!EquipmentCondition.isRangedWeapon(event.getItem())) return;
    AttributeInstance attribute = event.getEntity().getAttribute(Attributes.ATTACK_SPEED);
    if (attribute == null) return;
    double baseAttackSpeed = attribute.getBaseValue();
    if (baseAttackSpeed == 0) return;
    double attackSpeedBonus = attribute.getValue() / baseAttackSpeed - 1;
    if (attackSpeedBonus == 0) return;
    int tickBonus = attackSpeedBonus < 0 ? 1 : -1;
    while (attackSpeedBonus > 1) {
      event.setDuration(event.getDuration() + tickBonus);
      attackSpeedBonus--;
    }
    int bonusTickFrequency = (int) (1f / attackSpeedBonus);
    if (event.getEntity().tickCount % bonusTickFrequency == 0) {
      event.setDuration(event.getDuration() + tickBonus);
    }
  }
}