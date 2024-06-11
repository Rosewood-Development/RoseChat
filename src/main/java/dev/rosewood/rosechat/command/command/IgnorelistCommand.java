package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;

public class IgnorelistCommand extends RoseChatCommand {

    public IgnorelistCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("ignorelist")
                .permission("rosechat.ignore")
                .descriptionKey("command-ignorelist-description")
                .playerOnly(true)
                .build();
    }

    @Override
    protected boolean hasPriority() {
        return true;
    }

    @RoseExecutable
    public void execute(CommandContext context) {
        RosePlayer player = new RosePlayer(context.getSender());
        PlayerData data = player.getPlayerData();

        List<UUID> ignoring = data.getIgnoringPlayers();
        player.sendLocaleMessage("command-ignorelist-title",
                StringPlaceholders.of("amount", ignoring.size()));

        if (ignoring.isEmpty())
            return;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.getLocaleManager().getLocaleMessage("command-ignorelist-color"));

        boolean first = true;
        for (UUID uuid : ignoring) {
            Player ignoredPlayer = Bukkit.getPlayer(uuid);
            String name;
            if (ignoredPlayer != null) {
                RosePlayer roseIgnoredPlayer = new RosePlayer(ignoredPlayer);
                name = roseIgnoredPlayer.getName();
            } else {
                name = Bukkit.getOfflinePlayer(uuid).getName();
            }

            if (first) {
                stringBuilder.append(name);
                first = false;
            } else {
                stringBuilder.append(this.getLocaleManager().getMessage("command-ignorelist-separator"))
                        .append(name);
            }
        }

        player.send(HexUtils.colorify(stringBuilder.toString()));
    }

}
