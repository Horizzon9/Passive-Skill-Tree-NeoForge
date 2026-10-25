package daripher.itemproduction.event;

import javax.annotation.Nonnull;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class ItemProducedEvent extends Event {
  private final @Nonnull ItemStack stack;
  private final @Nonnull Player player;

  public ItemProducedEvent(@Nonnull ItemStack stack, @Nonnull Player player) {
    this.stack = stack.copy();
    this.player = player;
  }

  public @Nonnull ItemStack getStack() {
    return stack;
  }

  @Nonnull
  public Player getPlayer() {
    return player;
  }
}