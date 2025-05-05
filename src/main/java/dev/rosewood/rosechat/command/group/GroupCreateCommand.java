package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.api.event.group.GroupCreateEvent;
import dev.rosewood.rosechat.api.event.group.GroupPreCreateEvent;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageRules;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.argument.ArgumentHandlers;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GroupCreateCommand extends RoseChatCommand {

    public GroupCreateCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("create")
                .descriptionKey("command-gc-create-description")
                .permission("rosechat.group.create")
                .playerOnly(true)
                .arguments(ArgumentsDefinition.builder()
                        .required("id", ArgumentHandlers.STRING)
                        .optional("name", ArgumentHandlers.GREEDY_STRING)
                        .build())
                .build();
    }

    @RoseExecutable
    public void execute(CommandContext context, String id) {
        RosePlayer player = new RosePlayer(context.getSender());
        this.execute(player, id, player.getRealName() + "'s GroupChat");
    }

    @RoseExecutable
    public void execute(CommandContext context, String id, String name) {
        this.execute(new RosePlayer(context.getSender()), id, name);
    }

    private void execute(RosePlayer player, String id, String name) {
        if (this.getAPI().getGroupChatByOwner(player.getUUID()) != null) {
            this.getLocaleManager().sendComponentMessage(player, "command-gc-create-fail");
            return;
        }

        if (this.getAPI().getGroupChatById(id) != null) {
            this.getLocaleManager().sendComponentMessage(player, "command-gc-already-exists");
            return;
        }

        if (Settings.ADD_GROUP_CHANNELS_TO_CHANNEL_LIST.get() && this.getAPI().getChannelById(id) != null) {
            this.getLocaleManager().sendComponentMessage(player, "command-gc-already-exists");
            return;
        }

        if (!MessageUtils.canColor(player, name, PermissionArea.GROUP)) {
            this.getLocaleManager().sendComponentMessage(player, "no-permission");
            return;
        }

        RoseMessage message = RoseMessage.forLocation(player, PermissionArea.GROUP);
        MessageRules rules = new MessageRules().applyAllFilters();

        MessageRules.RuleOutputs outputs = rules.apply(message, name);
        if (outputs.isBlocked()) {
            if (outputs.getWarning() != null)
                outputs.getWarning().send(player);

            if (Settings.SEND_BLOCKED_MESSAGES_TO_STAFF.get()) {
                for (Player staffPlayer : Bukkit.getOnlinePlayers()) {
                    if (staffPlayer.hasPermission("rosechat.seeblocked")) {
                        RosePlayer rosePlayer = new RosePlayer(staffPlayer);
                        rosePlayer.sendLocaleMessage("blocked-message",
                                StringPlaceholders.of("player", message.getSender().getName(),
                                        "message", name));
                    }
                }
            }

            return;
        }

        GroupPreCreateEvent event = new GroupPreCreateEvent(player.asPlayer(), id, name);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        name = event.getName();

        GroupChannel group = this.getAPI().createGroupChat(id, player.asPlayer());
        group.setName(name);

        GroupCreateEvent groupCreateEvent = new GroupCreateEvent(group);
        Bukkit.getPluginManager().callEvent(groupCreateEvent);

        this.getLocaleManager().sendComponentMessage(player, "command-gc-create-success",
                StringPlaceholders.of("name", name));
    }

}
