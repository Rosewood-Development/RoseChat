package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SocialSpyCommand extends AbstractCommand {

    private final List<String> arguments;

    public SocialSpyCommand() {
        super(true, "socialspy", "ss", "spy");
        this.arguments = Arrays.asList("message", "channel", "group", "all");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0 || !this.arguments.contains(args[0])) {
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        UUID uuid = ((Player) sender).getUniqueId();
        PlayerData playerData = this.getAPI().getPlayerData(uuid);

        switch (args[0].toLowerCase()) {
            case "message":
                playerData.setMessageSpy(!playerData.hasMessageSpy());
                sendToggleMessage(sender, args, playerData.hasMessageSpy());
                break;
            case "channel":
                playerData.setChannelSpy(!playerData.hasChannelSpy());
                sendToggleMessage(sender, args, playerData.hasChannelSpy());
                break;
            case "group":
                playerData.setGroupSpy(!playerData.hasGroupSpy());
                sendToggleMessage(sender, args, playerData.hasGroupSpy());
                break;
            case "all":
                playerData.setMessageSpy(!playerData.hasMessageSpy());
                playerData.setChannelSpy(!playerData.hasChannelSpy());
                playerData.setGroupSpy(!playerData.hasGroupSpy());
                sendToggleMessage(sender, args, playerData.hasMessageSpy() || playerData.hasChannelSpy() || playerData.hasGroupSpy());
                break;
        }

        playerData.save();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("rosechat.spy.message")) tab.add("message");
            if (sender.hasPermission("rosechat.spy.channel")) tab.add("channel");
            if (sender.hasPermission("rosechat.spy.group")) tab.add("group");
            if (tab.size() == 3) tab.add("all");
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.spy";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-socialspy-usage");
    }

    private void sendToggleMessage(CommandSender sender, String[] args, boolean nowOn) {
        if (nowOn)
            this.getAPI().getLocaleManager().sendMessage(sender, "command-socialspy-enabled", StringPlaceholders.single("type",
                    this.getAPI().getLocaleManager().getLocaleMessage("command-socialspy-" + args[0].toLowerCase())));
        else
            this.getAPI().getLocaleManager().sendMessage(sender, "command-socialspy-disabled", StringPlaceholders.single("type",
                    this.getAPI().getLocaleManager().getLocaleMessage("command-socialspy-" + args[0].toLowerCase())));
    }
}
