package dev.rosewood.rosechat.message.tokenizer;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class PlayerMessageTokenizer implements MessageTokenizer {

    private final List<Tokenizer> tokenizers;
    private final RoseMessage roseMessage;
    private final RosePlayer viewer;
    private final Token rootToken;

    public PlayerMessageTokenizer(RoseMessage roseMessage, RosePlayer viewer, String message, boolean ignorePermissions, String... tokenizerBundles) {
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
        if (content.length() <= 1)
            return;

        outer:
        while (!content.isEmpty()) {
            for (Tokenizer tokenizer : this.tokenizers) {
                TokenizerParams params = new TokenizerParams(this.roseMessage, this.viewer, content);
                TokenizerResult result = tokenizer.tokenize(params);
                if (result != null) {
                    token.addChild(result.getToken());
                    content = content.substring(result.getConsumed());
                    continue outer;
                }
            }
            throw new IllegalStateException(String.format("No tokenizer was able to tokenize the content: [%s]", content));
        }

        for (Token child : token.getChildren())
            this.tokenizeContent(child, depth + 1);
    }

    @Override
    public BaseComponent[] toComponents() {
        return this.toComponents(this.rootToken);
    }

    private BaseComponent[] toComponents(Token token) {
        ComponentBuilder componentBuilder = new ComponentBuilder();

        for (Token child : token.getChildren()) {
            if (child.isResolved()) {
                componentBuilder.append(child.getContent());
            } else {
                componentBuilder.append(this.toComponents(child));
            }
        }

        return componentBuilder.create();
    }

}
