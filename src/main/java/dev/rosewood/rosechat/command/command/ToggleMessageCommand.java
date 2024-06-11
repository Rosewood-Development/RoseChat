package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;

public class ToggleMessageCommand extends RoseChatCommand {

    public ToggleMessageCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("togglemessage")
                .aliases("togglemessages", "togglepm", "togglepms", "togglemsg", "togglemsgs")
                .descriptionKey("command-togglemessage-description")
                .permission("rosechat.togglemessage")
                .playerOnly(true)
                .build();
    }

    @Override
    protected boolean hasPriority() {
        return true;
    }

    @RoseExecutable
    public void execute(CommandContext context) {
        RosePlayer player = new RosePlayer(context.getSender());
        PlayerData data = player.getPlayerData();

        data.setCanBeMessaged(!data.canBeMessaged());
        data.save();

        if (data.canBeMessaged())
            player.sendLocaleMessage("command-togglemessage-enabled");
        else
            player.sendLocaleMessage("command-togglemessage-disabled");
    }

}
