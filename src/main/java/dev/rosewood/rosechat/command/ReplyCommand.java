package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.listener.BungeeListener;
import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReplyCommand extends AbstractCommand {

    public ReplyCommand() {
        super(true, "reply", "r");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-reply-enter-message");
            return;
        }

        Player player = (Player) sender;
        PlayerData playerData = this.getAPI().getPlayerData(player.getUniqueId());
        String target = playerData.getReplyTo();

        if (target == null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-reply-no-one");
            return;
        }

        if (!target.equalsIgnoreCase("Console") && ConfigurationManager.Setting.ALLOW_BUNGEECORD_MESSAGES.getBoolean() && this.getAPI().isBungee()) {
            if (!this.getAPI().getPlayerDataManager().getPlayersOnServer("ALL").contains(target)) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "player-not-found");
                return;
            }
        } else {
            if (!target.equalsIgnoreCase("Console") && Bukkit.getPlayer(target) == null) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "player-not-found");
                return;
            }
        }

        String message = getAllArgs(0, args);
        if (message.isEmpty()) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-reply-enter-message");
            return;
        }

        if (MessageUtils.isMessageEmpty(message)) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "message-blank");
            return;
        }

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(target);
        AtomicBoolean canBeMessaged = new AtomicBoolean(true);
        this.getAPI().getPlayerDataManager().getPlayerData(targetPlayer.getUniqueId(), data -> {
            if (data != null) {
                if ((!sender.hasPermission("rosechat.togglemessage.bypass")) && (!data.canBeMessaged())) {
                    this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-togglemessage-cannot-message");
                    canBeMessaged.set(false);
                }
            }
        });

        if (!canBeMessaged.get()) return;

        RoseSender roseSender = new RoseSender(sender);
        MessageUtils.sendPrivateMessage(roseSender, target, message);

        PlayerData targetData = this.getAPI().getPlayerData(targetPlayer.getUniqueId());
        if (targetData == null) return;
        if (targetData.hasMessageSounds() && targetPlayer.isOnline() && !Setting.MESSAGE_SOUND.getString().equalsIgnoreCase("none")) {
            Player tPlayer = (Player) targetPlayer;
            tPlayer.playSound(tPlayer.getLocation(), Sound.valueOf(Setting.MESSAGE_SOUND.getString()), 1.0f, 1.0f);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        this.getAPI().getBungeeManager().getPlayers("ALL");
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
