package daripher.skilltree.network;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.network.message.GainSkillPointMessage;
import daripher.skilltree.network.message.LearnSkillMessage;
import daripher.skilltree.network.message.SyncPlayerSkillsMessage;
import daripher.skilltree.network.message.SyncServerDataMessage;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SkillTreeMod.MOD_ID)
public class NetworkDispatcher {
  public static final String PROTOCOL_VERSION = "1";

  @SubscribeEvent
  public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
    PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
    registrar.playToClient(
        SyncServerDataMessage.TYPE, SyncServerDataMessage.STREAM_CODEC, SyncServerDataMessage::handle);
    registrar.playToClient(
        SyncPlayerSkillsMessage.TYPE, SyncPlayerSkillsMessage.STREAM_CODEC, SyncPlayerSkillsMessage::handle);
    registrar.playToServer(
        LearnSkillMessage.TYPE, LearnSkillMessage.STREAM_CODEC, LearnSkillMessage::handle);
    registrar.playToServer(
        GainSkillPointMessage.TYPE, GainSkillPointMessage.STREAM_CODEC, GainSkillPointMessage::handle);
  }

  public static void sendToPlayer(ServerPlayer player, net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
    PacketDistributor.sendToPlayer(player, payload);
  }

  public static void sendToServer(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
    PacketDistributor.sendToServer(payload);
  }
}