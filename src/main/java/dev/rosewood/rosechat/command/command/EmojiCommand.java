package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.ContentArgumentHandler;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import dev.rosewood.rosechat.placeholder.DefaultPlaceholders;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import net.md_5.bungee.api.chat.BaseComponent;

public class EmojiCommand extends RoseChatCommand {

    public EmojiCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("emoji")
                .descriptionKey("command-emoji-description")
                .permission("rosechat.emoji")
                .arguments(ArgumentsDefinition.builder()
                        .optional("message", new ContentArgumentHandler(true))
                        .build())
                .build();
    }

    @Override
    protected boolean hasPriority() {
        return false;
    }

    @RoseExecutable
    public void execute(CommandContext context) {
        RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
            RosePlayer sender = new RosePlayer(context.getSender());

            String message = this.getLocaleManager().getMessage("command-emoji-default-reply");
            MessageTokenizerResults components = RoseChatAPI.getInstance().parse(sender, sender, message,
                    DefaultPlaceholders.getFor(sender, sender).build());
            sender.send(components);
        });
    }

    @RoseExecutable
    public void execute(CommandContext context, String message) {
        RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
            RosePlayer sender = new RosePlayer(context.getSender());

            MessageTokenizerResults components = RoseChatAPI.getInstance().parse(sender, sender, message,
                    DefaultPlaceholders.getFor(sender, sender).build());
            sender.send(components);
        });
    }

}
