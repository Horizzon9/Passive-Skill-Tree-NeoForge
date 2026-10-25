package daripher.itemproduction.block.entity;

import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;

public interface Interactive {
  @Nullable
  Player getUser();

  void setUser(@Nullable Player player);
}