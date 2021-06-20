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

public class ToggleSoundCommand extends AbstractCommand {

    private final List<String> arguments;

    public ToggleSoundCommand() {
        super(true, "togglesound", "togglesounds", "toggleping", "toggletag");
        this.arguments = Arrays.asList("messages", "tags", "all");
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
            case "messages":
                playerData.setMessageSounds(!playerData.hasMessageSounds());
                sendToggleMessage(sender, args, playerData.hasMessageSounds());
                break;
            case "tags":
                playerData.setTagSounds(!playerData.hasTagSounds());
                sendToggleMessage(sender, args, playerData.hasTagSounds());
                break;
            case "all":
                playerData.setMessageSounds(!playerData.hasMessageSounds());
                playerData.setTagSounds(!playerData.hasTagSounds());
                sendToggleMessage(sender, args, playerData.hasTagSounds() || playerData.hasMessageSounds());
                break;
        }

        playerData.save();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) tab.addAll(this.arguments);

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.togglesound";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-togglesound-usage");
    }

    private void sendToggleMessage(CommandSender sender, String[] args, boolean nowOn) {
        if (nowOn)
            this.getAPI().getLocaleManager().sendMessage(sender, "command-togglesound-on", StringPlaceholders.single("type",
                    this.getAPI().getLocaleManager().getLocaleMessage("command-toggle-sound-" + args[0].toLowerCase())));
        else
            this.getAPI().getLocaleManager().sendMessage(sender, "command-togglesound-off", StringPlaceholders.single("type",
                    this.getAPI().getLocaleManager().getLocaleMessage("command-toggle-sound-" + args[0].toLowerCase())));
    }
}
