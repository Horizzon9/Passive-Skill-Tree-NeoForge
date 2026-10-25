package daripher.skilltree.item.quiver;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import daripher.skilltree.init.PSTAttributes;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class SilentQuiverItem extends QuiverItem {
  public SilentQuiverItem() {
    super(150);
  }

  @Override
  public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
      SlotContext slotContext, ResourceLocation uuid, ItemStack stack) {
    Multimap<Holder<Attribute>, AttributeModifier> modifiers = LinkedHashMultimap.create();
    modifiers.put(PSTAttributes.STEALTH,
        new AttributeModifier(uuid, 5, Operation.ADD_VALUE));
    return modifiers;
  }
}
