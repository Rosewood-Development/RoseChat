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
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class CommandChatClear extends AbstractCommand {

    private RoseChat plugin;
    private LocaleManager localeManager;
    private DataManager dataManager;
    private ChannelManager channelManager;

    public CommandChatClear(RoseChat plugin) {
        super(true, "clear");
        this.plugin = plugin;
        this.localeManager = plugin.getManager(LocaleManager.class);
        this.channelManager = plugin.getManager(ChannelManager.class);
        this.dataManager = plugin.getManager(DataManager.class);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        ChatChannel channel = null;

        if (args.length == 0) {
            Player player = (Player) sender;
            channel = this.dataManager.getPlayerData(player.getUniqueId()).getCurrentChannel();
        } else if (args.length == 1) {
            for (String channelStr : this.channelManager.getChannels().keySet()) {
                if (channelStr.equalsIgnoreCase(args[0])) {
                    channel = this.channelManager.getChannel(channelStr);
                    break;
                }
            }
        }

        if (channel == null) {
            this.localeManager.sendMessage(sender, "command-channel-not-found");
            return;
        }

        if (channel.isVisible()) {
            this.localeManager.sendMessage(sender, "command-chat-clear-visible");
            return;
        }

        channel.message((Player) sender, StringUtils.repeat(" \n", 100), false);
        this.localeManager.sendMessage(sender, "command-chat-clear-cleared", StringPlaceholders.single("channel", channel.getId()));
        this.localeManager.sendMessage(Bukkit.getConsoleSender(), "command-chat-clear-cleared", StringPlaceholders.single("channel", channel.getId()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) tab.addAll(this.channelManager.getChannels().keySet());

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.admin.clear";
    }

    @Override
    public String getSyntax() {
        return this.localeManager.getLocaleMessage("command-chat-clear-usage");
    }
}
