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

public class GildedQuiverItem extends QuiverItem {
  public GildedQuiverItem() {
    super(150);
  }

  @Override
  public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
      SlotContext slotContext, ResourceLocation uuid, ItemStack stack) {
    Multimap<Holder<Attribute>, AttributeModifier> modifiers = LinkedHashMultimap.create();
    modifiers.put(
        Attributes.MOVEMENT_SPEED,
        new AttributeModifier(uuid, 0.05, Operation.ADD_MULTIPLIED_BASE));
    return modifiers;
  }

  @Override
  public boolean makesPiglinsNeutral(SlotContext slotContext, ItemStack stack) {
    return true;
  }
}
