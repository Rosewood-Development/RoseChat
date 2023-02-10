package dev.rosewood.rosechat.command.chat;

import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.List;

public class ChatInfoCommand extends AbstractCommand {

    public ChatInfoCommand() {
        super(false, "info");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", this.getSyntax()));
            return;
        }

        /*ChatChannel channel = this.getAPI().getChannelById(args[0]);
        if (channel == null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-channel-not-found");
            return;
        }

        String localeTrue = this.getAPI().getLocaleManager().getLocaleMessage("command-chat-info-true");
        String localeFalse = this.getAPI().getLocaleManager().getLocaleMessage("command-chat-info-false");
        String localeNone = this.getAPI().getLocaleManager().getLocaleMessage("command-chat-info-none");

        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-chat-info-title", StringPlaceholders.single("id", channel.getId()), false);
        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-chat-info-format", StringPlaceholders.builder()
                .addPlaceholder("default", channel.isDefaultChannel() ? localeTrue : localeFalse)
                .addPlaceholder("muted", channel.isMuted() ? localeTrue : localeFalse)
                .addPlaceholder("command", channel.getCommand() == null ? localeNone : "/" + channel.getCommand())
                .addPlaceholder("world", channel.getWorld() == null ? localeNone : channel.getWorld())
                .addPlaceholder("joinable", channel.isJoinable() ? localeTrue : localeFalse)
                .addPlaceholder("discord", channel.getDiscordChannel() == null ? localeNone : channel.getDiscordChannel())
                .addPlaceholder("players", channel.getMembers().size())
                .addPlaceholder("servers", channel.getServers().isEmpty() ? localeNone: channel.getServers().toString()).build(), false);*/
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            tab.addAll(this.getAPI().getChannelIDs());
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.admin.info";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-chat-info-usage");
    }

}
