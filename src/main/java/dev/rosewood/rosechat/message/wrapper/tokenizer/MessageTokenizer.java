package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.wrapper.ComponentSimplifier;
import dev.rosewood.rosechat.message.wrapper.tokenizer.character.CharacterTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.color.ColorTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.gradient.GradientTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.rainbow.RainbowTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.test.TestTokenizer;
import dev.rosewood.rosegarden.utils.HexUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
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
                new TestTokenizer(),
                new CharacterTokenizer()
        ));
    }

    private final MessageWrapper messageWrapper;
    private final List<Token> tokens;

    public MessageTokenizer(MessageWrapper messageWrapper, String message) {
        this.messageWrapper = messageWrapper;
        this.tokens = new ArrayList<>();
        this.tokenize(parseReplacements(message));
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
        for (int i = 0; i < message.length(); i++) {
            String substring = message.substring(i);
            for (Tokenizer<?> tokenizer : tokenizers) {
                Token token = tokenizer.tokenize(this.messageWrapper, substring);
                if (token != null) {
                    this.tokens.add(token);
                    i += token.getOriginalContent().length() - 1;
                    break;
                }
            }
        }

        this.tokens.forEach(x -> RoseChat.getInstance().getLogger().info(x.getClass().getSimpleName() + ": " + x.getOriginalContent()));
    }

    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        HexUtils.ColorGenerator colorGenerator = null;
        for (int i = 0; i < this.tokens.size(); i++) {
            Token token = this.tokens.get(i);
            if (token.hasColorGenerator())
                colorGenerator = token.getColorGenerator(this.messageWrapper, this.tokens.subList(i, this.tokens.size()));

            if (colorGenerator == null) {
                componentBuilder.append(token.getText(this.messageWrapper), ComponentBuilder.FormatRetention.NONE);

                String hover = token.getHover(this.messageWrapper);
                if (hover != null)
                    componentBuilder.event(new HoverEvent(token.getHoverAction(), TextComponent.fromLegacyText(hover)));

                String click = token.getClick(this.messageWrapper);
                if (click != null)
                    componentBuilder.event(new ClickEvent(token.getClickAction(), click));
            } else {
                for (char c : token.getText(this.messageWrapper).toCharArray()) {
                    componentBuilder.append(String.valueOf(c), ComponentBuilder.FormatRetention.NONE);
                    componentBuilder.color(colorGenerator.nextChatColor());

                    String hover = token.getHover(this.messageWrapper);
                    if (hover != null)
                        componentBuilder.event(new HoverEvent(token.getHoverAction(), TextComponent.fromLegacyText(hover)));

                    String click = token.getClick(this.messageWrapper);
                    if (click != null)
                        componentBuilder.event(new ClickEvent(token.getClickAction(), click));
                }
            }
        }

        // Appends an empty string to always have something in the component.
        if (componentBuilder.getParts().isEmpty()) componentBuilder.append("", ComponentBuilder.FormatRetention.NONE);
        BaseComponent[] components = componentBuilder.create();

        Bukkit.broadcastMessage(ComponentSerializer.toString(components));

        return ComponentSimplifier.simplify(components);
        //return components;
    }

}
