package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.GroupArgumentHandler;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageRules;
import dev.rosewood.rosechat.message.wrapper.MessageRules.RuleOutputs;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.argument.ArgumentHandlers;
import dev.rosewood.rosegarden.command.framework.ArgumentCondition;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;

public class GroupRenameCommand extends RoseChatCommand {

    public GroupRenameCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("rename")
                .descriptionKey("command-gc-rename-description")
                .permission("rosechat.group.rename")
                .arguments(ArgumentsDefinition.builder()
                        .optional("group", new GroupArgumentHandler(false),
                                ArgumentCondition.hasPermission("rosechat.group.admin"))
                        .required("name", ArgumentHandlers.GREEDY_STRING)
                        .build())
                .build();
    }

    @RoseExecutable
    public void execute(CommandContext context, String name) {
        RosePlayer player = new RosePlayer(context.getSender());
        GroupChannel group = player.getOwnedGroupChannel();
        if (group == null) {
            player.sendLocaleMessage("no-gc");
            return;
        }

        if (!MessageUtils.canColor(player, name, PermissionArea.GROUP)) {
            player.sendLocaleMessage("no-permission");
            return;
        }

        MessageRules rules = new MessageRules().applyLanguageFilter().applyCapsFilter().applyAllFilters();
        RuleOutputs outputs = rules.apply(player, PermissionArea.GROUP, name);
        if (outputs.isBlocked()) {
            if (outputs.getWarning() != null)
                outputs.getWarning().send(player);

            return;
        }

        this.execute(player, group, name);
    }

    @RoseExecutable
    public void execute(CommandContext context, GroupChannel group, String name) {
        this.execute(new RosePlayer(context.getSender()), group, name);
    }

    private void execute(RosePlayer player, GroupChannel group, String name) {
        boolean success = group.rename(name);
        if (!success)
            return;

        this.getLocaleManager().sendComponentMessage(player, "command-gc-rename-success",
                StringPlaceholders.of("name", name));
    }

}
