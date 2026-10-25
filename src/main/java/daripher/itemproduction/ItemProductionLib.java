package daripher.itemproduction;

import daripher.itemproduction.block.entity.Interactive;
import daripher.itemproduction.event.ItemProducedEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = "skilltree")
public class ItemProductionLib {
  @SubscribeEvent
  public static void setBlockEntityUser(PlayerInteractEvent.RightClickBlock event) {
    BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
    if (blockEntity instanceof Interactive interactive) {
      interactive.setUser(event.getEntity());
    }
  }

  public static ItemStack itemProduced(ItemStack stack, Player player) {
    if (stack.isEmpty()) return stack;
    ItemProducedEvent event = new ItemProducedEvent(stack, player);
    NeoForge.EVENT_BUS.post(event);
    return event.getStack();
  }

  public static ItemStack itemProduced(ItemStack stack, BlockEntity blockEntity) {
    if (!(blockEntity instanceof Interactive interactive)) return stack;
    Player user = interactive.getUser();
    return user == null ? stack : itemProduced(stack, user);
  }
}