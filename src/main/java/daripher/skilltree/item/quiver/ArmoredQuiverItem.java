package daripher.skilltree.item.quiver;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class ArmoredQuiverItem extends QuiverItem {
  public ArmoredQuiverItem() {
    super(150);
  }

  @Override
  public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
      SlotContext slotContext, ResourceLocation uuid, ItemStack stack) {
    Multimap<Holder<Attribute>, AttributeModifier> modifiers = LinkedHashMultimap.create();
    modifiers.put(Attributes.ARMOR, new AttributeModifier(uuid, 2, Operation.ADD_VALUE));
    return modifiers;
  }
}
