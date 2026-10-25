package daripher.skilltree.recipe;

import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;

public class RecipePlayerHolder {
  private static final ThreadLocal<Player> PLAYER = new ThreadLocal<>();

  public static void set(@Nullable Player player) {
    if (player == null) {
      PLAYER.remove();
    } else {
      PLAYER.set(player);
    }
  }

  public static void clear() {
    PLAYER.remove();
  }

  public static @Nullable Player get() {
    return PLAYER.get();
  }
}