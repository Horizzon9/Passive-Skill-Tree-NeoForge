package daripher.skilltree.network.message;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.capability.skill.IPlayerSkills;
import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import daripher.skilltree.client.ClientMessageHandler;
import daripher.skilltree.skill.PassiveSkill;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncPlayerSkillsMessage(List<ResourceLocation> learnedSkills, int skillPoints)
    implements CustomPacketPayload {
  public static final Type<SyncPlayerSkillsMessage> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(SkillTreeMod.MOD_ID, "sync_player_skills"));
  public static final StreamCodec<RegistryFriendlyByteBuf, SyncPlayerSkillsMessage> STREAM_CODEC =
      StreamCodec.of(SyncPlayerSkillsMessage::encode, SyncPlayerSkillsMessage::decode);

  public SyncPlayerSkillsMessage(Player player) {
    this(learnedSkillIds(player), PlayerSkillsProvider.get(player).getSkillPoints());
  }

  private static List<ResourceLocation> learnedSkillIds(Player player) {
    IPlayerSkills skills = PlayerSkillsProvider.get(player);
    return skills.getPlayerSkills().stream().map(PassiveSkill::getId).toList();
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  private static void encode(RegistryFriendlyByteBuf buf, SyncPlayerSkillsMessage message) {
    buf.writeInt(message.learnedSkills.size());
    message.learnedSkills.forEach(id -> buf.writeUtf(id.toString()));
    buf.writeInt(message.skillPoints);
  }

  private static SyncPlayerSkillsMessage decode(RegistryFriendlyByteBuf buf) {
    int count = buf.readInt();
    List<ResourceLocation> learnedSkills = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      learnedSkills.add(ResourceLocation.parse(buf.readUtf()));
    }
    return new SyncPlayerSkillsMessage(learnedSkills, buf.readInt());
  }

  public static void handle(SyncPlayerSkillsMessage message, IPayloadContext context) {
    context.enqueueWork(() -> ClientMessageHandler.handleSyncPlayerSkills(message));
  }
}