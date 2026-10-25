package daripher.skilltree.skill.bonus.condition.enchantment;

import daripher.skilltree.init.PSTRegistries;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public interface EnchantmentCondition {
  boolean met(ItemStack stack, Holder<Enchantment> enchantment);

  default String getDescriptionId() {
    ResourceLocation id = PSTRegistries.ENCHANTMENT_CONDITIONS.get().getKey(getSerializer());
    Objects.requireNonNull(id);
    return "enchantment_condition.%s.%s".formatted(id.getNamespace(), id.getPath());
  }

  EnchantmentCondition.Serializer getSerializer();

  interface Serializer
      extends daripher.skilltree.data.serializers.Serializer<EnchantmentCondition> {
    EnchantmentCondition createDefaultInstance();
  }
}
