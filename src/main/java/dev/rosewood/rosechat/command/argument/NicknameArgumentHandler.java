package dev.rosewood.rosechat.command.argument;

import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NicknameArgumentHandler extends ArgumentHandler<String> {

    public NicknameArgumentHandler() {
        super(String.class);
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
        return Arrays.asList(argument.parameter(), "off", "remove");
    }

}
