package daripher.skilltree.mixin.itemproduction;

import daripher.itemproduction.block.entity.Interactive;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockEntity.class)
public class BlockEntityMixin implements Interactive {
  @Unique private @Nullable Player skilltree$user;

  @Override
  public @Nullable Player getUser() {
    return skilltree$user;
  }

  @Override
  public void setUser(@Nullable Player player) {
    this.skilltree$user = player;
  }
}
