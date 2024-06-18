package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.ChannelArgumentHandler;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import dev.rosewood.rosechat.placeholder.DefaultPlaceholders;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.argument.ArgumentHandlers;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BroadcastCommand extends RoseChatCommand {

    public BroadcastCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("broadcast")
                .descriptionKey("command-broadcast-description")
                .permission("rosechat.broadcast")
                .arguments(ArgumentsDefinition.builder()
                        .optional("channel", new ChannelArgumentHandler(false))
                        .required("message", ArgumentHandlers.GREEDY_STRING)
                        .build())
                .build();
    }

    @Override
    protected boolean hasPriority() {
        return true;
    }

    @RoseExecutable
    public void execute(CommandContext context, String message) {
        RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
            RosePlayer sender = new RosePlayer(context.getSender());

            for (Player player : Bukkit.getOnlinePlayers()) {
                RosePlayer viewer = new RosePlayer(player);

                BaseComponent[] components = RoseChatAPI.getInstance().parse(sender, viewer, message,
                        DefaultPlaceholders.getFor(sender, viewer).build());
                viewer.send(components);
            }
        });
    }

    @RoseExecutable
    public void execute(CommandContext context, Channel channel, String message) {
        channel.send(new RosePlayer(context.getSender()), message, null);
    }

}
