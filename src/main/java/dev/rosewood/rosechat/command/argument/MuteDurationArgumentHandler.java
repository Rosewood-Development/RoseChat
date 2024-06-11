package dev.rosewood.rosechat.command.argument;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;
import dev.rosewood.rosegarden.utils.StringPlaceholders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MuteDurationArgumentHandler extends ArgumentHandler<Integer> {

    private final Map<String, String> localisedTimescales;

    public MuteDurationArgumentHandler() {
        super(Integer.class);

        LocaleManager localeManager = RoseChat.getInstance().getManager(LocaleManager.class);
        this.localisedTimescales = new HashMap<>(){{
            this.put(localeManager.getMessage("command-mute-seconds"), "seconds");
            this.put(localeManager.getMessage("command-mute-second"), "second");
            this.put(localeManager.getMessage("command-mute-minutes"), "minutes");
            this.put(localeManager.getMessage("command-mute-minute"), "minute");
            this.put(localeManager.getMessage("command-mute-hours"), "hours");
            this.put(localeManager.getMessage("command-mute-hour"), "hour");
            this.put(localeManager.getMessage("command-mute-days"), "days");
            this.put(localeManager.getMessage("command-mute-day"), "day");
            this.put(localeManager.getMessage("command-mute-months"), "months");
            this.put(localeManager.getMessage("command-mute-month"), "month");
            this.put(localeManager.getMessage("command-mute-years"), "years");
            this.put(localeManager.getMessage("command-mute-year"), "year");
        }};
    }

    @Override
    public Integer handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String timeInput = inputIterator.next();
        String timescaleInput = inputIterator.next();

        if (timescaleInput.isEmpty())
            throw new HandledArgumentException("command-mute-scale-required");

        int time;
        try {
            time = Integer.parseInt(timeInput);
        } catch (Exception e) {
            throw new HandledArgumentException("argument-handler-integer");
        }

        // Convert the localised timescale into something we can use.
        String internalTimescale = this.localisedTimescales.get(timescaleInput);
        if (internalTimescale == null) {
            throw new HandledArgumentException("argument-handler-timescale");
        }

        return time;
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        if (args.length == 0 || args.length == 1) {
            return List.of("<time>");
        } else {
            return this.localisedTimescales.keySet().stream().toList();
        }
    }

}

