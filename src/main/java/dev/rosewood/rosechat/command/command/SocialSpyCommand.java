package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;

public class SocialSpyCommand extends RoseChatCommand {

    public SocialSpyCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("socialspy")
                .aliases("ss", "spy")
                .descriptionKey("command-socialspy-description")
                .permission("rosechat.spy")
                .playerOnly(true)
                .arguments(ArgumentsDefinition.builder()
                        .requiredSub("type",
                                new MessageSpyCommand(this.rosePlugin),
                                new ChannelSpyCommand(this.rosePlugin),
                                new GroupSpyCommand(this.rosePlugin),
                                new AllSpyCommand(this.rosePlugin)
                        ))
                .build();
    }

    @Override
    protected boolean hasPriority() {
        return true;
    }

    public static class MessageSpyCommand extends RoseChatCommand {

        public MessageSpyCommand(RosePlugin rosePlugin) {
            super(rosePlugin);
        }

        @RoseExecutable
        public void execute(CommandContext context) {
            RosePlayer player = new RosePlayer(context.getSender());
            PlayerData data = player.getPlayerData();

            data.setMessageSpy(!data.hasMessageSpy());
            data.save();

            if (data.hasMessageSpy())
                player.sendLocaleMessage("command-socialspy-enabled",
                        StringPlaceholders.of("type", this.getLocaleManager().getMessage("command-socialspy-message")));
            else
                player.sendLocaleMessage("command-socialspy-disabled",
                        StringPlaceholders.of("type", this.getLocaleManager().getMessage("command-socialspy-message")));
        }

        @Override
        protected CommandInfo createCommandInfo() {
            return CommandInfo.builder("message")
                    .permission("rosechat.spy.message")
                    .build();
        }

    }

    public static class GroupSpyCommand extends RoseChatCommand {

        public GroupSpyCommand(RosePlugin rosePlugin) {
            super(rosePlugin);
        }

        @RoseExecutable
        public void execute(CommandContext context) {
            RosePlayer player = new RosePlayer(context.getSender());
            PlayerData data = player.getPlayerData();

            data.setGroupSpy(!data.hasGroupSpy());
            data.save();

            if (data.hasGroupSpy())
                player.sendLocaleMessage("command-socialspy-enabled",
                        StringPlaceholders.of("type", this.getLocaleManager().getMessage("command-socialspy-group")));
            else
                player.sendLocaleMessage("command-socialspy-disabled",
                        StringPlaceholders.of("type", this.getLocaleManager().getMessage("command-socialspy-group")));
        }

        @Override
        protected CommandInfo createCommandInfo() {
            return CommandInfo.builder("group")
                    .permission("rosechat.spy.group")
                    .build();
        }

    }

    public static class ChannelSpyCommand extends RoseChatCommand {

        public ChannelSpyCommand(RosePlugin rosePlugin) {
            super(rosePlugin);
        }

        @RoseExecutable
        public void execute(CommandContext context) {
            RosePlayer player = new RosePlayer(context.getSender());
            PlayerData data = player.getPlayerData();

            data.setChannelSpy(!data.hasChannelSpy());
            data.save();

            if (data.hasChannelSpy())
                player.sendLocaleMessage("command-socialspy-enabled",
                        StringPlaceholders.of("type", this.getLocaleManager().getMessage("command-socialspy-channel")));
            else
                player.sendLocaleMessage("command-socialspy-disabled",
                        StringPlaceholders.of("type", this.getLocaleManager().getMessage("command-socialspy-channel")));
        }

        @Override
        protected CommandInfo createCommandInfo() {
            return CommandInfo.builder("channel")
                    .permission("rosechat.spy.channel")
                    .build();
        }

    }

    public static class AllSpyCommand extends RoseChatCommand {

        public AllSpyCommand(RosePlugin rosePlugin) {
            super(rosePlugin);
        }

        @RoseExecutable
        public void execute(CommandContext context) {
            RosePlayer player = new RosePlayer(context.getSender());
            PlayerData data = player.getPlayerData();

            boolean newState = !(data.hasMessageSpy() && data.hasChannelSpy()) ||
                    !(data.hasMessageSpy() && data.hasGroupSpy()) ||
                    !(data.hasChannelSpy() && data.hasGroupSpy());

            data.setMessageSpy(newState);
            data.setChannelSpy(newState);
            data.setGroupSpy(newState);
            data.save();

            if (newState)
                player.sendLocaleMessage("command-socialspy-enabled",
                        StringPlaceholders.of("type", this.getLocaleManager().getMessage("command-socialspy-all")));
            else
                player.sendLocaleMessage("command-socialspy-disabled",
                        StringPlaceholders.of("type", this.getLocaleManager().getMessage("command-socialspy-all")));;
        }

        @Override
        protected CommandInfo createCommandInfo() {
            return CommandInfo.builder("all")
                    .permission("rosechat.spy.message")
                    .build();
        }

    }

}
