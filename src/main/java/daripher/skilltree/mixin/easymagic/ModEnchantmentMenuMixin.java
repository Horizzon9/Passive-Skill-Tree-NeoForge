package daripher.skilltree.mixin.easymagic;

import daripher.skilltree.container.ContainerHelper;
import daripher.skilltree.container.menu.EnchantmentMenuExtension;
import daripher.skilltree.skill.bonus.SkillBonusHandler;
import fuzs.easymagic.mixin.accessor.EnchantmentMenuAccessor;
import fuzs.easymagic.world.inventory.ModEnchantmentMenu;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "fuzs.easymagic.world.inventory.ModEnchantmentMenu")
public class ModEnchantmentMenuMixin {
  private @Shadow @Final DataSlot enchantmentSeed;

  @Redirect(
      method = "updateLevelsAndClues",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentCost(Lnet/minecraft/util/RandomSource;IILnet/minecraft/world/item/ItemStack;)I"))
  private int reduceLevelRequirements(RandomSource random, int slot, int power, ItemStack stack) {
    int cost = EnchantmentHelper.getEnchantmentCost(random, slot, power, stack);
    EnchantmentMenuExtension extension = (EnchantmentMenuExtension) this;
    extension.getCostsBeforeReduction()[slot] = cost;
    @SuppressWarnings("DataFlowIssue")
    ModEnchantmentMenu menu = (ModEnchantmentMenu) (Object) this;
    Player player = ContainerHelper.getViewingPlayer(menu);
    if (player == null) return cost;
    return SkillBonusHandler.adjustEnchantmentCost(cost, player);
  }

  @Redirect(
      method = "updateLevelsAndClues",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lfuzs/easymagic/world/inventory/ModEnchantmentMenu;getEnchantmentList(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/ItemStack;II)Ljava/util/List;"))
  private List<EnchantmentInstance> amplifyEnchantments(
      ModEnchantmentMenu menu,
      RegistryAccess registryAccess,
      ItemStack itemStack,
      int slot,
      int reducedCost) {
    EnchantmentMenuExtension extension = (EnchantmentMenuExtension) this;
    EnchantmentMenuAccessor accessor = (EnchantmentMenuAccessor) this;
    int cost = extension.getCostsBeforeReduction()[slot];
    List<EnchantmentInstance> enchantments =
        accessor.callGetEnchantmentList(registryAccess, itemStack, slot, cost);
    Player player = ContainerHelper.getViewingPlayer(menu);
    if (player == null) return enchantments;
    RandomSource random = RandomSource.create(enchantmentSeed.get());
    SkillBonusHandler.amplifyEnchantments(itemStack, enchantments, random, player);
    return enchantments;
  }
}
