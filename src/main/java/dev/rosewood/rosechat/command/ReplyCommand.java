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
            BungeeListener.getPlayers("ALL");
            if (!this.getAPI().getDataManager().getPlayersOnServer("ALL").contains(target)) {
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
        this.getAPI().getDataManager().getPlayerData(targetPlayer.getUniqueId(), data -> {
            if (targetPlayer != null && data != null) {
                if (!data.canBeMessaged() || (targetPlayer.isOnline() && (sender instanceof Player)) && !((Player) sender).canSee(targetPlayer.getPlayer())) {
                    this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-togglemessage-cannot-message");
                    canBeMessaged.set(false);
                    return;
                }
            }
        });

        if (!canBeMessaged.get()) return;

        RoseSender roseSender = new RoseSender(sender);
        MessageWrapper messageWrapper = new MessageWrapper(roseSender, MessageLocation.MESSAGE, null, message).validate().filter().applyDefaultColor();
        if (!messageWrapper.canBeSent()) {
            if (messageWrapper.getFilterType() != null) messageWrapper.getFilterType().sendWarning(roseSender);
            return;
        }

        MessageUtils.sendPrivateMessage(roseSender, target, messageWrapper);


        PlayerData targetData = this.getAPI().getPlayerData(targetPlayer.getUniqueId());
        if (targetData == null) return;
        if (targetData.hasMessageSounds() && targetPlayer.isOnline() && !Setting.MESSAGE_SOUND.getString().equalsIgnoreCase("none")) {
            Player tPlayer = (Player) targetPlayer;
            tPlayer.playSound(tPlayer.getLocation(), Sound.valueOf(Setting.MESSAGE_SOUND.getString()), 1.0f, 1.0f);
        }
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
