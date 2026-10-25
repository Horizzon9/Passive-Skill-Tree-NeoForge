package daripher.skilltree.mixin.minecraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import daripher.skilltree.potion.PotionHelper;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownPotion.class)
public abstract class ThrownPotionMixin {
  @ModifyExpressionValue(
      method = "onHit",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
  private Object applyEffectBonuses(Object original) {
    ThrownPotion potion = (ThrownPotion) (Object) this;
    return original instanceof PotionContents contents
        ? PotionHelper.applyEffectBonuses(potion.getItem(), contents)
        : original;
  }
}
