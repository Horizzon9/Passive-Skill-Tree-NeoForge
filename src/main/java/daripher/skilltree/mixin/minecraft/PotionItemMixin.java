package daripher.skilltree.mixin.minecraft;

import daripher.skilltree.potion.PotionHelper;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PotionItem.class)
public class PotionItemMixin extends Item {
  @SuppressWarnings("DataFlowIssue")
  private PotionItemMixin() {
    super(null);
  }

  @Override
  public @NotNull Component getName(@NotNull ItemStack stack) {
    if (PotionHelper.isMixture(stack)) return getMixtureName(stack);
    if (PotionHelper.getDurationMultiplier(stack) != 1f
        || PotionHelper.getAmplifierBonus(stack) != 0) {
      return getSuperiorPotionName(stack);
    }
    return super.getName(stack);
  }

  protected Component getMixtureName(ItemStack stack) {
    String mixtureId = getMixtureId(stack);
    MutableComponent translatedName = Component.translatable(mixtureId);
    if (translatedName.getString().equals(mixtureId)) {
      return Component.translatable(getDescriptionId() + ".mixture");
    }
    return translatedName;
  }

  protected Component getSuperiorPotionName(ItemStack stack) {
    Component potionName = super.getName(stack);
    return Component.translatable("potion.superior", potionName);
  }

  @ModifyExpressionValue(
      method = "finishUsingItem",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
  private Object applyEffectBonusesOnDrink(Object original, ItemStack stack) {
    return original instanceof PotionContents contents
        ? PotionHelper.applyEffectBonuses(stack, contents)
        : original;
  }

  @ModifyExpressionValue(
      method = "appendHoverText",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
  private Object applyEffectBonusesOnTooltip(Object original, ItemStack stack) {
    return original instanceof PotionContents contents
        ? PotionHelper.applyEffectBonuses(stack, contents)
        : original;
  }

  @Inject(method = "appendHoverText", at = @At("TAIL"))
  public void addAdvancedTooltip(
      ItemStack itemStack,
      Item.TooltipContext context,
      List<Component> components,
      TooltipFlag tooltipFlag,
      CallbackInfo callbackInfo) {
    if (tooltipFlag != TooltipFlag.Default.ADVANCED) return;
    addAdvancedTooltip(itemStack, components);
  }

  private void addAdvancedTooltip(ItemStack itemStack, List<Component> components) {
    if (PotionHelper.isMixture(itemStack)) {
      PotionHelper.getMobEffects(itemStack).stream()
          .map(MobEffectInstance::getEffect)
          .map(Holder::value)
          .map(MobEffect::getDescriptionId)
          .map(s -> s.replaceAll("effect.", ""))
          .map(Component::literal)
          .map(c -> c.withStyle(ChatFormatting.DARK_GRAY))
          .forEach(components::add);
    }
  }

  protected String getMixtureId(ItemStack itemStack) {
    StringBuilder name = new StringBuilder(getDescriptionId() + ".mixture");
    PotionHelper.getMobEffects(itemStack).stream()
        .map(MobEffectInstance::getEffect)
          .map(Holder::value)
        .map(MobEffect::getDescriptionId)
        .map(id -> id.replaceAll("effect.", ""))
        .forEach(id -> name.append(".").append(id));
    return name.toString();
  }
}
