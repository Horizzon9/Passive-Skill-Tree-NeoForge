package daripher.skilltree.network.message;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.client.ClientMessageHandler;
import daripher.skilltree.data.reloader.GemTypesReloader;
import daripher.skilltree.data.reloader.SkillTreesReloader;
import daripher.skilltree.data.reloader.SkillsReloader;
import daripher.skilltree.network.NetworkHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncServerDataMessage() implements CustomPacketPayload {
  public static final Type<SyncServerDataMessage> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(SkillTreeMod.MOD_ID, "sync_server_data"));
  public static final StreamCodec<RegistryFriendlyByteBuf, SyncServerDataMessage> STREAM_CODEC =
      StreamCodec.of(SyncServerDataMessage::encode, SyncServerDataMessage::decode);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  private static void encode(RegistryFriendlyByteBuf buf, SyncServerDataMessage message) {
    NetworkHelper.writeSkillTreeConfig(buf);
    NetworkHelper.writePassiveSkills(buf, SkillsReloader.getSkills().values());
    NetworkHelper.writePassiveSkillTrees(buf, SkillTreesReloader.getSkillTrees().values());
    NetworkHelper.writeGemTypes(buf, GemTypesReloader.getGemTypes().values());
  }

  private static SyncServerDataMessage decode(RegistryFriendlyByteBuf buf) {
    NetworkHelper.loadSkillTreeConfig(buf);
    SkillsReloader.loadFromByteBuf(buf);
    SkillTreesReloader.loadFromByteBuf(buf);
    GemTypesReloader.loadFromByteBuf(buf);
    return new SyncServerDataMessage();
  }

  public static void handle(SyncServerDataMessage message, IPayloadContext context) {
    context.enqueueWork(ClientMessageHandler::handleSyncServerData);
  }
}