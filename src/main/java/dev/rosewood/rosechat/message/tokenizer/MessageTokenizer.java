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
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class MessageTokenizer {

    private final List<Tokenizer> tokenizers;
    private final RoseMessage roseMessage;
    private final RosePlayer viewer;
    private final Token rootToken;

    private MessageTokenizer(RoseMessage roseMessage, RosePlayer viewer, String message, String... tokenizerBundles) {
        this.roseMessage = roseMessage;
        this.viewer = viewer;
        this.tokenizers = zipperMerge(Arrays.stream(tokenizerBundles).map(Tokenizers::getBundleValues).collect(Collectors.toList()));
        this.rootToken = Token.group(message).build();

        this.tokenizeContent(this.rootToken, 0);
    }

    private static <T> List<T> zipperMerge(List<List<T>> listOfLists) {
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
        if (token.getType() != TokenType.GROUP)
            throw new IllegalStateException("Cannot tokenize a token that is not of type GROUP");

        if (depth > 15)
            throw new RuntimeException("Exceeded a depth of 15 when tokenizing message. This is probably due to infinite recursion somewhere: " + this.rootToken.getContent());

        String content = token.getContent();
        List<Token> children = new ArrayList<>();

        outer:
        while (!content.isEmpty()) {
            for (Tokenizer tokenizer : this.tokenizers) {
                TokenizerParams params = new TokenizerParams(this.roseMessage, this.viewer, content, token.containsPlayerInput());
                TokenizerResult result = tokenizer.tokenize(params);
                if (result == null)
                    continue;

                Token child = result.getToken();
                child.parent = token;
                children.add(child);
                content = content.substring(result.getConsumed());
                continue outer;
            }
            throw new IllegalStateException(String.format("No tokenizer was able to tokenize the content: [%s]", content));
        }

        token.setChildren(children);

        if (token.getType() == TokenType.GROUP)
            for (Token child : token.getChildren())
                if (child.getType() == TokenType.GROUP)
                    this.tokenizeContent(child, depth + 1);
    }

    public BaseComponent[] toComponents() {
        BaseComponent[] components = this.toComponents(this.rootToken, new TokenDecorators());
        RoseChat.getInstance().getLogger().warning(ComponentSerializer.toString(components));
        return components;
    }

    private BaseComponent[] toComponents(Token token, TokenDecorators contextDecorators) {
        ComponentBuilder componentBuilder = new ComponentBuilder();

        switch (token.getType()) {
            case TEXT -> {
                TokenDecorators textDecorators = contextDecorators.copyWith(token.getDecorators());
                componentBuilder.append(token.getContent(), FormatRetention.NONE);
                if (!token.getContent().trim().isEmpty()) // Don't color empty text
                    textDecorators.apply(componentBuilder);
            }
            case DECORATOR -> contextDecorators.add(token.getDecorators());
            case GROUP -> {
                if (token.getDecorators().isEmpty()) {
                    for (Token child : token.getChildren())
                        for (BaseComponent component : this.toComponents(child, contextDecorators))
                            componentBuilder.append(component, FormatRetention.NONE);

                    contextDecorators.apply(componentBuilder);
                } else {
                    TextComponent groupComponent = new TextComponent();
                    for (Token child : token.getChildren())
                        for (BaseComponent component : this.toComponents(child, contextDecorators))
                            groupComponent.addExtra(component);

                    componentBuilder.append(groupComponent, FormatRetention.NONE);
                    contextDecorators.apply(componentBuilder);
                }
            }
        }

        return componentBuilder.create();
    }

    public static BaseComponent[] tokenize(RoseMessage roseMessage, RosePlayer viewer, String message, String... tokenizerBundles) {
        return new MessageTokenizer(roseMessage, viewer, message, tokenizerBundles).toComponents();
    }

}
