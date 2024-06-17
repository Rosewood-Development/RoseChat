package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;

public class ToggleEmojiCommand extends RoseChatCommand {

    public ToggleEmojiCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("toggleemoji")
                .descriptionKey("command-toggleemoji-description")
                .permission("rosechat.toggleemoji")
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

        data.setEmojis(!data.hasEmojis());
        data.save();

        if (data.hasEmojis())
            player.sendLocaleMessage("command-toggleemoji-enabled");
        else
            player.sendLocaleMessage("command-toggleemoji-disabled");
    }

}
