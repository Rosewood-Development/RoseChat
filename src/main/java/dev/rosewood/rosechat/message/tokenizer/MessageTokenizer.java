package dev.rosewood.rosechat.message.tokenizer;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.chat.ComponentSerializer;

public class MessageTokenizer {

    private final List<Tokenizer> tokenizers;
    private final RoseMessage roseMessage;
    private final RosePlayer viewer;
    private final Token rootToken;

    public MessageTokenizer(RoseMessage roseMessage, RosePlayer viewer, String message, String... tokenizerBundles) {
        this.roseMessage = roseMessage;
        this.viewer = viewer;
        this.tokenizers = zipperMerge(Arrays.stream(tokenizerBundles).map(Tokenizers::getBundleValues).collect(Collectors.toList()));
        this.rootToken = Token.builder().content(message).build();

        this.tokenizeContent(this.rootToken, 0);
    }

    public static <T> List<T> zipperMerge(List<List<T>> listOfLists) {
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

    private void tokenizeContent(Token token, int depth) {
        if (depth > 15)
            throw new RuntimeException("Exceeded a depth of 15 when tokenizing message. This is probably due to infinite recursion somewhere: " + this.rootToken.getContent());

        String content = token.getContent();
        if (content.isEmpty())
            throw new IllegalStateException("Token with empty content was not marked as resolved");

        outer:
        while (!content.isEmpty()) {
            for (Tokenizer tokenizer : this.tokenizers) {
                TokenizerParams params = new TokenizerParams(this.roseMessage, this.viewer, content, token.containsPlayerInput());
                TokenizerResult result = tokenizer.tokenize(params);
                if (result != null) {
                    Token child = result.getToken();
                    child.parent = token;
                    token.children.add(child);
                    content = content.substring(result.getConsumed());
                    continue outer;
                }
            }
            throw new IllegalStateException(String.format("No tokenizer was able to tokenize the content: [%s]", content));
        }

        for (Token child : token.getChildren())
            if (!child.isResolved())
                this.tokenizeContent(child, depth + 1);
    }

    public BaseComponent[] toComponents() {
        BaseComponent[] components = this.toComponents(this.rootToken, new TokenDecorators());
        RoseChat.getInstance().getLogger().warning(ComponentSerializer.toString(components));
        return components;
    }

    private BaseComponent[] toComponents(Token token, TokenDecorators contextDecorators) {
        ComponentBuilder componentBuilder = new ComponentBuilder();

        if (!token.hasChildren()) {
            contextDecorators.add(token.getDecorators());

            if (token.hasContent()) {
                componentBuilder.append(token.getContent(), FormatRetention.NONE);
                contextDecorators.apply(componentBuilder);
            }

            return componentBuilder.create();
        }

        StringBuilder builder = new StringBuilder();
        for (Token child : token.getChildren()) {
            if (child.hasContent() && !child.hasDecorators() && !child.hasChildren()) {
                builder.append(child.getContent());
                continue;
            }

            if (builder.length() > 0) {
                componentBuilder.append(builder.toString(), FormatRetention.NONE);
                builder.setLength(0);
                contextDecorators.apply(componentBuilder);
            }

            contextDecorators.add(child.getDecorators());

            if (child.hasChildren()) {
                BaseComponent[] components = this.toComponents(child, contextDecorators);
                if (components.length > 0)
                    componentBuilder.append(components, FormatRetention.NONE);
            } else if (child.hasContent()) {
                componentBuilder.append(child.getContent(), FormatRetention.NONE);
                contextDecorators.apply(componentBuilder);
            }
        }

        if (builder.length() > 0) {
            componentBuilder.append(builder.toString(), FormatRetention.NONE);
            contextDecorators.apply(componentBuilder);
        }

        return componentBuilder.create();
    }

}
