package daripher.skilltree.client;

import daripher.skilltree.capability.skill.IPlayerSkills;
import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import daripher.skilltree.client.screen.SkillTreeScreen;
import daripher.skilltree.data.reloader.SkillsReloader;
import daripher.skilltree.network.message.SyncPlayerSkillsMessage;
import daripher.skilltree.skill.PassiveSkill;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientMessageHandler {
  public static void handleSyncPlayerSkills(SyncPlayerSkillsMessage message) {
    Minecraft minecraft = Minecraft.getInstance();
    assert minecraft.player != null;
    IPlayerSkills skills = PlayerSkillsProvider.get(minecraft.player);
    skills.getPlayerSkills().clear();
    message.learnedSkills().stream()
        .map(SkillsReloader::getSkillById)
        .filter(Objects::nonNull)
        .forEach(skills.getPlayerSkills()::add);
    skills.setSkillPoints(message.skillPoints());
    if (minecraft.screen instanceof SkillTreeScreen screen) {
      screen.skillPoints = skills.getSkillPoints() - screen.newlyLearnedSkills.size();
      screen.init();
    }
  }

  public static void handleSyncServerData() {}
}