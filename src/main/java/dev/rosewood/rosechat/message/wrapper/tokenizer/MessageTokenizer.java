package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.ComponentSimplifier;
import dev.rosewood.rosechat.message.wrapper.tokenizer.character.CharacterTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.color.ColorTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.gradient.GradientTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.PAPIPlaceholderTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.RoseChatPlaceholderTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.rainbow.RainbowTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.replacement.EmojiTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.replacement.RegexReplacementTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.tag.TagTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.url.URLTokenizer;
import dev.rosewood.rosegarden.utils.HexUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageTokenizer {

    private static final List<Tokenizer<?>> tokenizers;
    static {
        tokenizers = new ArrayList<>(Arrays.asList(
                new GradientTokenizer(),
                new RainbowTokenizer(),
                new ColorTokenizer(),
                new URLTokenizer(),
                new RoseChatPlaceholderTokenizer(),
                new PAPIPlaceholderTokenizer(),
                new EmojiTokenizer(),
                new TagTokenizer(),
                new RegexReplacementTokenizer(),
                new CharacterTokenizer()
        ));
    }

    private final MessageWrapper messageWrapper;
    private final RoseSender viewer;
    private final List<Token> tokens;

    public MessageTokenizer(MessageWrapper messageWrapper, RoseSender viewer, String message) {
        this.messageWrapper = messageWrapper;
        this.viewer = viewer;
        this.tokens = new ArrayList<>();
        this.tokenize(this.parseReplacements(message));
    }

    private String parseReplacements(String message) {
        for (ChatReplacement replacement : RoseChatAPI.getInstance().getReplacements()) {
            if (replacement.isRegex() || !message.contains(replacement.getText())) continue;
            String groupPermission = this.messageWrapper.getGroup() == null ? "" : "." + this.messageWrapper.getGroup().getLocationPermission();
            if (this.messageWrapper.getLocation() != MessageLocation.NONE
                    && !this.messageWrapper.getSender().hasPermission("rosechat.replacements." + this.messageWrapper.getLocation().toString().toLowerCase() + groupPermission)
                    || !this.messageWrapper.getSender().hasPermission("rosechat.replacement." + replacement.getId())) continue;
            message = message.replace(replacement.getText(), replacement.getReplacement());
        }

        return message;
    }

    private void tokenize(String message) {
        this.tokens.clear();
        this.tokens.addAll(this.tokenizeContent(message, 0));
    }

    private List<Token> tokenizeContent(String content, int depth) {
        List<Token> added = new ArrayList<>();
        for (int i = 0; i < content.length(); i++) {
            String substring = content.substring(i);
            for (Tokenizer<?> tokenizer : tokenizers) {
                Token token = tokenizer.tokenize(this.messageWrapper, this.viewer, substring);
                if (token != null) {
                    i += token.getOriginalContent().length() - 1;
                    if (token.requiresTokenizing() && depth < 10) {
                        added.add(token);
                        List<Token> generated = this.tokenizeContent(token.getContent(), depth + 1);
                        token.addChildren(generated);
                    } else {
                        added.add(token);
                    }
                    break;
                }
            }
        }
        return added;
    }

    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        HexUtils.ColorGenerator colorGenerator = null;
        this.toComponents(componentBuilder, colorGenerator, this.tokens);

        // Appends an empty string to always have something in the component.
        if (componentBuilder.getParts().isEmpty()) componentBuilder.append("", ComponentBuilder.FormatRetention.NONE);
        BaseComponent[] components = componentBuilder.create();

        return ComponentSimplifier.simplify(components);
    }

    public void toComponents(ComponentBuilder componentBuilder, HexUtils.ColorGenerator colorGenerator, List<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (!token.getChildren().isEmpty()) {
                this.toComponents(componentBuilder, colorGenerator, token.getChildren());
            } else {
                if (token.hasColorGenerator())
                    colorGenerator = token.getColorGenerator(tokens.subList(i, tokens.size()));

                if (colorGenerator == null) {
                    componentBuilder.append(token.getContent(), ComponentBuilder.FormatRetention.NONE).font(token.getFont());

                    String hover = token.getHover();
                    if (hover != null)
                        componentBuilder.event(new HoverEvent(token.getHoverAction(), TextComponent.fromLegacyText(hover)));

                    String click = token.getClick();
                    if (click != null)
                        componentBuilder.event(new ClickEvent(token.getClickAction(), click));
                } else {
                    for (char c : token.getContent().toCharArray()) {
                        componentBuilder.append(String.valueOf(c), ComponentBuilder.FormatRetention.NONE).font(token.getFont());
                        componentBuilder.color(colorGenerator.nextChatColor());

                        String hover = token.getHover();
                        if (hover != null)
                            componentBuilder.event(new HoverEvent(token.getHoverAction(), TextComponent.fromLegacyText(hover)));

                        String click = token.getClick();
                        if (click != null)
                            componentBuilder.event(new ClickEvent(token.getClickAction(), click));
                    }
                }
            }
        }
    }

}
