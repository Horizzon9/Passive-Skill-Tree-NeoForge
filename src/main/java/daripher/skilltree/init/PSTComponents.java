package daripher.skilltree.init;

import daripher.skilltree.SkillTreeMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.mojang.serialization.Codec;

public class PSTComponents {
  public static final DeferredRegister.DataComponents REGISTRY =
      DeferredRegister.createDataComponents(SkillTreeMod.MOD_ID);

  public static final DeferredHolder<DataComponentType<?>, DataComponentType<java.util.List<CompoundTag>>>
      SKILL_BONUSES =
          REGISTRY.registerComponentType(
              "skill_bonuses",
              builder ->
                  builder
                      .persistent(Codec.list(CompoundTag.CODEC))
                      .networkSynchronized(ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.list())));
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<java.util.List<CompoundTag>>>
      DURABILITY_BONUSES =
          REGISTRY.registerComponentType(
              "durability_bonuses",
              builder ->
                  builder
                      .persistent(Codec.list(CompoundTag.CODEC))
                      .networkSynchronized(ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.list())));
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<java.util.List<CompoundTag>>>
      GEM_BONUSES =
          REGISTRY.registerComponentType(
              "gem_bonuses",
              builder ->
                  builder
                      .persistent(Codec.list(CompoundTag.CODEC))
                      .networkSynchronized(ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.list())));
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<java.util.List<ResourceLocation>>>
      GEMS =
          REGISTRY.registerComponentType(
              "gems",
              builder ->
                  builder
                      .persistent(Codec.list(ResourceLocation.CODEC))
                      .networkSynchronized(ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list())));
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>>
      GEM_TYPE =
          REGISTRY.registerComponentType(
              "gem_type",
              builder ->
                  builder
                      .persistent(ResourceLocation.CODEC)
                      .networkSynchronized(ResourceLocation.STREAM_CODEC));
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<java.util.List<MobEffectInstance>>>
      POISONS =
          REGISTRY.registerComponentType(
              "poisons",
              builder ->
                  builder
                      .persistent(Codec.list(MobEffectInstance.CODEC))
                      .networkSynchronized(MobEffectInstance.STREAM_CODEC.apply(ByteBufCodecs.list())));
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemStack>> ARROWS =
      REGISTRY.registerComponentType(
          "arrows",
          builder ->
              builder
                  .persistent(ItemStack.CODEC)
                  .networkSynchronized(ItemStack.OPTIONAL_STREAM_CODEC));
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ARROWS_COUNT =
      REGISTRY.registerComponentType(
          "arrows_count",
          builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> MIXED_POTION =
      REGISTRY.registerComponentType(
          "mixed_potion",
          builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

  public static void register() {}
}