package daripher.skilltree.item.gem;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.compat.apotheosis.ApotheosisCompatibility;
import daripher.skilltree.data.reloader.GemTypesReloader;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.entity.player.PlayerHelper;
import daripher.skilltree.init.PSTComponents;
import daripher.skilltree.init.PSTItems;
import daripher.skilltree.item.ItemHelper;
import daripher.skilltree.item.gem.bonus.GemBonusProvider;
import daripher.skilltree.skill.bonus.item.ItemBonus;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = SkillTreeMod.MOD_ID, value = Dist.CLIENT)
public class GemItem extends Item {
  public GemItem() {
    super(new Properties());
  }

  @Override
  public @NotNull String getDescriptionId(@NotNull ItemStack stack) {
    String gemId = getGemType(stack).id().toString().replace(":", ".");
    return getDescriptionId() + "." + gemId;
  }

  @Override
  public boolean isFoil(@NotNull ItemStack stack) {
    return true;
  }

  @Override
  public void appendHoverText(
      @NotNull ItemStack stack,
      @NotNull Item.TooltipContext tooltipContext,
      @NotNull List<Component> components,
      @NotNull TooltipFlag tooltipFlag) {
    if (SkillTreeMod.apotheosisEnabled()) {
      components.add(Component.translatable("gem.disabled").withStyle(ChatFormatting.RED));
      return;
    }
    Component gemTooltip = Component.translatable("gem.tooltip").withStyle(ChatFormatting.YELLOW);
    components.add(gemTooltip);
    appendItemBonusesTooltips(stack, components);
  }

  private static void appendItemBonusesTooltips(
      @NotNull ItemStack stack, @NotNull List<Component> components) {
    getGemType(stack)
        .bonuses()
        .forEach(
            (c, b) -> {
              Component itemDescription = c.getTooltip();
              Component bonusDescription = b.getTooltip(stack);
              Component tooltip =
                  Component.translatable("gem_class_format", itemDescription, bonusDescription)
                      .withStyle(ChatFormatting.GRAY);
              components.add(tooltip);
            });
  }

  public static GemType getGemType(ItemStack gemStack) {
    ResourceLocation id = gemStack.get(PSTComponents.GEM_TYPE.get());
    if (id == null) return GemTypesReloader.NO_TYPE;
    return GemTypesReloader.getGemTypeById(id);
  }

  public static boolean canInsertGem(Player player, ItemStack itemStack, ItemStack gemStack) {
    if (!ItemHelper.canInsertGem(itemStack)) return false;
    GemBonusProvider bonusProvider = getGemType(gemStack).getBonusProvider(itemStack);
    if (bonusProvider == null) return false;
    return bonusProvider.canApply(player, itemStack, gemStack);
  }

  public static void insertGem(Player player, ItemStack itemStack, ItemStack gemStack) {
    GemBonusProvider bonusProvider = getGemType(gemStack).getBonusProvider(itemStack);
    if (bonusProvider == null) return;
    bonusProvider.addGemBonus(player, itemStack, gemStack);
  }

  public static void addGemBonus(
      @Nonnull Player player,
      @Nonnull ItemStack itemStack,
      @Nonnull ItemStack gemStack,
      @Nonnull ItemBonus<?> bonus) {
    GemType gemType = getGemType(gemStack);
    float gemPower = PlayerHelper.getGemPower(player, itemStack);
    bonus = bonus.copy().multiply(gemPower);
    List<CompoundTag> bonusesTag =
        new ArrayList<>(itemStack.getOrDefault(PSTComponents.GEM_BONUSES.get(), List.of()));
    List<ResourceLocation> gemsTag =
        new ArrayList<>(itemStack.getOrDefault(PSTComponents.GEMS.get(), List.of()));
    CompoundTag bonusTag = new CompoundTag();
    SerializationHelper.serializeItemBonus(bonusTag, bonus);
    bonusesTag.add(bonusTag);
    gemsTag.add(gemType.id());
    itemStack.set(PSTComponents.GEM_BONUSES.get(), bonusesTag);
    itemStack.set(PSTComponents.GEMS.get(), gemsTag);
  }

  public static void removeGemBonuses(ItemStack stack) {
    stack.remove(PSTComponents.GEM_BONUSES.get());
    stack.remove(PSTComponents.GEMS.get());
  }

  public static boolean hasGem(ItemStack stack, int socket) {
    return getGemBonuses(stack).size() > socket;
  }

  public static ItemStack getDefaultGemStack(GemType gemType) {
    ItemStack gemStack = new ItemStack(PSTItems.GEM.get());
    gemStack.set(PSTComponents.GEM_TYPE.get(), gemType.id());
    return gemStack;
  }

  public static List<? extends ItemBonus<?>> getGemBonuses(ItemStack stack) {
    if (SkillTreeMod.apotheosisEnabled()) {
      return ApotheosisCompatibility.INSTANCE.getGemBonuses(stack);
    }
    List<CompoundTag> tags = stack.get(PSTComponents.GEM_BONUSES.get());
    if (tags == null) return List.of();
    return tags.stream().map(SerializationHelper::deserializeItemBonus).toList();
  }

  public static List<ItemStack> getGems(ItemStack stack) {
    if (SkillTreeMod.apotheosisEnabled()) {
      return ApotheosisCompatibility.INSTANCE.getGemStacks(stack);
    }
    List<ResourceLocation> gems = stack.get(PSTComponents.GEMS.get());
    if (gems == null) return List.of();
    return gems.stream()
        .map(GemTypesReloader::getGemTypeById)
        .map(GemItem::getDefaultGemStack)
        .toList();
  }

  @SubscribeEvent
  public static void addGemModels(ModelEvent.RegisterAdditional event) {
    Set<ResourceLocation> models =
        Minecraft.getInstance()
            .getResourceManager()
            .listResources(
                "models",
                loc ->
                    SkillTreeMod.MOD_ID.equals(loc.getNamespace())
                        && loc.getPath().contains("/gems/")
                        && loc.getPath().endsWith(".json"))
            .keySet();
    for (ResourceLocation s : models) {
      String path =
          s.getPath().substring("models/".length(), s.getPath().length() - ".json".length());
      event.register(
          ModelResourceLocation.standalone(
              ResourceLocation.fromNamespaceAndPath(SkillTreeMod.MOD_ID, path)));
    }
  }

  @SubscribeEvent
  public static void replaceGemModel(ModelEvent.ModifyBakingResult event) {
    ModelResourceLocation model =
        new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(SkillTreeMod.MOD_ID, "gem"), "inventory");
    BakedModel oldModel = event.getModels().get(model);
    if (oldModel != null) {
      event.getModels().put(model, new GemModel(oldModel));
    }
  }
}
