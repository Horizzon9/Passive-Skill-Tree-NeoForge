package daripher.skilltree.skill.bonus.condition.enchantment;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.init.PSTEnchantmentConditions;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import daripher.skilltree.skill.bonus.condition.item.EquipmentCondition;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class ArmorEnchantmentCondition implements EnchantmentCondition {
  @Override
  public boolean met(ItemStack stack, Holder<Enchantment> enchantment) {
    return EquipmentCondition.isArmor(stack);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    return o != null && getClass() == o.getClass();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getSerializer());
  }

  @Override
  public EnchantmentCondition.Serializer getSerializer() {
    return PSTEnchantmentConditions.ARMOR.get();
  }

  public static class Serializer implements EnchantmentCondition.Serializer {
    @Override
    public EnchantmentCondition deserialize(JsonObject json) throws JsonParseException {
      return new ArmorEnchantmentCondition();
    }

    @Override
    public void serialize(JsonObject json, EnchantmentCondition condition) {
      if (!(condition instanceof ArmorEnchantmentCondition)) {
        throw new IllegalArgumentException();
      }
    }

    @Override
    public EnchantmentCondition deserialize(CompoundTag tag) {
      return new ArmorEnchantmentCondition();
    }

    @Override
    public CompoundTag serialize(EnchantmentCondition condition) {
      if (!(condition instanceof ArmorEnchantmentCondition)) {
        throw new IllegalArgumentException();
      }
      return new CompoundTag();
    }

    @Override
    public EnchantmentCondition deserialize(FriendlyByteBuf buf) {
      return new ArmorEnchantmentCondition();
    }

    @Override
    public void serialize(FriendlyByteBuf buf, EnchantmentCondition condition) {
      if (!(condition instanceof ArmorEnchantmentCondition)) {
        throw new IllegalArgumentException();
      }
    }

    @Override
    public EnchantmentCondition createDefaultInstance() {
      return new ArmorEnchantmentCondition();
    }
  }
}
