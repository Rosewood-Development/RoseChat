package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MuteCommand extends AbstractCommand {

    public MuteCommand() {
        super(false, "mute");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0 || args.length == 2) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "player-not-found");
            return;
        }

        if (target.hasPermission("rosechat.mute.bypass")) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-mute-cannot-be-muted");
            return;
        }

        PlayerData targetData = this.getAPI().getPlayerData(target.getUniqueId());
        int outTime = 0;
        String outScale = "";
        int muteTime = -1;
        if (args.length == 1) {
            String name = targetData.getNickname() == null ? target.getDisplayName() : targetData.getNickname();
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-mute-indefinite", StringPlaceholders.single("player", name));
            this.getAPI().getLocaleManager().sendComponentMessage(target, "command-mute-muted");
            targetData.mute(-1);
            targetData.save();
            return;
        } else if (args.length == 3) {
            try {
                outTime = Integer.parseInt(args[1]);
                String scale = args[2].toLowerCase();

                switch (scale) {
                    case "seconds":
                    case "second":
                        outScale = outTime == 1 ? "second" : "seconds";
                        muteTime = outTime;
                        break;
                    case "minute":
                    case "minutes":
                        outScale = outTime == 1 ? "minute" : "minutes";
                        muteTime = outTime * 60;
                        break;
                    case "hour":
                    case "hours":
                        outScale = outTime == 1 ? "hour" : "hours";
                        muteTime = outTime * 3600;
                        break;
                    case "day":
                    case "days":
                        outScale = outTime == 1 ? "day" : "days";
                        muteTime = outTime * (3600 * 24);
                        break;
                    case "month":
                    case "months":
                        outScale = outTime == 1 ? "month" : "months";
                        muteTime = outTime * 2629800;
                        break;
                    case "year":
                    case "years":
                        if (outTime > 1000) {
                            outScale = "indefinite";
                        } else {
                            outScale = outTime == 1 ? "year" : "years";
                            muteTime = (outTime * 364) * 86400;
                        }
                        break;
                    default:
                        this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
                        return;
                }
            } catch (NumberFormatException e) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "not-a-number");
                return;
            }
        }

        String name = targetData.getNickname() == null ? target.getDisplayName() : targetData.getNickname();

        if (outScale.equalsIgnoreCase("indefinite")) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-mute-indefinite",
                    StringPlaceholders.builder("player", name).build());
        } else {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-mute-success",
                    StringPlaceholders.builder("player", name)
                            .addPlaceholder("time", outTime)
                            .addPlaceholder("scale", this.getAPI().getLocaleManager().getLocaleMessage("command-mute-" + outScale)).build());
        }

        this.getAPI().getLocaleManager().sendComponentMessage(target, "command-mute-muted");

        if (muteTime > 0) targetData.mute((muteTime * 1000L) + System.currentTimeMillis());

        targetData.save();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player != sender) tab.add(player.getName());
            }
        } else if (args.length == 2) {
            tab.add("<time>");
        } else if (args.length == 3) {
            tab.addAll(Arrays.asList("seconds", "minutes", "hours", "days", "months", "years"));
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.mute";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-mute-usage");
    }

}
