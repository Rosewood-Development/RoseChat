package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.MessageUtils;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.floralapi.AbstractCommand;
import dev.rosewood.rosechat.managers.DataManager;
import dev.rosewood.rosechat.managers.LocaleManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;

public class CommandReply extends AbstractCommand {

    private RoseChat plugin;
    private DataManager dataManager;
    private LocaleManager localeManager;

    public CommandReply(RoseChat plugin) {
        super(true, "reply", "r");
        this.plugin = plugin;
        this.dataManager = plugin.getManager(DataManager.class);
        this.localeManager = plugin.getManager(LocaleManager.class);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // TODO: Maybe allow the console to reply?

        if (!(sender instanceof Player)) {
            this.localeManager.sendMessage(sender, "player-only");
            return;
        }

        if (args.length == 0) {
            this.localeManager.sendMessage(sender, "command-reply-enter-message");
            return;
        }

        Player player = (Player) sender;
        PlayerData playerData = this.dataManager.getPlayerData(player.getUniqueId());

        if (playerData.getReplyTo() == null) {
            this.localeManager.sendMessage(sender, "command-reply-no-one");
            return;
        }

        Player target = Bukkit.getPlayer(playerData.getReplyTo());
        if (target == null) {
            this.localeManager.sendMessage(sender, "command-reply-no-one");
            return;
        }

        PlayerData targetData = this.dataManager.getPlayerData(target.getUniqueId());
        String message = getAllArgs(0, args);

        if (message.isEmpty()) {
            this.localeManager.sendMessage(sender, "command-reply-enter-message");
            return;
        }

        if (!targetData.canBeMessaged()) {
            this.localeManager.sendMessage(sender, "command-togglemessage-cannot-message");
            return;
        }

        MessageUtils.sendPrivateMessage(this.dataManager, playerData, targetData, message);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return "rosechat.reply";
    }

    @Override
    public String getSyntax() {
        return localeManager.getLocaleMessage("command-reply-usage");
    }
}
