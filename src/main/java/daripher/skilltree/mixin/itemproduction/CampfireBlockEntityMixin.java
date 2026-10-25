package daripher.skilltree.mixin.itemproduction;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import daripher.itemproduction.ItemProductionLib;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin {
  @Inject(
      method = "cookTick",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V",
              shift = At.Shift.BEFORE))
  private static void itemProduced(
      Level level,
      BlockPos pos,
      BlockState state,
      CampfireBlockEntity entity,
      CallbackInfo callbackInfo,
      @Local(ordinal = 1) LocalRef<ItemStack> stack) {
    stack.set(ItemProductionLib.itemProduced(stack.get(), entity));
  }
}
