package daripher.skilltree.mixin.itemproduction;

import daripher.itemproduction.ItemProductionLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin {
  @Inject(method = "doBrew", at = @At("TAIL"))
  private static void itemProduced(
      Level level, BlockPos blockPos, NonNullList<ItemStack> itemStacks, CallbackInfo callbackInfo) {
    BlockEntity blockEntity = level.getBlockEntity(blockPos);
    for (int slot = 0; slot < 3; slot++) {
      itemStacks.set(slot, ItemProductionLib.itemProduced(itemStacks.get(slot), blockEntity));
    }
  }
}
