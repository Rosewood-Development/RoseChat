package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.ComponentColorizer;
import dev.rosewood.rosechat.message.wrapper.ComponentSimplifier;
import dev.rosewood.rosechat.message.wrapper.tokenizer.character.CharacterTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.color.ColorTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.from.FromDiscordTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.to.ToDiscordTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.gradient.GradientTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.papi.PAPIPlaceholderTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.rosechat.RosechatFormattingTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.rosechat.RosechatPlaceholderTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.rainbow.RainbowTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.replacement.RegexReplacementTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.replacement.ReplacementToken;
import dev.rosewood.rosechat.message.wrapper.tokenizer.replacement.ReplacementTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.tag.TagTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.whitespace.WhitespaceTokenizer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageTokenizer {

    public static final RosechatFormattingTokenizer ROSECHAT_FORMATTING_TOKENIZER = new RosechatFormattingTokenizer();
    public static final GradientTokenizer GRADIENT_TOKENIZER = new GradientTokenizer();
    public static final RainbowTokenizer RAINBOW_TOKENIZER = new RainbowTokenizer();
    public static final ColorTokenizer COLOR_TOKENIZER = new ColorTokenizer();
    public static final ToDiscordTokenizer TO_DISCORD_TOKENIZER = new ToDiscordTokenizer();
    public static final FromDiscordTokenizer FROM_DISCORD_TOKENIZER = new FromDiscordTokenizer();
    public static final PAPIPlaceholderTokenizer PAPI_PLACEHOLDER_TOKENIZER = new PAPIPlaceholderTokenizer();
    public static final RosechatPlaceholderTokenizer ROSECHAT_PLACEHOLDER_TOKENIZER = new RosechatPlaceholderTokenizer();
    public static final TagTokenizer TAG_TOKENIZER = new TagTokenizer();
    public static final RegexReplacementTokenizer REGEX_REPLACEMENT_TOKENIZER = new RegexReplacementTokenizer();
    public static final ReplacementTokenizer REPLACEMENT_TOKENIZER = new ReplacementTokenizer();
    public static final WhitespaceTokenizer WHITESPACE_TOKENIZER = new WhitespaceTokenizer();
    public static final CharacterTokenizer CHARACTER_TOKENIZER = new CharacterTokenizer();
    public static final List<Tokenizer<?>> DEFAULT_TOKENIZERS = new ArrayList<Tokenizer<?>>() {
        {
            add(REPLACEMENT_TOKENIZER);
            add(GRADIENT_TOKENIZER);
            add(RAINBOW_TOKENIZER);
            add(COLOR_TOKENIZER);
            add(ROSECHAT_PLACEHOLDER_TOKENIZER);
            add(PAPI_PLACEHOLDER_TOKENIZER);
            add(TAG_TOKENIZER);
            add(REGEX_REPLACEMENT_TOKENIZER);
            add(WHITESPACE_TOKENIZER);
            add(CHARACTER_TOKENIZER);
        }
    };

    public static final List<Tokenizer<?>> FORMATTING_TOKENIZERS = new ArrayList<Tokenizer<?>>() {
        {
            add(ROSECHAT_FORMATTING_TOKENIZER);
            addAll(DEFAULT_TOKENIZERS);
        }
    };

    public static final List<Tokenizer<?>> TAG_TOKENIZERS = new ArrayList<Tokenizer<?>>() {
        {
            add(REPLACEMENT_TOKENIZER);
            add(ROSECHAT_FORMATTING_TOKENIZER);
            add(GRADIENT_TOKENIZER);
            add(RAINBOW_TOKENIZER);
            add(COLOR_TOKENIZER);
            add(ROSECHAT_PLACEHOLDER_TOKENIZER);
            add(REGEX_REPLACEMENT_TOKENIZER);
            add(WHITESPACE_TOKENIZER);
            add(CHARACTER_TOKENIZER);
        }
    };

    private final MessageWrapper messageWrapper;
    private final Group group;
    private final RoseSender sender;
    private final RoseSender viewer;
    private final MessageLocation location;
    private final List<Tokenizer<?>> tokenizers;
    private final List<Token> tokens;

    public MessageTokenizer(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String message, List<Tokenizer<?>> tokenizers) {
        this.messageWrapper = messageWrapper;
        this.group = group;
        this.sender = sender;
        this.viewer = viewer;
        this.location = location;
        this.tokenizers = tokenizers;
        this.tokens = new ArrayList<>();
        this.tokenize(message);
    }

    public MessageTokenizer(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String message, Tokenizer<?>... tokenizers) {
        this(messageWrapper, group, sender, viewer, location, message, Arrays.asList(tokenizers));
    }

    public MessageTokenizer(MessageWrapper messageWrapper, Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String message) {
        this(messageWrapper, group, sender, viewer, location, message, DEFAULT_TOKENIZERS);
    }

    public MessageTokenizer(Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String message, List<Tokenizer<?>> tokenizers) {
        this(null, group, sender, viewer, location, message, tokenizers);
    }

    public MessageTokenizer(Group group, RoseSender sender, RoseSender viewer, MessageLocation location, String message, Tokenizer<?>... tokenizers) {
        this(null, group, sender, viewer, location, message, Arrays.asList(tokenizers));
    }

    public MessageTokenizer(RoseSender sender, RoseSender viewer, MessageLocation location, String message) {
        this(null, null, sender, viewer, location, message, DEFAULT_TOKENIZERS);
    }

    private void tokenize(String message) {
        for (int i = 0; i < message.length(); i++) {
            String substring = message.substring(i);
            for (Tokenizer<?> tokenizer : tokenizers) {
                Token token = tokenizer.tokenize(this.messageWrapper, this.group, this.sender, this.viewer, this.location, substring);
                if (token != null) {
                    this.tokens.add(token);
                    i += token.getOriginalContent().length() - 1;
                    break;
                }
            }
        }
    }

    public BaseComponent[] toComponents() {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        for (Token token : this.tokens) {
            String font = "default";

            if (token instanceof ReplacementToken)
                font = ((ReplacementToken) token).getFont();

            BaseComponent[] components = token.toComponents();
            if (components != null && components.length > 0) componentBuilder.append(components, ComponentBuilder.FormatRetention.NONE).font(font);
        }

        // Appends an empty string to always have something in the component.
        componentBuilder.append("", ComponentBuilder.FormatRetention.NONE);
        BaseComponent[] colorized = ComponentColorizer.colorize(componentBuilder.create());

        return ComponentSimplifier.simplify(colorized);
    }
}
