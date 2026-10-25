package daripher.skilltree.network.message;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.capability.skill.IPlayerSkills;
import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import daripher.skilltree.config.Config;
import daripher.skilltree.network.NetworkDispatcher;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GainSkillPointMessage() implements CustomPacketPayload {
  public static final Type<GainSkillPointMessage> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(SkillTreeMod.MOD_ID, "gain_skill_point"));
  public static final StreamCodec<ByteBuf, GainSkillPointMessage> STREAM_CODEC =
      StreamCodec.unit(new GainSkillPointMessage());

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(GainSkillPointMessage message, IPayloadContext context) {
    ServerPlayer player = (ServerPlayer) context.player();
    context.enqueueWork(
        () -> {
          IPlayerSkills skills = PlayerSkillsProvider.get(player);
          int learnedSkills = skills.getPlayerSkills().size();
          int points = skills.getSkillPoints();
          int level = learnedSkills + points;
          if (level >= Config.max_skill_points) return;
          int cost = Config.getSkillPointCost(level);
          if (player.totalExperience < cost) return;
          player.giveExperiencePoints(-cost);
          skills.grantSkillPoints(1);
          NetworkDispatcher.sendToPlayer(player, new SyncPlayerSkillsMessage(player));
        });
  }
}