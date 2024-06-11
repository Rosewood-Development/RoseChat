package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.BaseRoseCommand;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

// Parent command class to avoid getting the API and locale manager repeatedly.
public class RoseChatCommand extends BaseRoseCommand {

    public RoseChatCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return null;
    }

    public RoseChatAPI getAPI() {
        return RoseChatAPI.getInstance();
    }

    public LocaleManager getLocaleManager() {
        return this.rosePlugin.getManager(LocaleManager.class);
    }

    public RosePlayer findPlayer(String name) {
        Player player = MessageUtils.getPlayerExact(name);
        if (player != null)
            return new RosePlayer(player);

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (!offlinePlayer.hasPlayedBefore())
            return null;

        return new RosePlayer(offlinePlayer);
    }

}
