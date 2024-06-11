package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageRules;
import dev.rosewood.rosechat.message.wrapper.MessageRules.RuleOutputs;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.argument.ArgumentHandlers;
import dev.rosewood.rosegarden.command.framework.ArgumentCondition;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;

public class NicknameCommand extends RoseChatCommand {

    public NicknameCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("nickname")
                .aliases("nick")
                .descriptionKey("command-nickname-description")
                .permission("rosechat.nickname")
                .arguments(ArgumentsDefinition.builder()
                        .optional("player", RoseChatArgumentHandlers.ROSE_PLAYER,
                                ArgumentCondition.hasPermission("rosechat.nickname.others"))
                        .required("nickname", ArgumentHandlers.GREEDY_STRING)
                        .build())
                .build();
    }

    @Override
    protected boolean hasPriority() {
        return true;
    }

    @RoseExecutable
    public void execute(CommandContext context, String nickname) {
        RosePlayer player = new RosePlayer(context.getSender());
        if (player.isConsole()) {
            player.sendLocaleMessage("only-player");
            return;
        }

        if (nickname.equalsIgnoreCase("off") || nickname.equalsIgnoreCase("remove")) {
            this.removeNickname(player, player);
            return;
        }

        nickname = MessageUtils.stripShaderColors(nickname);

        RoseMessage nicknameMessage = RoseMessage.forLocation(player, PermissionArea.NICKNAME);
        nicknameMessage.setPlayerInput(nickname);

        MessageRules rules = new MessageRules().applyAllFilters().ignoreMessageLogging();
        RuleOutputs outputs = rules.apply(nicknameMessage, nickname);

        if (outputs.isBlocked()) {
            if (outputs.getWarning() != null)
                outputs.getWarning().send(player);
            return;
        }

        if (!MessageUtils.isNicknameAllowed(player, player, nicknameMessage))
            return;

        this.setNickname(player, player, nickname);
    }

    @RoseExecutable
    public void execute(CommandContext context, RosePlayer target, String nickname) {
        RosePlayer player = new RosePlayer(context.getSender());

        if (nickname.equalsIgnoreCase("off") || nickname.equalsIgnoreCase("remove")) {
            this.removeNickname(player, target);
            return;
        }

        nickname = MessageUtils.stripShaderColors(nickname);
        this.setNickname(player, target, nickname);
    }

    private void removeNickname(RosePlayer player, RosePlayer target) {
        boolean success = target.removeNickname();
        if (!success)
            return;

        if (player.isConsole() || !player.getUUID().equals(target.getUUID()))
            player.sendLocaleMessage("command-nickname-player",
                    StringPlaceholders.of(
                            "player", target.getRealName(),
                            "name", target.getName()
                    ));
        target.sendLocaleMessage("command-nickname-success",
                StringPlaceholders.of("name", target.getName()));
    }

    private void setNickname(RosePlayer player, RosePlayer target, String nickname) {
        boolean success = target.setNickname(nickname);
        if (!success)
            return;

        if (player.isConsole() || !player.getUUID().equals(target.getUUID()))
            player.sendLocaleMessage("command-nickname-player",
                    StringPlaceholders.of(
                            "player", target.getRealName(),
                            "name", target.getName()
                    ));
        target.sendLocaleMessage("command-nickname-success",
                StringPlaceholders.of("name", target.getName()));
    }

}
