package daripher.skilltree.mixin.minecraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
  @SuppressWarnings("DataFlowIssue")
  public AnvilMenuMixin() {
    super(null, 0, null, null);
  }

  @ModifyExpressionValue(
      method = "createResult",
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I"),
      require = 0)
  private int uncapEnchantmentLevel(int original) {
    ItemStack base = inputSlots.getItem(0);
    if (base.getItem() == Items.ENCHANTED_BOOK) return original;
    ItemStack addition = inputSlots.getItem(1);
    if (EnchantmentHelper.getEnchantmentsForCrafting(base).isEmpty() && addition.getItem() == Items.ENCHANTED_BOOK) {
      return Integer.MAX_VALUE;
    }
    return original;
  }
}
