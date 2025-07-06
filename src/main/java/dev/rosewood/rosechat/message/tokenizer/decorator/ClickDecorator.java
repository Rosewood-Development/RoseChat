package dev.rosewood.rosechat.message.tokenizer.decorator;

import java.util.Objects;
import java.util.regex.Pattern;

public abstract class ClickDecorator implements TokenDecorator {

    protected static final Pattern PATTERN = Pattern.compile("^https?://");

    protected final Action action;
    protected final String value;

    public ClickDecorator(Action action, String value) {
        this.action = Objects.requireNonNullElse(action, Action.SUGGEST_COMMAND);
        this.value = value;
    }

    @Override
    public DecoratorType getType() {
        return DecoratorType.ACTION;
    }

    public enum Action {
        OPEN_URL,
        OPEN_FILE,
        RUN_COMMAND,
        SUGGEST_COMMAND,
        CHANGE_PAGE,
        COPY_TO_CLIPBOARD
    }

}
