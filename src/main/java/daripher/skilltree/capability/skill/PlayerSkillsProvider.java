package daripher.skilltree.capability.skill;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.init.PSTAttachments;
import daripher.skilltree.network.NetworkDispatcher;
import daripher.skilltree.network.message.SyncPlayerSkillsMessage;
import daripher.skilltree.network.message.SyncServerDataMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;

@EventBusSubscriber(modid = SkillTreeMod.MOD_ID)
public class PlayerSkillsProvider {
  @SubscribeEvent(priority = EventPriority.LOWEST)
  public static void restoreSkillsAttributeModifiers(EntityJoinLevelEvent event) {
    if (!(event.getEntity() instanceof ServerPlayer player)) return;
    get(player).getPlayerSkills().forEach(skill -> skill.learn(player, false));
  }

  @SubscribeEvent
  public static void syncSkills(PlayerLoggedInEvent event) {
    if (event.getEntity().level().isClientSide) return;
    NetworkDispatcher.sendToPlayer((ServerPlayer) event.getEntity(), new SyncServerDataMessage());
  }

  @SubscribeEvent
  public static void sendTreeResetMessage(EntityJoinLevelEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (event.getEntity().level().isClientSide) return;
    IPlayerSkills skills = get(player);
    if (skills.isTreeReset()) {
      player.sendSystemMessage(
          Component.translatable("skilltree.message.reset").withStyle(ChatFormatting.YELLOW));
      skills.setTreeReset(false);
    }
  }

  @SubscribeEvent
  public static void syncPlayerSkills(EntityJoinLevelEvent event) {
    if (!(event.getEntity() instanceof ServerPlayer player)) return;
    NetworkDispatcher.sendToPlayer(player, new SyncPlayerSkillsMessage(player));
  }

  public static IPlayerSkills get(Player player) {
    return player.getData(PSTAttachments.PLAYER_SKILLS);
  }

  public static boolean hasSkills(Player player) {
    return player.hasData(PSTAttachments.PLAYER_SKILLS.get());
  }
}