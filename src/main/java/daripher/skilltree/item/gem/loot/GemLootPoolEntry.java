package daripher.skilltree.item.gem.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.compat.apotheosis.ApotheosisCompatibility;
import daripher.skilltree.data.reloader.GemTypesReloader;
import daripher.skilltree.item.gem.GemItem;
import daripher.skilltree.item.gem.GemType;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

public class GemLootPoolEntry extends LootPoolSingletonContainer {
  public static final MapCodec<GemLootPoolEntry> CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      ResourceLocation.CODEC
                          .fieldOf("gem")
                          .forGetter(entry -> entry.gemTypeId))
                  .and(singletonFields(instance))
                  .apply(instance, GemLootPoolEntry::new));
  public static final LootPoolEntryType TYPE = new LootPoolEntryType(CODEC);
  private final ResourceLocation gemTypeId;

  private GemLootPoolEntry(
      ResourceLocation gemTypeId,
      int weight,
      int quality,
      List<LootItemCondition> conditions,
      List<LootItemFunction> functions) {
    super(weight, quality, conditions, functions);
    this.gemTypeId = gemTypeId;
  }

  @Override
  protected void createItemStack(
      @NotNull Consumer<ItemStack> consumer, @NotNull LootContext context) {
    if (SkillTreeMod.apotheosisEnabled()) {
      ApotheosisCompatibility.INSTANCE.createGemStack(consumer, context, gemTypeId);
      return;
    }
    GemType gemType = GemTypesReloader.getGemTypeById(gemTypeId);
    consumer.accept(GemItem.getDefaultGemStack(gemType));
  }

  @Override
  public @NotNull LootPoolEntryType getType() {
    return TYPE;
  }

  public static class Builder extends LootPoolSingletonContainer.Builder<GemLootPoolEntry.Builder> {
    private final ResourceLocation gemTypeId;

    public Builder(ResourceLocation gemTypeId) {
      this.gemTypeId = gemTypeId;
    }

    @Override
    protected @NotNull Builder getThis() {
      return this;
    }

    @Override
    public @NotNull LootPoolEntryContainer build() {
      return new GemLootPoolEntry(
          gemTypeId,
          GemLootHandler.getGemLootWeight(gemTypeId),
          GemLootHandler.getGemLootQuality(gemTypeId),
          getConditions(),
          getFunctions());
    }
  }
}