package dev.rosewood.rosechat.commands.chat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.floralapi.AbstractCommand;
import dev.rosewood.rosechat.managers.ChannelManager;
import dev.rosewood.rosechat.managers.DataManager;
import dev.rosewood.rosechat.managers.LocaleManager;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandChatMove extends AbstractCommand {

    private RoseChat plugin;
    private LocaleManager localeManager;
    private DataManager dataManager;
    private ChannelManager channelManager;

    public CommandChatMove(RoseChat plugin) {
        super(true, "move");
        this.plugin = plugin;
        this.localeManager = plugin.getManager(LocaleManager.class);
        this.channelManager = plugin.getManager(ChannelManager.class);
        this.dataManager = plugin.getManager(DataManager.class);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            this.localeManager.sendMessage(sender, "command-chat-move-usage");
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        String channelStr = args[1];

        if (player == null) {
            this.localeManager.sendMessage(sender, "player-not-found");
            return;
        }

        if (this.channelManager.getChannel(channelStr) == null) {
            this.localeManager.sendMessage(sender, "command-channel-not-found");
            return;
        }

        ChatChannel oldChannel = this.dataManager.getPlayerData(player.getUniqueId()).getCurrentChannel();
        ChatChannel channel = this.channelManager.getChannel(channelStr);

        oldChannel.remove(player);
        channel.add(player);

        this.localeManager.sendMessage(sender, "command-chat-move-success",
                StringPlaceholders.builder("player", player.getName())
                        .addPlaceholder("channel", channel.getId()).build());
        this.localeManager.sendMessage(player, "command-chat-move-moved", StringPlaceholders.single("channel", channel.getId()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                tab.add(player.getName());
            }
        } else if (args.length == 2) {
            tab.addAll(this.plugin.getManager(ChannelManager.class).getChannels().keySet());
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.admin.move";
    }

    @Override
    public String getSyntax() {
        return this.localeManager.getLocaleMessage("command-chat-move-usage");
    }
}
