package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;

public class ReplyCommand extends AbstractCommand {

    public ReplyCommand() {
        super(true, "reply", "r");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // TODO: Maybe allow the console to reply?

        if (!(sender instanceof Player)) {
            this.getAPI().getLocaleManager().sendMessage(sender, "player-only");
            return;
        }

        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-reply-enter-message");
            return;
        }

        Player player = (Player) sender;
        PlayerData playerData = this.getAPI().getPlayerData(player.getUniqueId());

        if (playerData.getReplyTo() == null) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-reply-no-one");
            return;
        }

        Player target = Bukkit.getPlayer(playerData.getReplyTo());
        if (target == null) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-reply-no-one");
            return;
        }

        PlayerData targetData = this.getAPI().getPlayerData(target.getUniqueId());
        String message = getAllArgs(0, args);

        if (message.isEmpty()) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-reply-enter-message");
            return;
        }

        if (!targetData.canBeMessaged()) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-togglemessage-cannot-message");
            return;
        }

        //MessageUtils.sendPrivateMessage(this.dataManager, playerData, targetData, message);
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
        return this.getAPI().getLocaleManager().getLocaleMessage("command-reply-usage");
    }
}
