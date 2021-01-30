package dev.rosewood.rosechat.commands.chat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.MessageSender;
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

public class CommandChatSudo extends AbstractCommand {

    private RoseChat plugin;
    private LocaleManager localeManager;
    private DataManager dataManager;
    private ChannelManager channelManager;

    public CommandChatSudo(RoseChat plugin) {
        super(true, "sudo");
        this.plugin = plugin;
        this.localeManager = plugin.getManager(LocaleManager.class);
        this.channelManager = plugin.getManager(ChannelManager.class);
        this.dataManager = plugin.getManager(DataManager.class);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length != 3) {
            this.localeManager.sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", this.getSyntax()));
            return;
        }

        String player = args[0];
        ChatChannel channel = this.channelManager.getChannel(args[1]);

        MessageSender sudoSender = new MessageSender(player, "default");

        //TODO: Sudo
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) tab.add(player.getName());
        } else if (args.length == 2) {
            tab.addAll(this.channelManager.getChannels().keySet());
        }

        ///chat sudo <player> <channel> <message>

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.admin.sudo";
    }

    @Override
    public String getSyntax() {
        return this.localeManager.getLocaleMessage("command-chat-sudo-usage");
    }
}
