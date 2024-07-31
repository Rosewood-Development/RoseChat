package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.argument.ArgumentHandlers;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.BaseRoseCommand;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;

public class GroupMessageCommand extends BaseRoseCommand {

    public GroupMessageCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("gcm")
                .aliases("gcmsg", "gmsg")
                .descriptionKey("command-gcm-description")
                .permission("rosechat.group.message")
                .playerOnly(true)
                .arguments(ArgumentsDefinition.builder()
                        .required("group", RoseChatArgumentHandlers.MEMBER_GROUP)
                        .optional("message", ArgumentHandlers.GREEDY_STRING)
                        .build())
                .build();
    }

    @RoseExecutable
    public void execute(CommandContext context, GroupChannel group) {
        RosePlayer player = new RosePlayer(context.getSender());

        if (!Settings.CAN_JOIN_GROUP_CHANNELS.get()) {
            player.sendLocaleMessage("message-blank");
            return;
        }

        boolean success = player.switchChannel(group, true);
        if (!success) {
            player.sendLocaleMessage("argument-handler-group");
            return;
        }

        player.sendLocaleMessage("command-channel-joined",
                StringPlaceholders.of("id", group.getName()));
    }

    @RoseExecutable
    public void execute(CommandContext context, GroupChannel group, String message) {
        RosePlayer player = new RosePlayer(context.getSender());

        if (MessageUtils.isMessageEmpty(message)) {
            player.sendLocaleMessage("message-blank");
            return;
        }

        player.quickChat(group, message);
    }

}
