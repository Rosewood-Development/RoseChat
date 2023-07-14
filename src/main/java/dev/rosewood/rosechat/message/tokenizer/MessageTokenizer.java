package dev.rosewood.rosechat.message.tokenizer;

import com.google.common.base.Stopwatch;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.composer.TokenComposer;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import dev.rosewood.rosechat.message.tokenizer.placeholder.RoseChatPlaceholderTokenizer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class MessageTokenizer {

    private final List<Tokenizer> tokenizers;
    private final RoseMessage roseMessage;
    private final RosePlayer viewer;
    private final Token rootToken;

    private MessageTokenizer(RoseMessage roseMessage, RosePlayer viewer, String format, Tokenizers.TokenizerBundle... tokenizerBundles) {
        this.roseMessage = roseMessage;
        this.viewer = viewer;
        this.tokenizers = zipperMerge(Arrays.stream(tokenizerBundles).map(Tokenizers.TokenizerBundle::tokenizers).toList());
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
                if (token.ignoresTokenizer(tokenizer))
                    continue;

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

    public BaseComponent[] toComponents(TokenComposer composer) {
        return this.toComponents(this.rootToken, composer);
    }

    public BaseComponent[] toComponents(Token token, TokenComposer composer) {
        BaseComponent[] components = composer.compose(token);
        RoseChat.getInstance().getLogger().warning(Arrays.stream(components).map(ComponentSerializer::toString).collect(Collectors.joining(",")));
        return components;
    }

    public static BaseComponent[] tokenize(RoseMessage roseMessage, RosePlayer viewer, String message, Tokenizers.TokenizerBundle... tokenizerBundles) {
        return tokenize(roseMessage, viewer, message, null, tokenizerBundles);
    }

    public static BaseComponent[] tokenize(RoseMessage roseMessage, RosePlayer viewer, String message, TokenComposer composer, Tokenizers.TokenizerBundle... tokenizerBundles) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        MessageTokenizer tokenizer = new MessageTokenizer(roseMessage, viewer, message, tokenizerBundles);

        if (composer == null)
            composer = TokenComposer.decorated(tokenizer);

        BaseComponent[] components = tokenizer.toComponents(composer);
        RoseChat.getInstance().getLogger().warning("Took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms to tokenize " + countTokens(tokenizer.rootToken) + " tokens");
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

    public int findDecoratorContentLength(TokenDecorator decorator) {
        return this.findDecoratorContentLength(this.rootToken, decorator, new AtomicBoolean());
    }

    private int findDecoratorContentLength(Token token, TokenDecorator decorator, AtomicBoolean counting) {
        if (token.getType() != TokenType.GROUP)
            throw new IllegalStateException("Cannot find decorator content length of a token that is not of type GROUP");

        int length = 0;
        for (Token child : token.getChildren()) {
            switch (child.getType()) {
                case TEXT -> {
                    if (counting.get())
                        length += child.getContent().replaceAll("\\s", "").length();
                }
                case DECORATOR -> {
                    if (counting.get() && child.getDecorators().stream().anyMatch(decorator::isOverwrittenBy)) {
                        counting.set(false);
                        return length;
                    } else if (child.getDecorators().contains(decorator)) {
                        counting.set(true);
                    }
                }
                case GROUP -> {
                    if (!counting.get() || !child.shouldEncapsulate())
                        length += this.findDecoratorContentLength(child, decorator, counting);
                }
            }
        }

        if (token.shouldEncapsulate())
            counting.set(false);

        return length;
    }

}
