package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeleteMessageCommand extends AbstractCommand {

    public DeleteMessageCommand() {
        super(true, "delmsg");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        try {
            UUID uuid = UUID.fromString(args[0]);
            DeletableMessage message = null;

            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerData playerData = this.getAPI().getPlayerData(player.getUniqueId());
                List<DeletableMessage> localMessagesToDelete = new ArrayList<>();
                for (DeletableMessage deletableMessage : playerData.getMessageLog().getDeletableMessages()) {
                    if (!deletableMessage.getUUID().equals(uuid)) continue;
                    if (ConfigurationManager.Setting.DELETED_MESSAGE_FORMAT.getString().equalsIgnoreCase("none")) {
                        localMessagesToDelete.add(deletableMessage);
                        continue;
                    }

                    deletableMessage.setJson(ComponentSerializer.toString(TextComponent.fromLegacyText(HexUtils.colorify("&7&oDeleted Message"))));
                    message = deletableMessage;
                }

                if (message == null || (message.isClient() && !message.getUUID().equals(uuid))) continue;
                for (DeletableMessage deletableMessage : localMessagesToDelete) playerData.getMessageLog().getDeletableMessages().remove(deletableMessage);

                for (int i = 0; i < 100; i++) player.sendMessage("\n");
                for (DeletableMessage deletableMessage : playerData.getMessageLog().getDeletableMessages()) {
                    player.spigot().sendMessage(ComponentSerializer.parse(deletableMessage.getJson()));
                }
            }
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getSyntax() {
        return null;
    }
}
