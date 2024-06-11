package dev.rosewood.rosechat.command.group;

import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class GroupInviteCommand extends RoseChatCommand {

    public GroupInviteCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("invite")
                .descriptionKey("command-gc-invite-description")
                .permission("rosechat.group.invite")
                .playerOnly(true)
                .arguments(ArgumentsDefinition.builder()
                        .required("player", RoseChatArgumentHandlers.ROSE_PLAYER)
                        .build())
                .build();
    }

    @RoseExecutable
    public void execute(CommandContext context, RosePlayer target) {
        RosePlayer player = new RosePlayer(context.getSender());
        GroupChannel group = player.getOwnedGroupChannel();
        if (group == null) {
            player.sendLocaleMessage("no-gc");
            return;
        }

        if (group.getMembers().contains(target.getUUID())) {
            player.sendLocaleMessage("command-gc-invite-member");
            return;
        }

        target.invite(group);

        player.sendLocaleMessage("command-gc-invite-success",
                StringPlaceholders.of(
                        "player", target.getName(),
                        "name", group.getName()
                ));
        target.sendLocaleMessage("command-gc-invite-invited",
                StringPlaceholders.of(
                        "player", player.getName(),
                        "name", group.getName()
                ));

        // Build and send the clickable accept message.
        ComponentBuilder builder = new ComponentBuilder();
        builder.append("          ");
        builder.append(this.getLocaleManager().getLocaleMessage("command-gc-accept-accept"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        TextComponent.fromLegacyText(this.getLocaleManager().getLocaleMessage("command-gc-accept-hover"))))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/group accept " + group.getId()));
        builder.append("          ").retain(ComponentBuilder.FormatRetention.NONE);
        builder.append(this.getLocaleManager().getLocaleMessage("command-gc-deny-deny"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        TextComponent.fromLegacyText(this.getLocaleManager().getLocaleMessage("command-gc-deny-hover"))))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/group deny " + group.getId()));

        target.send(builder.create());
    }

}
