package daripher.skilltree.data.generation;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.init.PSTItems;
import daripher.skilltree.item.gem.GemType;
import java.util.Collection;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

public class PSTItemModelsProvider extends ItemModelProvider {
  private final PSTGemTypesProvider gemTypesProvider;

  public PSTItemModelsProvider(
      DataGenerator dataGenerator,
      ExistingFileHelper existingFileHelper,
      PSTGemTypesProvider gemTypesProvider) {
    super(dataGenerator.getPackOutput(), SkillTreeMod.MOD_ID, existingFileHelper);
    this.gemTypesProvider = gemTypesProvider;
  }

  @Override
  protected void registerModels() {
    Collection<DeferredHolder<Item, ? extends Item>> items = PSTItems.REGISTRY.getEntries();
    items.stream().map(DeferredHolder::get).forEach(this::basicItem);
    gemTypesProvider.getGemTypes().values().forEach(this::gemModels);
  }

  public void gemModels(GemType gemType) {
    String gemName = gemType.id().getPath();
    ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("skilltree", "item/gems/" + gemName);
    ModelFile.UncheckedModelFile parent = new ModelFile.UncheckedModelFile("item/generated");
    getBuilder("apotheosis:item/gems/" + gemName).parent(parent).texture("layer0", texture);
    getBuilder("skilltree:item/gems/" + gemName).parent(parent).texture("layer0", texture);
  }
}
