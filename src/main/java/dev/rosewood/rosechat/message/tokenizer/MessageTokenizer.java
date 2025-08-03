package dev.rosewood.rosechat.message.tokenizer;

import com.google.common.base.Stopwatch;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.manager.DebugManager;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.composer.ChatComposer;
import dev.rosewood.rosechat.message.tokenizer.decorator.DecoratorType;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import dev.rosewood.rosechat.message.tokenizer.placeholder.RoseChatPlaceholderTokenizer;
import dev.rosewood.rosechat.message.contents.MessageContents;
import dev.rosewood.rosechat.message.RoseMessage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MessageTokenizer {

    private static final DebugManager DEBUG_MANAGER = RoseChat.getInstance().getManager(DebugManager.class);
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("#.##");

    private final List<Tokenizer> tokenizers;
    private final RoseMessage roseMessage;
    private final RosePlayer viewer;
    private final MessageDirection direction;
    private final MessageOutputs outputs;
    private int parses = 0;

    private MessageTokenizer(RoseMessage roseMessage, RosePlayer viewer, MessageDirection direction, MessageOutputs outputs,
                             List<Tokenizer> tokenizers) {
        this.roseMessage = roseMessage;
        this.viewer = viewer;
        this.tokenizers = tokenizers;
        this.outputs = outputs;
        this.direction = direction;
    }

    public void tokenize(Token token) {
        this.tokenize(token, 0);
    }

    private void tokenize(Token token, int depth) {
        if (token.getType() != TokenType.GROUP)
            throw new IllegalStateException("Cannot tokenize a token that is not of type GROUP");

        if (depth > 15) {
            Deque<Token> tokens = new ArrayDeque<>();
            Token current = token;
            while (current != null) {
                tokens.addFirst(current);
                current = current.parent;
            }

            throw new RuntimeException("Exceeded a depth of 15 when tokenizing message; this is probably due to infinite recursion somewhere. Token stack: " +
                    tokens.stream().map(t -> t.getType().name() + ":" + t.getContent()).collect(Collectors.joining(" -> ")));
        }

        List<Token> children = this.tokenizeContent(token.getContent(), token, depth);
        token.getChildren().addAll(children);
        token.getChildren().forEach(x -> x.parent = token); // Make sure all children have their parent assigned

        this.tokenizeContentDecorators(token, depth);

        for (Token child : token.getChildren()) {
            this.tokenizeContentDecorators(child, depth);
            if (child.getType() == TokenType.GROUP)
                this.tokenize(child, depth + 1);
        }
    }

    private List<Token> tokenizeContent(String content, Token parentToken, int depth) {
        if (content.isEmpty())
            return List.of();

        TokenizerParams params = new TokenizerParams(this.roseMessage, this.viewer, content, parentToken,
                this.roseMessage.shouldUsePlayerChatColor(), this.direction, this.outputs);
        for (Tokenizer tokenizer : this.tokenizers) {
            if (parentToken.getIgnoredTokenizers().contains(tokenizer))
                continue;

            long startTime = System.nanoTime();
            this.parses++;

            List<TokenizerResult> results = tokenizer.tokenize(params);
            if (results == null || results.isEmpty())
                continue;

            // Match, build tokens from matched content and then tokenize the content between matches
            List<Token> children = new ArrayList<>();
            int currentIndex = 0;
            for (TokenizerResult result : results) {
                if (result.index() > currentIndex) {
                    // Content before the match, tokenize
                    children.addAll(this.tokenizeContent(content.substring(currentIndex, result.index()), parentToken, depth));
                    currentIndex = result.index();
                }
                children.add(result.token());
                currentIndex += result.consumed();
            }

            if (currentIndex < content.length()) {
                // Content after the match, tokenize
                children.addAll(this.tokenizeContent(content.substring(currentIndex), parentToken, depth + 1));
            }

            if (DEBUG_MANAGER.isEnabled() && tokenizer != Tokenizers.CHARACTER) {
                DEBUG_MANAGER.addMessage(() ->
                        "[" + tokenizer.getClass().getSimpleName() + "] Tokenized: " + content + " -> " +
                                parentToken.getChildren().stream().filter(x -> x.getType() != TokenType.DECORATOR).map(Token::getContent).collect(Collectors.joining()) + " in " +
                                NUMBER_FORMAT.format((System.nanoTime() - startTime) / 1000000.0) + "ms");
            }

            return children;
        }

        throw new IllegalStateException(String.format("No tokenizer was able to tokenize the content: [%s]", content));
    }

    private void tokenizeContentDecorators(Token token, int depth) {
        if (token.getType() != TokenType.TEXT) {
            for (TokenDecorator decorator : token.getDecorators()) {
                if (decorator.getType() == DecoratorType.CONTENT && decorator.getContent() != null) {
                    Token.Builder decoratorContent = decorator.getContent();
                    decoratorContent.placeholders(token.getPlaceholders());
                    token.getIgnoredTokenizers().forEach(decoratorContent::ignoreTokenizer);
                    token.getIgnoredFilters().forEach(decoratorContent::ignoreFilter);
                    Token decoratorToken = decoratorContent.build();
                    this.tokenize(decoratorToken, depth + 1);
                    decorator.setContentToken(decoratorToken);
                }
            }
        }
    }

    public static MessageContents tokenize(RoseMessage roseMessage, RosePlayer viewer, String format, MessageDirection direction,
                                           Tokenizers.TokenizerBundle... tokenizerBundles) {
        if (format == null) {
            if (roseMessage.getPlayerInput() != null) {
                new RuntimeException("A null format was passed to the MessageTokenizer. The format has been replaced with {message} instead. " +
                        "A harmless stacktrace will be printed below so this can be fixed.").printStackTrace();
                format = RoseChatPlaceholderTokenizer.MESSAGE_PLACEHOLDER;
            } else {
                new RuntimeException("A null format was passed to the MessageTokenizer. The format has been replaced with an empty string instead. " +
                        "A harmless stacktrace will be printed below so this can be fixed.").printStackTrace();
                format = "";
            }
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Tokenizer> tokenizers = zipperMerge(Arrays.stream(tokenizerBundles).map(Tokenizers.TokenizerBundle::tokenizers).toList());
        if (!tokenizers.contains(Tokenizers.CHARACTER))
            tokenizers.addLast(Tokenizers.CHARACTER);
        MessageOutputs outputs = new MessageOutputs();
        MessageTokenizer tokenizer = new MessageTokenizer(roseMessage, viewer, direction, outputs, tokenizers);

        Token token = Token.group(format).build();
        tokenizer.tokenize(token);

        if (DEBUG_MANAGER.isEnabled()) {
            String plainText = ChatComposer.plain().compose(token);
            DEBUG_MANAGER.addMessage(() ->
                    "Completed Tokenizing: " + plainText + "\n"
                    + "Took " + NUMBER_FORMAT.format(stopwatch.elapsed(TimeUnit.NANOSECONDS) / 1000000.0) +
                            "ms to tokenize " + countTokens(token) + " tokens " + tokenizer.parses + " times \n");
        }

        return MessageContents.fromToken(token, outputs);
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

        Set<T> allValues = new HashSet<>(); // used to disallow duplicates in the final List
        List<T> mergedList = new ArrayList<>();
        int maxSize = 0;

        for (List<T> list : listOfLists)
            if (list.size() > maxSize)
                maxSize = list.size();

        for (int i = 0; i < maxSize; i++) {
            for (List<T> list : listOfLists) {
                if (i < list.size()) {
                    T value = list.get(i);
                    if (allValues.add(value))
                        mergedList.add(list.get(i));
                }
            }
        }

        return mergedList;
    }

    public static int findDecoratorContentLength(Token source, TokenDecorator decorator) {
        return findDecoratorContentLength(source.getHighestParent(), decorator, new AtomicBoolean());
    }

    private static int findDecoratorContentLength(Token token, TokenDecorator decorator, AtomicBoolean counting) {
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
                    boolean countingBefore = counting.get();
                    length += findDecoratorContentLength(child, decorator, counting);
                    if (child.shouldEncapsulate())
                        counting.set(countingBefore);
                }
            }
        }

        return length;
    }

}
