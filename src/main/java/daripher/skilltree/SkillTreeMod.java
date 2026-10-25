package daripher.skilltree;

import daripher.skilltree.compat.apotheosis.ApotheosisCompatibility;
import daripher.skilltree.compat.attributeslib.AttributesLibCompatibility;
import daripher.skilltree.compat.tetra.TetraCompatibility;
import daripher.skilltree.config.ClientConfig;
import daripher.skilltree.config.Config;
import daripher.skilltree.init.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SkillTreeMod.MOD_ID)
public class SkillTreeMod {
  public static final String MOD_ID = "skilltree";
  public static final Logger LOGGER = LogManager.getLogger(SkillTreeMod.MOD_ID);

  public SkillTreeMod(IEventBus modEventBus, ModContainer modContainer) {
    PSTItems.REGISTRY.register(modEventBus);
    PSTAttributes.REGISTRY.register(modEventBus);
    PSTRecipeSerializers.REGISTRY.register(modEventBus);
    PSTEffects.REGISTRY.register(modEventBus);
    PSTCreativeTabs.REGISTRY.register(modEventBus);
    PSTSkillBonuses.REGISTRY.register(modEventBus);
    PSTLivingConditions.REGISTRY.register(modEventBus);
    PSTLivingMultipliers.REGISTRY.register(modEventBus);
    PSTDamageConditions.REGISTRY.register(modEventBus);
    PSTItemBonuses.REGISTRY.register(modEventBus);
    PSTItemConditions.REGISTRY.register(modEventBus);
    PSTEnchantmentConditions.REGISTRY.register(modEventBus);
    PSTLootPoolEntries.REGISTRY.register(modEventBus);
    PSTGemBonuses.REGISTRY.register(modEventBus);
    PSTEventListeners.REGISTRY.register(modEventBus);
    PSTAttachments.REGISTRY.register(modEventBus);
    PSTComponents.REGISTRY.register(modEventBus);
    modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    addCompatibilities();
  }

  public static boolean apotheosisEnabled() {
    return isModLoaded("apotheosis") && ApotheosisCompatibility.INSTANCE.adventureModuleEnabled();
  }

  public static boolean isModLoaded(String modId) {
    return net.neoforged.fml.ModList.get().isLoaded(modId);
  }

  public static void errorMissingAttribute(
      net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute) {
    LOGGER.error(
        "Attempting to add attribute modifier to attribute {}, which is not present for player",
        attribute);
  }

  protected void addCompatibilities() {
    if (isModLoaded("apotheosis")) ApotheosisCompatibility.INSTANCE.register();
    if (isModLoaded("tetra")) TetraCompatibility.INSTANCE.register();
    if (isModLoaded("apothic_attributes")) AttributesLibCompatibility.INSTANCE.register();
  }
}