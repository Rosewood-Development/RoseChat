package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.MuteTask;
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
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            this.getAPI().getLocaleManager().sendMessage(sender, "player-not-found");
            return;
        }

        if (target.hasPermission("rosechat.mute.bypass")) {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-mute-cannot-be-muted");
            return;
        }

        PlayerData targetData = this.getAPI().getPlayerData(target.getUniqueId());
        int outTime = 0;
        String outScale = "";
        int muteTime = -1;
        if (args.length == 1)
        {
            this.getAPI().getLocaleManager().sendMessage(sender, "command-mute-indefinite", StringPlaceholders.single("player", target.getName()));
            this.getAPI().getLocaleManager().sendMessage(target, "command-mute-muted");
            targetData.setMuteTime(-1);
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
                        this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
                        return;
                }
            } catch (NumberFormatException e) {
                this.getAPI().getLocaleManager().sendMessage(sender, "not-a-number");
                return;
            }
        }

        this.getAPI().getLocaleManager().sendMessage(sender, "command-mute-success",
                StringPlaceholders.builder("player", target.getDisplayName())
                        .addPlaceholder("time", outTime)
                        .addPlaceholder("scale", this.getAPI().getLocaleManager().getLocaleMessage("command-mute-" + outScale)).build());

        this.getAPI().getLocaleManager().sendMessage(target, "command-mute-muted");

        if (muteTime > 0) {
            targetData.setMuteTime((muteTime * 1000L) + System.currentTimeMillis());
            this.getAPI().getDataManager().getMuteTasks().put(target.getUniqueId(), new MuteTask(targetData));
        }

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
