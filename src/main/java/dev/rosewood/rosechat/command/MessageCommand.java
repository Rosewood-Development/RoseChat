package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageCommand extends AbstractCommand {

    public MessageCommand() {
        super(false, "message", "msg", "m", "pm", "whisper", "w", "tell", "t");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.of("syntax", getSyntax()));
            return;
        }

        if (args.length == 1) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-message-enter-message");
            return;
        }

        String target = args[0];

        if (!target.equalsIgnoreCase("Console") && Bukkit.getPlayer(args[0]) == null && Setting.ALLOW_BUNGEECORD_MESSAGES.getBoolean()
                && this.getAPI().isBungee()) {
            this.getAPI().getBungeeManager().getPlayers("ALL");
            if (!this.getAPI().getBungeeManager().getBungeePlayers().get("ALL").contains(target)) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "player-not-found");
                return;
            }
        } else {
            if (!target.equalsIgnoreCase("Console") && Bukkit.getPlayer(target) == null) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "player-not-found");
                return;
            }
        }

        String message = getAllArgs(1, args);

        if (message.isEmpty()) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-message-enter-message");
            return;
        }

        if (MessageUtils.isMessageEmpty(message)) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "message-blank");
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(target);
        AtomicBoolean canBeMessaged = new AtomicBoolean(true);
        if (targetPlayer != null) {
            this.getAPI().getPlayerDataManager().getPlayerData(targetPlayer.getUniqueId(), data -> {
                if (data != null) {
                    if ((!sender.hasPermission("rosechat.togglemessage.bypass"))
                            && (!data.canBeMessaged() || ((sender instanceof Player) && !((Player) sender).canSee(targetPlayer)))) {
                        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-togglemessage-cannot-message");
                        canBeMessaged.set(false);
                    }
                }
            });
        }

        if (!canBeMessaged.get()) return;

        RosePlayer rosePlayer = new RosePlayer(sender);
        MessageUtils.sendPrivateMessage(rosePlayer, target, message);

        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerData playerData = this.getAPI().getPlayerData(player.getUniqueId());
            playerData.setReplyTo(target);
            playerData.save();
        }

        if (target.equalsIgnoreCase("Console")) return;

        if (this.getAPI().isBungee()) {
            this.getAPI().getBungeeManager().sendUpdateReply(sender.getName(), target);
        }

        if (targetPlayer == null || !targetPlayer.isOnline()) return;
        Player player = targetPlayer.getPlayer();

        PlayerData targetData = this.getAPI().getPlayerData(player.getUniqueId());
        if (targetData == null) return;

        targetData.setReplyTo(sender.getName());
        targetData.save();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (MessageUtils.isPlayerVanished(player))
                    continue;

                if (player != sender) tab.add(player.getName());
            }

            if (RoseChatAPI.getInstance().isBungee() && Setting.ALLOW_BUNGEECORD_MESSAGES.getBoolean()) {
                if (this.getAPI().getBungeeManager().getBungeePlayers().containsKey("ALL")) {
                    Collection<String> players = this.getAPI().getBungeeManager().getBungeePlayers().get("ALL");
                    for (String player : players) {
                        if (sender instanceof Player && sender.getName().equalsIgnoreCase(player)) continue;
                        tab.add(player);
                    }
                }
            }
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.message";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-message-usage");
    }

}
