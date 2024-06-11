package dev.rosewood.rosechat.command.argument;

import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;
import java.util.List;
import java.util.UUID;

public class UUIDArgumentHandler extends ArgumentHandler<UUID> {

    public UUIDArgumentHandler() {
        super(UUID.class);
    }

    @Override
    public UUID handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();

        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        // Internal use only, used for deleting messages.
        return List.of();
    }

}
