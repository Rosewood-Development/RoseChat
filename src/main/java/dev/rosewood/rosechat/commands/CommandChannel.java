package dev.rosewood.rosechat.commands;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.MessageUtils;
import dev.rosewood.rosechat.chat.MessageWrapper;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.floralapi.AbstractCommand;
import dev.rosewood.rosechat.managers.ChannelManager;
import dev.rosewood.rosechat.managers.DataManager;
import dev.rosewood.rosechat.managers.LocaleManager;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class CommandChannel extends AbstractCommand {

    private RoseChat plugin;
    private LocaleManager localeManager;
    private ChannelManager channelManager;

    public CommandChannel(RoseChat plugin) {
        super(false, "channel", "c");
        this.plugin = plugin;
        this.localeManager = plugin.getManager(LocaleManager.class);
        this.channelManager = plugin.getManager(ChannelManager.class);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.localeManager.sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        DataManager dataManager = this.plugin.getManager(DataManager.class);

        ChatChannel oldChannel = dataManager.getPlayerData(((Player) sender).getUniqueId()).getCurrentChannel();
        ChatChannel channel = null;
        for (String channelStr : this.channelManager.getChannels().keySet()) {
            if (channelStr.equalsIgnoreCase(args[0])) {
                channel = this.channelManager.getChannel(channelStr);
                break;
            }
        }

        if (channel == null) {
            this.localeManager.sendMessage(sender, "command-channel-not-found");
            return;
        }

        if (args.length == 1) {
            Player player = (Player) sender;

            this.localeManager.sendMessage(sender, "command-channel-joined", StringPlaceholders.single("id", channel.getId()));
            oldChannel.remove(player);
            channel.add(player);

            PlayerData playerData = dataManager.getPlayerData(player.getUniqueId());
            playerData.setCurrentChannel(channel);
            playerData.save();
        } else {
            String message = getAllArgs(1, args);
            //  TODO: Check channel permissions & config settings.
            MessageWrapper messageWrapper = new MessageWrapper((Player) sender, message)
                    .checkAll("rosechat.chat")
                    .filterAll("rosechat.message")
                    .withReplacements()
                    .withTags()
                    .inChannel(channel)
                    .parse(channel.getFormatId(), null);

            MessageUtils.sendStandardMessage(sender, messageWrapper, channel);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) tab.addAll(this.channelManager.getChannels().keySet());

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.channel";
    }

    @Override
    public String getSyntax() {
        return localeManager.getLocaleMessage("command-channel-usage");
    }
}
