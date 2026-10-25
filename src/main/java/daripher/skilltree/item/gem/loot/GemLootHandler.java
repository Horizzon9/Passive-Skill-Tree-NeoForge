package daripher.skilltree.item.gem.loot;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.config.Config;
import daripher.skilltree.skill.bonus.SkillBonusHandler;
import daripher.skilltree.skill.bonus.player.LootDuplicationBonus;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = SkillTreeMod.MOD_ID)
public class GemLootHandler {
  @SubscribeEvent
  public static void dropGemFromOre(BlockEvent.BreakEvent event) {
    Player player = event.getPlayer();
    if (!canDropGem(event, player)) return;
    ServerLevel level = (ServerLevel) player.level();
    LootTable lootTable = getGemsLootTable(level);
    LootParams lootParams = createGemsLootParams(event, level, player);
    float multiplier = getGemLootMultiplier(player);
    if (player.getRandom().nextFloat() < multiplier % 1) {
      multiplier++;
    }
    List<ItemStack> foundGems = lootTable.getRandomItems(lootParams);
    for (int i = 0; i < (int) multiplier; i++) {
      foundGems.stream()
          .map(ItemStack::copy)
          .forEach(s -> Block.popResource(level, event.getPos(), s));
    }
  }

  private static float getGemLootMultiplier(Player player) {
    return 1f + SkillBonusHandler.getLootMultiplier(player, LootDuplicationBonus.LootType.GEMS);
  }

  public static int getGemLootWeight(ResourceLocation gemId) {
    if (gemId.getPath().contains("vacucite")) {
      return 200;
    }
    int tier = Integer.parseInt(gemId.getPath().substring(gemId.getPath().length() - 1));
    return switch (tier) {
      case 0 -> 1000;
      case 1 -> 350;
      case 2 -> 100;
      case 3 -> 10;
      default -> 0;
    };
  }

  public static int getGemLootQuality(ResourceLocation gemId) {
    if (gemId.getPath().contains("vacucite")) {
      return 1;
    }
    int tier = Integer.parseInt(gemId.getPath().substring(gemId.getPath().length() - 1));
    return switch (tier) {
      case 0 -> -50;
      case 1 -> -10;
      case 2 -> -1;
      case 4 -> 5;
      case 5 -> 15;
      default -> 0;
    };
  }

  private static boolean canDropGem(BlockEvent.BreakEvent event, Player player) {
    if (player.isCreative()) return false;
    if (player.level().isClientSide) return false;
    if (Config.gem_drop_chance == 0) return false;
    if (!player.level().getBlockState(event.getPos()).is(Tags.Blocks.ORES)) return false;
    if (player.getRandom().nextFloat() >= Config.gem_drop_chance) return false;
    if (!event.getState().canHarvestBlock(player.level(), event.getPos(), player)) return false;
    return player.getMainHandItem().getEnchantments().keySet().stream()
        .noneMatch(enchantment -> enchantment.is(Enchantments.SILK_TOUCH));
  }

  @NotNull
  private static LootTable getGemsLootTable(ServerLevel serverLevel) {
    String name = SkillTreeMod.apotheosisEnabled() ? "apotheosis_gems" : "gems";
    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SkillTreeMod.MOD_ID, name);
    ResourceKey<LootTable> tableKey = ResourceKey.create(Registries.LOOT_TABLE, id);
    return serverLevel.getServer().reloadableRegistries().getLootTable(tableKey);
  }

  @NotNull
  private static LootParams createGemsLootParams(
      BlockEvent.BreakEvent event, ServerLevel serverLevel, Player player) {
    return new LootParams.Builder(serverLevel)
        .withParameter(LootContextParams.BLOCK_STATE, event.getState())
        .withParameter(LootContextParams.THIS_ENTITY, player)
        .withParameter(
            LootContextParams.ORIGIN,
            new Vec3(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ()))
        .withParameter(LootContextParams.TOOL, player.getMainHandItem())
        .withLuck(player.getLuck())
        .create(LootContextParamSets.BLOCK);
  }
}
