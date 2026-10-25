package daripher.skilltree.network.message;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.capability.skill.IPlayerSkills;
import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import daripher.skilltree.data.reloader.SkillsReloader;
import daripher.skilltree.network.NetworkDispatcher;
import daripher.skilltree.skill.PassiveSkill;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LearnSkillMessage(ResourceLocation skillId) implements CustomPacketPayload {
  public static final Type<LearnSkillMessage> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(SkillTreeMod.MOD_ID, "learn_skill"));
  public static final StreamCodec<ByteBuf, LearnSkillMessage> STREAM_CODEC =
      StreamCodec.composite(
          ResourceLocation.STREAM_CODEC, LearnSkillMessage::skillId, LearnSkillMessage::new);

  public LearnSkillMessage(PassiveSkill skill) {
    this(skill.getId());
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(LearnSkillMessage message, IPayloadContext context) {
    ServerPlayer player = (ServerPlayer) context.player();
    context.enqueueWork(
        () -> {
          IPlayerSkills skills = PlayerSkillsProvider.get(player);
          PassiveSkill skill = SkillsReloader.getSkillById(message.skillId());
          Objects.requireNonNull(skill);
          if (skills.learnSkill(skill)) {
            skill.learn(player, true);
          }
          NetworkDispatcher.sendToPlayer(player, new SyncPlayerSkillsMessage(player));
        });
  }
}