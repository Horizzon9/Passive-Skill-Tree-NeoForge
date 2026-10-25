package daripher.skilltree.mixin.minecraft;

import daripher.skilltree.container.ContainerHelper;
import daripher.skilltree.container.menu.EnchantmentMenuExtension;
import daripher.skilltree.skill.bonus.SkillBonusHandler;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin implements EnchantmentMenuExtension {
  private final int[] costsBeforeReduction = new int[3];
  public @Shadow @Final int[] costs;
  private @Shadow @Final DataSlot enchantmentSeed;

  @Redirect(
      method = "lambda$slotsChanged$0",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/neoforged/neoforge/event/EventHooks;onEnchantmentLevelSet(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;IILnet/minecraft/world/item/ItemStack;I)I"))
  private int reduceEnchantmentCost(
      Level level, BlockPos pos, int slot, int power, ItemStack itemStack, int enchantmentLevel) {
    int cost =
        EventHooks.onEnchantmentLevelSet(level, pos, slot, power, itemStack, costs[slot]);
    costsBeforeReduction[slot] = cost;
    @SuppressWarnings("DataFlowIssue")
    EnchantmentMenu menu = (EnchantmentMenu) (Object) this;
    Player player = ContainerHelper.getViewingPlayer(menu);
    if (player == null) return cost;
    return SkillBonusHandler.adjustEnchantmentCost(cost, player);
  }

  @Redirect(
      method = "lambda$slotsChanged$0",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/inventory/EnchantmentMenu;getEnchantmentList(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/ItemStack;II)Ljava/util/List;"))
  private List<EnchantmentInstance> amplifyEnchantmentsVisually(
      EnchantmentMenu menu, RegistryAccess registryAccess, ItemStack itemStack, int slot, int cost) {
    return amplifyEnchantments(registryAccess, itemStack, slot);
  }

  @Redirect(
      method = "lambda$clickMenuButton$1",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/inventory/EnchantmentMenu;getEnchantmentList(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/ItemStack;II)Ljava/util/List;"))
  private List<EnchantmentInstance> amplifyEnchantmentsOnButtonClick(
      EnchantmentMenu menu, RegistryAccess registryAccess, ItemStack itemStack, int slot, int cost) {
    return amplifyEnchantments(registryAccess, itemStack, slot);
  }

  private List<EnchantmentInstance> amplifyEnchantments(
      RegistryAccess registryAccess, ItemStack itemStack, int slot) {
    List<EnchantmentInstance> enchantments =
        getEnchantmentList(registryAccess, itemStack, slot, costsBeforeReduction[slot]);
    RandomSource random = RandomSource.create(enchantmentSeed.get());
    @SuppressWarnings("DataFlowIssue")
    EnchantmentMenu menu = (EnchantmentMenu) (Object) this;
    Player player = ContainerHelper.getViewingPlayer(menu);
    if (player == null) return enchantments;
    SkillBonusHandler.amplifyEnchantments(itemStack, enchantments, random, player);
    return enchantments;
  }

  @SuppressWarnings({"unused", "DataFlowIssue"})
  @Shadow
  private @Nonnull List<EnchantmentInstance> getEnchantmentList(
      RegistryAccess registryAccess, ItemStack stack, int slot, int cost) {
    return null;
  }

  @Override
  public int[] getCostsBeforeReduction() {
    return costsBeforeReduction;
  }

  @Override
  public int getEnchantmentSeed() {
    return enchantmentSeed.get();
  }
}
