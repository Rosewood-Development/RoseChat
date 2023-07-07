package dev.rosewood.rosechat.message.tokenizer;

import com.google.common.base.Stopwatch;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorators;
import dev.rosewood.rosechat.message.tokenizer.placeholder.RoseChatPlaceholderTokenizer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class MessageTokenizer {

    private final List<Tokenizer> tokenizers;
    private final RoseMessage roseMessage;
    private final RosePlayer viewer;
    private final Token rootToken;

    private MessageTokenizer(RoseMessage roseMessage, RosePlayer viewer, String format, String... tokenizerBundles) {
        this.roseMessage = roseMessage;
        this.viewer = viewer;
        this.tokenizers = zipperMerge(Arrays.stream(tokenizerBundles).map(Tokenizers::getBundleValues).toList());
        this.rootToken = Token.group(Objects.requireNonNullElse(format, RoseChatPlaceholderTokenizer.MESSAGE_PLACEHOLDER)).build();

        this.tokenizeContent(this.rootToken, 0);
    }

    public void tokenizeContent(Token token, int depth) {
        if (token.getType() != TokenType.GROUP)
            throw new IllegalStateException("Cannot tokenize a token that is not of type GROUP");

        if (depth > 15)
            throw new RuntimeException("Exceeded a depth of 15 when tokenizing message. This is probably due to infinite recursion somewhere: " + this.rootToken.getContent());

        String content = token.getContent();

        outer:
        while (!content.isEmpty()) {
            for (Tokenizer tokenizer : this.tokenizers) {
                TokenizerParams params = new TokenizerParams(this.roseMessage, this.viewer, content, token.containsPlayerInput());
                TokenizerResult result = tokenizer.tokenize(params);
                if (result == null)
                    continue;

                Token child = result.getToken();
                child.parent = token;
                token.children.add(child);
                content = content.substring(result.getConsumed());
                continue outer;
            }
            throw new IllegalStateException(String.format("No tokenizer was able to tokenize the content: [%s]", content));
        }

        if (token.getType() == TokenType.GROUP)
            for (Token child : token.getChildren())
                if (child.getType() == TokenType.GROUP)
                    this.tokenizeContent(child, depth + 1);
    }

    public BaseComponent[] toComponents() {
        BaseComponent[] components = this.toComponents(this.rootToken, new TokenDecorators());
        RoseChat.getInstance().getLogger().warning(Arrays.stream(components).map(ComponentSerializer::toString).collect(Collectors.joining(",")));
        return components;
    }

    public BaseComponent[] toComponents(Token token, TokenDecorators contextDecorators) {
        if (token.getType() != TokenType.GROUP)
            throw new IllegalStateException("Cannot convert a token that is not of type GROUP");

        ComponentBuilder componentBuilder = new ComponentBuilder();
        StringBuilder contentBuilder = new StringBuilder();

        for (Token child : token.getChildren()) {
            if (child.getType() != TokenType.TEXT && !contentBuilder.isEmpty()) {
                componentBuilder.append(contentBuilder.toString(), FormatRetention.NONE);
                contentBuilder.setLength(0);
                contextDecorators.apply(componentBuilder, this, child.getPlaceholders());
            }

            switch (child.getType()) {
                case TEXT -> contentBuilder.append(child.getContent());
                case DECORATOR -> contextDecorators.add(child.getDecorators());
                case GROUP -> {
                    TokenDecorators childDecorators;
                    if (child.shouldEncapsulate()) {
                        childDecorators = new TokenDecorators(contextDecorators);
                    } else {
                        childDecorators = contextDecorators;
                    }

                    for (BaseComponent component : this.toComponents(child, childDecorators))
                        componentBuilder.append(component, FormatRetention.NONE);
                }
            }
        }

        if (!contentBuilder.isEmpty()) {
            componentBuilder.append(contentBuilder.toString(), FormatRetention.NONE);
            contextDecorators.apply(componentBuilder, this, token.getPlaceholders());
        }

        BaseComponent[] components = componentBuilder.create();
        if (token.isPlain() || components.length == 0)
            return components;

        TextComponent wrapperComponent = new TextComponent(components);
        token.getDecorators().forEach(x -> x.apply(wrapperComponent, this, token.getPlaceholders()));
        return new BaseComponent[] { wrapperComponent };
    }

    public static BaseComponent[] tokenize(RoseMessage roseMessage, RosePlayer viewer, String message, String... tokenizerBundles) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        MessageTokenizer tokenizer = new MessageTokenizer(roseMessage, viewer, message, tokenizerBundles);
        BaseComponent[] components = tokenizer.toComponents();
        RoseChat.getInstance().getLogger().warning("Parsing took " + (stopwatch.elapsed(TimeUnit.NANOSECONDS) / 1_000_000D) + "ms with " + countTokens(tokenizer.rootToken) + " tokens");
        return components;
    }

    private static int countTokens(Token token) {
        if (token.getType() != TokenType.GROUP)
            return 1;

        int count = 1;
        for (Token child : token.getChildren())
            count += countTokens(child);
        return count;
    }

    private static <T> List<T> zipperMerge(List<List<T>> listOfLists) {
        if (listOfLists.isEmpty())
            return new ArrayList<>();

        if (listOfLists.size() == 1)
            return listOfLists.get(0);

        List<T> mergedList = new ArrayList<>();
        int maxSize = 0;

        for (List<T> list : listOfLists)
            if (list.size() > maxSize)
                maxSize = list.size();

        for (int i = 0; i < maxSize; i++)
            for (List<T> list : listOfLists)
                if (i < list.size())
                    mergedList.add(list.get(i));

        return mergedList;
    }

}
