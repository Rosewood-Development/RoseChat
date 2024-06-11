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

public class ToggleSoundCommand extends RoseChatCommand {

    public ToggleSoundCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("togglesound")
                .aliases("togglesounds", "toggleping", "toggletag", "togglepings", "toggletags")
                .descriptionKey("command-togglesound-description")
                .permission("rosechat.togglesound")
                .playerOnly(true)
                .arguments(ArgumentsDefinition.builder()
                        .requiredSub("type",
                                new ToggleMessageSoundsCommand(this.rosePlugin),
                                new ToggleTagSoundsCommand(this.rosePlugin),
                                new ToggleAllSoundsCommand(this.rosePlugin)
                        ))
                .build();
    }

    @Override
    protected boolean hasPriority() {
        return true;
    }

    public static class ToggleMessageSoundsCommand extends RoseChatCommand {

        public ToggleMessageSoundsCommand(RosePlugin rosePlugin) {
            super(rosePlugin);
        }

        @RoseExecutable
        public void execute(CommandContext context) {
            RosePlayer player = new RosePlayer(context.getSender());
            PlayerData data = player.getPlayerData();

            data.setMessageSounds(!data.hasMessageSounds());
            data.save();

            if (data.hasMessageSounds())
                player.sendLocaleMessage("command-togglesound-enabled",
                        StringPlaceholders.of("type", this.getLocaleManager().getMessage("command-togglesound-messages")));
            else
                player.sendLocaleMessage("command-togglesound-disabled",
                        StringPlaceholders.of("type", this.getLocaleManager().getMessage("command-togglesound-messages")));
        }

        @Override
        protected CommandInfo createCommandInfo() {
            return CommandInfo.builder("messages")
                    .build();
        }

    }

    public static class ToggleTagSoundsCommand extends RoseChatCommand {

        public ToggleTagSoundsCommand(RosePlugin rosePlugin) {
            super(rosePlugin);
        }

        @RoseExecutable
        public void execute(CommandContext context) {
            RosePlayer player = new RosePlayer(context.getSender());
            PlayerData data = player.getPlayerData();

            data.setTagSounds(!data.hasTagSounds());
            data.save();

            if (data.hasTagSounds())
                player.sendLocaleMessage("command-togglesound-enabled",
                        StringPlaceholders.of("type", this.getLocaleManager().getMessage("command-togglesound-tags")));
            else
                player.sendLocaleMessage("command-togglesound-disabled",
                        StringPlaceholders.of("type", this.getLocaleManager().getMessage("command-togglesound-tags")));
        }

        @Override
        protected CommandInfo createCommandInfo() {
            return CommandInfo.builder("tags")
                    .build();
        }

    }

    public static class ToggleAllSoundsCommand extends RoseChatCommand {

        public ToggleAllSoundsCommand(RosePlugin rosePlugin) {
            super(rosePlugin);
        }

        @RoseExecutable
        public void execute(CommandContext context) {
            RosePlayer player = new RosePlayer(context.getSender());
            PlayerData data = player.getPlayerData();

            boolean newState = !(data.hasMessageSounds() || data.hasTagSounds());

            data.setTagSounds(newState);
            data.setMessageSounds(newState);
            data.save();

            if (newState)
                player.sendLocaleMessage("command-togglesound-enabled",
                        StringPlaceholders.of("type", this.getLocaleManager().getMessage("command-togglesound-all")));
            else
                player.sendLocaleMessage("command-togglesound-disabled",
                        StringPlaceholders.of("type", this.getLocaleManager().getMessage("command-togglesound-all")));
        }

        @Override
        protected CommandInfo createCommandInfo() {
            return CommandInfo.builder("all")
                    .build();
        }

    }

}
