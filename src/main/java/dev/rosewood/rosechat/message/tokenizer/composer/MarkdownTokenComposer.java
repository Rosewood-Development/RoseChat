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
import net.md_5.bungee.api.ChatColor;

public class MarkdownTokenComposer implements TokenComposer<String> {

    private static final Map<ChatColor, String> CHAT_COLOR_TO_MARKDOWN = new HashMap<>() {{
        this.put(ChatColor.BOLD, "**");
        this.put(ChatColor.ITALIC, "*");
        this.put(ChatColor.UNDERLINE, "__");
        this.put(ChatColor.STRIKETHROUGH, "~~");
    }};

    protected MarkdownTokenComposer() {

    }

    @Override
    public String compose(Token token) {
        return this.compose(token, new StringBuilder(), new ArrayDeque<>());
    }

    protected String compose(Token token, StringBuilder stringBuilder, Deque<ChatColor> activeFormats) {
        if (token.getType() != TokenType.GROUP)
            throw new IllegalStateException("Cannot convert a token that is not of type GROUP");

        for (Token child : token.getChildren()) {
            switch (child.getType()) {
                case TEXT -> stringBuilder.append(child.getContent());
                case DECORATOR -> stringBuilder.append(this.decoratorsToMarkdown(child.getDecorators(), activeFormats));
                case GROUP -> {
                    Deque<ChatColor> childFormats = child.shouldEncapsulate() ? new ArrayDeque<>(activeFormats) : activeFormats;
                    this.compose(child, stringBuilder, childFormats);
                }
            }
        }

        stringBuilder.append(this.squish(activeFormats));

        return stringBuilder.toString();
    }

    private String decoratorsToMarkdown(List<TokenDecorator> decorators, Deque<ChatColor> activeFormats) {
        StringBuilder format = new StringBuilder();

        for (TokenDecorator decorator : decorators) {
            if (!(decorator instanceof FormatDecorator formatDecorator))
                continue;

            ChatColor chatColor = formatDecorator.getChatColor();
            if (formatDecorator.isMarker()) {
                // Marker, this means end the markdown here, only end if it's active
                if (activeFormats.remove(chatColor)) {
                    String markdown = CHAT_COLOR_TO_MARKDOWN.get(chatColor);
                    format.append(markdown);
                }
                continue;
            }

            if (chatColor == ChatColor.RESET) {
                format.append(this.squish(activeFormats));
            } else {
                String markdown = CHAT_COLOR_TO_MARKDOWN.get(chatColor);
                if (markdown != null) {
                    format.append(markdown);
                    activeFormats.add(chatColor);
                }
            }
        }

        return format.toString();
    }

    private String squish(Deque<ChatColor> existingDecorators) {
        StringBuilder format = new StringBuilder();
        while (!existingDecorators.isEmpty()) {
            ChatColor chatColor = existingDecorators.removeLast();
            format.append(CHAT_COLOR_TO_MARKDOWN.get(chatColor));
        }
        return format.toString();
    }

}
