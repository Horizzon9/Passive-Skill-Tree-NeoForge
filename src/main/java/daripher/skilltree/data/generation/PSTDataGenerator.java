package daripher.skilltree.data.generation;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.data.generation.loot.PSTLootTablesProvider;
import daripher.skilltree.data.generation.skills.PSTSkillTreesProvider;
import daripher.skilltree.data.generation.skills.PSTSkillsProvider;
import daripher.skilltree.data.generation.translation.PSTEnglishTranslationProvider;
import daripher.skilltree.data.generation.translation.PSTRussianTranslationProvider;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = SkillTreeMod.MOD_ID)
public class PSTDataGenerator {
  @SubscribeEvent
  public static void onGatherData(GatherDataEvent event) {
    DataGenerator dataGenerator = event.getGenerator();
    ExistingFileHelper fileHelper = event.getExistingFileHelper();
    CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

    PSTBlockTagsProvider blockTagsProvider =
        new PSTBlockTagsProvider(dataGenerator, lookupProvider, fileHelper);
    dataGenerator.addProvider(event.includeServer(), blockTagsProvider);
    dataGenerator.addProvider(
        event.includeServer(),
        new PSTItemTagsProvider(dataGenerator, lookupProvider, blockTagsProvider, fileHelper));
    PSTGemTypesProvider gemTypesProvider = new PSTGemTypesProvider(dataGenerator);
    dataGenerator.addProvider(event.includeServer(), gemTypesProvider);
    dataGenerator.addProvider(
        event.includeServer(),
        new PSTLootTablesProvider(dataGenerator, gemTypesProvider, lookupProvider));
    dataGenerator.addProvider(
        event.includeServer(), new PSTRecipesProvider(dataGenerator, lookupProvider));

    dataGenerator.addProvider(
        event.includeClient(), new PSTEnglishTranslationProvider(dataGenerator));
    dataGenerator.addProvider(
        event.includeClient(), new PSTRussianTranslationProvider(dataGenerator));
    dataGenerator.addProvider(
        event.includeClient(),
        new PSTItemModelsProvider(dataGenerator, fileHelper, gemTypesProvider));

    PSTSkillsProvider skillsProvider = new PSTSkillsProvider(dataGenerator);
    dataGenerator.addProvider(event.includeServer(), skillsProvider);
    dataGenerator.addProvider(
        event.includeServer(), new PSTSkillTreesProvider(dataGenerator, skillsProvider));
  }
}
