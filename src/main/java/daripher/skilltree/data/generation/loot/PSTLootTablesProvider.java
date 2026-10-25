package daripher.skilltree.data.generation.loot;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import daripher.skilltree.data.generation.PSTGemTypesProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.NotNull;

public class PSTLootTablesProvider extends LootTableProvider {

  public PSTLootTablesProvider(
      DataGenerator generator,
      PSTGemTypesProvider gemTypesProvider,
      CompletableFuture<HolderLookup.Provider> registries) {
    super(
        generator.getPackOutput(),
        Set.of(),
        List.of(
            new SubProviderEntry(
                provider -> new PSTBlockLoot(gemTypesProvider), LootContextParamSets.BLOCK)),
        registries);
  }

  @Override
  protected void validate(
      @NotNull WritableRegistry<LootTable> registry,
      @NotNull ValidationContext ctx,
      @NotNull ProblemReporter.Collector reporter) {}
}
