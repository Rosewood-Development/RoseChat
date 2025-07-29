package dev.rosewood.rosechat.message.tokenizer.composer;

import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.TokenType;
import dev.rosewood.rosechat.message.tokenizer.decorator.FormatDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;

public class MarkdownChatComposer implements ChatComposer<String> {

    public static final MarkdownChatComposer INSTANCE = new MarkdownChatComposer();

    private static final Map<FormatDecorator.FormatType, String> FORMAT_TO_MARKDOWN = new HashMap<>() {{
        this.put(FormatDecorator.FormatType.BOLD, "**");
        this.put(FormatDecorator.FormatType.ITALIC, "*");
        this.put(FormatDecorator.FormatType.UNDERLINE, "__");
        this.put(FormatDecorator.FormatType.STRIKETHROUGH, "~~");
    }};

    private MarkdownChatComposer() {

    }

    @Override
    public String compose(Token token) {
        return this.compose(token, new StringBuilder(), new ArrayDeque<>());
    }

    protected String compose(Token token, StringBuilder stringBuilder, Deque<FormatDecorator.FormatType> activeFormats) {
        if (token.getType() != TokenType.GROUP)
            throw new IllegalStateException("Cannot convert a token that is not of type GROUP");

        for (Token child : token.getChildren()) {
            switch (child.getType()) {
                case TEXT -> stringBuilder.append(child.getContent());
                case DECORATOR -> stringBuilder.append(this.decoratorsToMarkdown(child.getDecorators(), activeFormats));
                case GROUP -> {
                    Deque<FormatDecorator.FormatType> childFormats = child.shouldEncapsulate() ? new ArrayDeque<>(activeFormats) : activeFormats;
                    this.compose(child, stringBuilder, childFormats);
                }
            }
        }

        stringBuilder.append(this.squish(activeFormats));

        return stringBuilder.toString();
    }

    private String decoratorsToMarkdown(List<TokenDecorator> decorators, Deque<FormatDecorator.FormatType> activeFormats) {
        StringBuilder format = new StringBuilder();

        for (TokenDecorator decorator : decorators) {
            if (!(decorator instanceof FormatDecorator formatDecorator))
                continue;

            FormatDecorator.FormatType formatType = formatDecorator.getFormatType();
            if (formatDecorator.isMarker()) {
                // Marker, this means end the markdown here, only end if it's active
                if (activeFormats.remove(formatType)) {
                    String markdown = FORMAT_TO_MARKDOWN.get(formatType);
                    format.append(markdown);
                }

                continue;
            }

            if (formatType == FormatDecorator.FormatType.RESET) {
                format.append(this.squish(activeFormats));
            } else {
                String markdown = FORMAT_TO_MARKDOWN.get(formatType);
                if (markdown != null) {
                    format.append(markdown);
                    activeFormats.add(formatType);
                }
            }
        }

        return format.toString();
    }

    private String squish(Deque<FormatDecorator.FormatType> existingDecorators) {
        StringBuilder format = new StringBuilder();
        while (!existingDecorators.isEmpty()) {
            FormatDecorator.FormatType formatType = existingDecorators.removeLast();
            format.append(FORMAT_TO_MARKDOWN.get(formatType));
        }

        return format.toString();
    }

    /**
     * Not implemented.
     * @throws UnsupportedOperationException always
     */
    @Override
    public String composeLegacy(String text) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Not implemented.
     * @throws UnsupportedOperationException always
     */
    @Override
    public String composeJson(String json) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Not implemented.
     * @throws UnsupportedOperationException always
     */
    @Override
    public String composeBungee(BaseComponent[] components) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ChatComposer.Adventure<String> composeAdventure() {
        return Adventure.INSTANCE;
    }

    public static final class Adventure implements ChatComposer.Adventure<String> {

        private static final Adventure INSTANCE = new Adventure();

        private Adventure() {

        }

        /**
         * Not implemented.
         * @throws UnsupportedOperationException always
         */
        @Override
        public String compose(Component component) {
            throw new UnsupportedOperationException("Not implemented");
        }

    }

}
