package dev.rosewood.rosechat.command.argument;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.filter.Filter;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.placeholder.CustomPlaceholder;
import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;
import java.util.ArrayList;
import java.util.List;

public class ContentArgumentHandler extends ArgumentHandler<String> {

    private final RoseChatAPI api;
    private final boolean onlyEmoji;

    public ContentArgumentHandler(boolean onlyEmoji) {
        super(String.class);

        this.api = RoseChatAPI.getInstance();
        this.onlyEmoji = onlyEmoji;
    }

    @Override
    public String handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        List<String> inputs = new ArrayList<>();
        while (inputIterator.hasNext())
            inputs.add(inputIterator.next());

        String combined = String.join(" ", inputs);
        if (combined.isEmpty())
            throw new HandledArgumentException("argument-handler-string");

        return combined;
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        RosePlayer player = new RosePlayer(context.getSender());

        List<String> arguments = new ArrayList<>();
        if (this.onlyEmoji) {
            for (Filter filter : this.api.getFilters()) {
                if (!filter.isEmoji())
                    continue;

                if (!filter.hasPermission(player))
                    continue;

                arguments.addAll(filter.matches());
                arguments.add(filter.prefix());
            }
        } else {
            for (Filter filter : this.api.getFilters()) {
                if (!filter.hasPermission(player))
                    continue;

                if (filter.matches().isEmpty())
                    continue;

                arguments.addAll(filter.matches());
                arguments.add(filter.prefix());
            }

            for (CustomPlaceholder placeholder : this.api.getPlaceholderManager().getPlaceholders().values()) {
                String permission = "rosechat.placeholder.rosechat." + placeholder.getId();
                if (!player.hasPermission(permission))
                    continue;

                arguments.add("{" + placeholder.getId() + "}");
            }
        }

        return arguments;
    }

}
