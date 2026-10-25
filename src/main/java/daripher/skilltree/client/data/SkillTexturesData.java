package daripher.skilltree.client.data;

import daripher.skilltree.SkillTreeMod;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(
    modid = SkillTreeMod.MOD_ID,
        value = Dist.CLIENT)
public class SkillTexturesData implements ResourceManagerReloadListener {
  public static final Set<ResourceLocation> TOOLTIP_BACKGROUNDS = new TreeSet<>();
  public static final Set<ResourceLocation> BORDERS = new TreeSet<>();
  public static final Set<ResourceLocation> ICONS = new TreeSet<>();

  @SubscribeEvent
  public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(new SkillTexturesData());
  }

  @Override
  public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
    reloadTextures(TOOLTIP_BACKGROUNDS, "tooltip", resourceManager);
    reloadTextures(BORDERS, "icons/background", resourceManager);
    reloadTextures(ICONS, "icons", resourceManager);
    ICONS.removeAll(BORDERS);
  }

  private void reloadTextures(
      Set<ResourceLocation> storage, String folder, ResourceManager resourceManager) {
    storage.clear();
    storage.addAll(
        resourceManager
            .listResources("textures/" + folder, l -> l.getPath().endsWith(".png"))
            .keySet());
  }
}
