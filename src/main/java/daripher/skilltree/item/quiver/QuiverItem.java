package daripher.skilltree.item.quiver;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.init.PSTComponents;
import daripher.skilltree.item.ItemBonusProvider;
import daripher.skilltree.item.ItemHelper;
import daripher.skilltree.skill.bonus.item.ItemBonus;
import daripher.skilltree.skill.bonus.item.QuiverCapacityBonus;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

@EventBusSubscriber(modid = SkillTreeMod.MOD_ID)
public class QuiverItem extends Item implements ICurioItem, ItemBonusProvider {
  private final int capacity;

  public QuiverItem(int capacity) {
    super(new Properties().stacksTo(1).durability(capacity));
    this.capacity = capacity;
  }

  public QuiverItem() {
    this(250);
  }

  @SubscribeEvent(priority = EventPriority.HIGH)
  public static void storeArrowsOnPickup(ItemEntityPickupEvent.Pre event) {
    ItemStack arrows = event.getItemEntity().getItem();
    if (!(arrows.getItem() instanceof ArrowItem)) return;
    CuriosApi.getCuriosInventory(event.getPlayer())
        .ifPresent(
            inv -> {
              Optional<SlotResult> curio = inv.findFirstCurio(ItemHelper::isQuiver);
              if (curio.isEmpty()) return;
              ItemStack quiver = curio.get().stack();
              if (quiver.isEmpty() || isFull(quiver)) return;
              if (!containsArrows(quiver)) {
                setArrows(quiver, arrows.copy(), arrows.getCount());
                event.getItemEntity().setItem(ItemStack.EMPTY);
                event.setCanPickup(TriState.FALSE);
              } else if (ItemStack.isSameItemSameComponents(getArrows(quiver), arrows)) {
                int capacity = getCapacity(quiver);
                int arrowsTaken = Math.min(capacity - getArrowsCount(quiver), arrows.getCount());
                addArrows(quiver, arrowsTaken);
                if (arrows.getCount() == arrowsTaken) {
                  event.getItemEntity().setItem(ItemStack.EMPTY);
                  event.setCanPickup(TriState.FALSE);
                } else {
                  arrows.shrink(arrowsTaken);
                }
              }
            });
  }

  @SubscribeEvent
  public static void takeArrowFromQuiver(LivingGetProjectileEvent event) {
    CuriosApi.getCuriosInventory(event.getEntity())
        .ifPresent(
            inv -> {
              Optional<SlotResult> curio = inv.findFirstCurio(ItemHelper::isQuiver);
              if (curio.isEmpty()) return;
              ItemStack quiver = curio.get().stack();
              if (quiver.isEmpty()) return;
              if (containsArrows(quiver)) {
                event.setProjectileItemStack(getArrows(quiver).copy());
              }
            });
  }

  @SubscribeEvent
  public static void removeArrowFromQuiver(ArrowLooseEvent event) {
    if (event.getEntity().isCreative()) return;
    CuriosApi.getCuriosInventory(event.getEntity())
        .ifPresent(
            inv -> {
              Optional<SlotResult> curio = inv.findFirstCurio(ItemHelper::isQuiver);
              if (curio.isEmpty()) return;
              ItemStack quiver = curio.get().stack();
              if (quiver.isEmpty()) return;
              if (containsArrows(quiver)) {
                addArrows(quiver, -1);
              }
            });
  }

  public static boolean isFull(ItemStack quiver) {
    return getArrowsCount(quiver) == getCapacity(quiver);
  }

  public static boolean isEmpty(ItemStack quiver) {
    return getArrowsCount(quiver) == 0;
  }

  public static int getCapacity(ItemStack quiver) {
    int capacity = ((QuiverItem) quiver.getItem()).capacity;
    capacity += (int) getCapacityBonus(quiver, AttributeModifier.Operation.ADD_VALUE);
    float multiplier = 1f + getCapacityBonus(quiver, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    capacity = (int) (capacity * multiplier);
    multiplier = 1f + getCapacityBonus(quiver, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    capacity = (int) (capacity * multiplier);
    return capacity;
  }

  private static float getCapacityBonus(ItemStack quiver, AttributeModifier.Operation operation) {
    return ItemHelper.getItemBonuses(quiver, QuiverCapacityBonus.class).stream()
        .filter(b -> b.getOperation() == operation)
        .map(QuiverCapacityBonus::getAmount)
        .reduce(Float::sum)
        .orElse(0f);
  }

  public static boolean containsArrows(ItemStack stack) {
    return !getArrows(stack).isEmpty() && getArrowsCount(stack) > 0;
  }

  public static ItemStack getArrows(ItemStack stack) {
    ItemStack arrows = stack.get(PSTComponents.ARROWS.get());
    return arrows == null ? ItemStack.EMPTY : arrows;
  }

  public static int getArrowsCount(ItemStack stack) {
    Integer count = stack.get(PSTComponents.ARROWS_COUNT.get());
    return count == null ? 0 : count;
  }

  public static void addArrows(ItemStack stack, ItemStack arrows, int count) {
    if (isEmpty(stack)) setArrows(stack, arrows);
    addArrows(stack, count);
  }

  public static void setArrows(ItemStack stack, ItemStack arrows, int count) {
    arrows = arrows.copy();
    arrows.setCount(1);
    stack.set(PSTComponents.ARROWS.get(), arrows);
    setArrowsCount(stack, count);
  }

  public static void setArrows(ItemStack stack, ItemStack arrow) {
    stack.set(PSTComponents.ARROWS.get(), arrow.copy());
  }

  public static void setArrowsCount(ItemStack stack, int count) {
    stack.set(PSTComponents.ARROWS_COUNT.get(), count);
  }

  public static void addArrows(ItemStack stack, int count) {
    setArrowsCount(stack, getArrowsCount(stack) + count);
  }

  @Override
  public void appendHoverText(
      @NotNull ItemStack stack,
      Item.TooltipContext context,
      List<Component> components,
      @NotNull TooltipFlag tooltipFlag) {
    Component capacity = Component.literal("" + getCapacity(stack)).withStyle(ChatFormatting.BLUE);
    components.add(
        Component.translatable("quiver.capacity", capacity).withStyle(ChatFormatting.YELLOW));
    if (!containsArrows(stack)) return;
    ItemStack arrows = getArrows(stack);
    Component arrowName =
        Component.empty().append(arrows.getHoverName()).withStyle(ChatFormatting.GRAY);
    Component contents =
        Component.translatable("quiver.contents", arrowName).withStyle(ChatFormatting.YELLOW);
    components.add(contents);
    arrows.getItem().appendHoverText(arrows, context, components, tooltipFlag);
  }

  @Override
  public int getDamage(ItemStack stack) {
    if (!containsArrows(stack)) return 0;
    return getCapacity(stack) - getArrowsCount(stack);
  }

  @Override
  public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
    return false;
  }

  @Override
  public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
    return false;
  }

  @Override
  public @NotNull InteractionResultHolder<ItemStack> use(
      Level level, Player player, @NotNull InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (level.isClientSide) return InteractionResultHolder.success(stack);
    int arrowsLeft = getArrowsCount(stack);
    while (arrowsLeft >= 64) {
      dropArrows(player, stack, 64);
      arrowsLeft -= 64;
    }
    if (arrowsLeft > 0) dropArrows(player, stack, arrowsLeft);
    setArrows(stack, ItemStack.EMPTY, 0);
    return InteractionResultHolder.success(stack);
  }

  @Override
  public List<Component> getAttributesTooltip(
      List<Component> tooltips, Item.TooltipContext context, ItemStack stack) {
    getItemBonuses().stream().map(ItemBonus::getTooltip).forEach(tooltips::add);
    return tooltips;
  }

  @Override
  public @NotNull List<ItemBonus<?>> getItemBonuses() {
    return List.of();
  }

  private void dropArrows(Player player, ItemStack stack, int count) {
    ItemStack arrowsStack = getArrows(stack).copy();
    arrowsStack.setCount(count);
    player.spawnAtLocation(arrowsStack);
  }
}