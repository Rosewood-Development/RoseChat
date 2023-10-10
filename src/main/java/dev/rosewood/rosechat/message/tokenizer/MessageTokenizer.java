package dev.rosewood.rosechat.message.tokenizer;

import com.google.common.base.Stopwatch;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.manager.DebugManager;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.composer.TokenComposer;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import dev.rosewood.rosechat.message.tokenizer.placeholder.RoseChatPlaceholderTokenizer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
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
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;

public class MessageTokenizer {

    private static final DebugManager DEBUG_MANAGER = RoseChat.getInstance().getManager(DebugManager.class);
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("#.##");

    private final List<Tokenizer> tokenizers;
    private final RoseMessage roseMessage;
    private final RosePlayer viewer;
    private final Token rootToken;
    private final MessageOutputs outputs;
    private int parses = 0;

    private MessageTokenizer(RoseMessage roseMessage, RosePlayer viewer, String format, Tokenizers.TokenizerBundle... tokenizerBundles) {
        this.roseMessage = roseMessage;
        this.viewer = viewer;
        this.tokenizers = zipperMerge(Arrays.stream(tokenizerBundles).map(Tokenizers.TokenizerBundle::tokenizers).toList());
        this.rootToken = Token.group(format).build();
        this.outputs = new MessageOutputs();
    }

    public MessageOutputs tokenizeContent() {
        this.tokenizeContent(this.rootToken, 0);
        return this.outputs;
    }

    public void tokenizeContent(Token token, int depth) {
        if (token.getType() != TokenType.GROUP)
            throw new IllegalStateException("Cannot tokenize a token that is not of type GROUP");

        if (depth > 15) {
            Deque<Token> tokens = new ArrayDeque<>();
            Token current = token;
            while (current != null) {
                tokens.addFirst(current);
                current = current.parent;
            }
            throw new RuntimeException("Exceeded a depth of 15 when tokenizing message; this is probably due to infinite recursion somewhere. Token stack: " + tokens.stream().map(t -> t.getType().name() + ":" + t.getContent()).collect(Collectors.joining(" -> ")));
        }

        String content = token.getContent();

        outer:
        while (!content.isEmpty()) {
            for (Tokenizer tokenizer : this.tokenizers) {
                if (token.getIgnoredTokenizers().contains(tokenizer))
                    continue;

                this.parses++;
                TokenizerParams params = new TokenizerParams(this.roseMessage, this.viewer, content, token.containsPlayerInput());
                TokenizerResult result = tokenizer.tokenize(params);
                if (result == null)
                    continue;

                Token child = result.token();
                child.parent = token;
                token.getChildren().add(child);
                content = content.substring(result.consumed());
                this.outputs.merge(params.getOutputs());
                continue outer;
            }
            throw new IllegalStateException(String.format("No tokenizer was able to tokenize the content: [%s]", content));
        }

        if (token.getType() == TokenType.GROUP)
            for (Token child : token.getChildren())
                if (child.getType() == TokenType.GROUP)
                    this.tokenizeContent(child, depth + 1);
    }

    public <T> T toComponents(TokenComposer<T> composer) {
        return this.toComponents(this.rootToken, composer);
    }

    public <T> T toComponents(Token token, TokenComposer<T> composer) {
        return composer.compose(token);
    }

    public static MessageTokenizerResults<BaseComponent[]> tokenize(RoseMessage roseMessage, RosePlayer viewer, String message, Tokenizers.TokenizerBundle... tokenizerBundles) {
        return tokenize(roseMessage, viewer, message, null, tokenizerBundles);
    }

    @SuppressWarnings("unchecked")
    public static <T> MessageTokenizerResults<T> tokenize(RoseMessage roseMessage, RosePlayer viewer, String message, TokenComposer<T> composer, Tokenizers.TokenizerBundle... tokenizerBundles) {
        if (message == null) {
            if (roseMessage.getPlayerInput() != null) {
                new RuntimeException("A null format was passed to the MessageTokenizer. The format has been replaced with {message} instead. A harmless stacktrace will be printed below so this can be fixed.").printStackTrace();
                message = RoseChatPlaceholderTokenizer.MESSAGE_PLACEHOLDER;
            } else {
                new RuntimeException("A null format was passed to the MessageTokenizer. The format has been replaced with an empty string instead. A harmless stacktrace will be printed below so this can be fixed.").printStackTrace();
                message = "";
            }
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        MessageTokenizer tokenizer = new MessageTokenizer(roseMessage, viewer, message, tokenizerBundles);

        if (composer == null) // a null composer means this will always be BaseComponent[], so this is safe
            composer = (TokenComposer<T>) TokenComposer.decorated(tokenizer);

        MessageOutputs outputs = tokenizer.tokenizeContent();
        T components = tokenizer.toComponents(composer);
        DEBUG_MANAGER.addMessage(() -> "Took " + NUMBER_FORMAT.format(stopwatch.elapsed(TimeUnit.NANOSECONDS) / 1000000.0) + "ms to tokenize " + countTokens(tokenizer.rootToken) + " tokens " + tokenizer.parses + " times");
        return new MessageTokenizerResults<T>(components, outputs);
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
                        length += child.getContent().length();
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
                    length += this.findDecoratorContentLength(child, decorator, counting);
                    if (child.shouldEncapsulate())
                        counting.set(countingBefore);
                }
            }
        }

        return length;
    }

}
