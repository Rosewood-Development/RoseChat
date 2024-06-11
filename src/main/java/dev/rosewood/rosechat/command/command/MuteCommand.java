package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;

public class MuteCommand extends RoseChatCommand {

    public MuteCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("mute")
                .descriptionKey("command-mute-description")
                .permission("rosechat.mute")
                .arguments(ArgumentsDefinition.builder()
                        .required("player", RoseChatArgumentHandlers.ROSE_PLAYER)
                        .optional("duration", RoseChatArgumentHandlers.MUTE_DURATION)
                        .build())
                .build();
    }

    @Override
    protected boolean hasPriority() {
        return true;
    }

    @RoseExecutable
    public void execute(CommandContext context, RosePlayer target) {
        RosePlayer player = new RosePlayer(context.getSender());

        if (target.isMuted()) {
            target.unmute();

            player.sendLocaleMessage("command-unmute-success",
                    StringPlaceholders.of("player", target.getName()));
            target.sendLocaleMessage("command-mute-unmuted");
            return;
        }

        target.mute();

        player.sendLocaleMessage("command-mute-indefinite",
                StringPlaceholders.of("player", target.getName()));
        target.sendLocaleMessage("command-mute-muted");
    }

    @RoseExecutable
    public void execute(CommandContext context, RosePlayer target, Integer time) {
        RosePlayer player = new RosePlayer(context.getSender());

        String timescale = context.getRawArguments()[context.getRawArguments().length - 1];
        String displayTimescale = "";
        int muteTime = 0;

        switch (timescale) {
            case "seconds", "second" -> {
                displayTimescale = (time == 1 ? "second" : "seconds");
                muteTime = time;
            }
            case "minutes", "minute" -> {
                displayTimescale = (time == 1 ? "minute" : "minutes");
                muteTime = time * 60;
            }
            case "hours", "hour" -> {
                displayTimescale = (time == 1 ? "hour" : "hours");
                muteTime = time * 3600;
            }
            case "days", "day" -> {
                displayTimescale = (time == 1 ? "day" : "days");
                muteTime = time * (3600 * 24);
            }
            case "months", "month" -> {
                displayTimescale = (time == 1 ? "month" : "months");
                muteTime = time * 2629800;
            }
            case "years", "year" -> {
                if (time > 1000) {
                    displayTimescale = "indefinite";
                } else {
                    displayTimescale = (time == 1 ? "year" : "years");
                    muteTime = (time * 364) * 86400;
                }
            }
        }

        if (muteTime > 0)
            target.mute(muteTime);

        if (displayTimescale.equalsIgnoreCase("indefinite"))
            player.sendLocaleMessage("command-mute-indefinite",
                    StringPlaceholders.of("player", target.getName()));
        else
            player.sendLocaleMessage("command-mute-success",
                    StringPlaceholders.of(
                            "player", target.getName(),
                            "time", time,
                            "scale", this.getLocaleManager().getLocaleMessage("command-mute-" + displayTimescale)
                    ));

        target.sendLocaleMessage("command-mute-muted");
    }

}
