package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.ComponentColorizer;
import dev.rosewood.rosechat.message.wrapper.ComponentSimplifier;
import dev.rosewood.rosechat.message.wrapper.tokenizer.character.CharacterTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.color.ColorTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.bold.DiscordBoldTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.code.DiscordCodeTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.code.DiscordMultiCodeTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.emoji.DiscordEmojiTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.format.DiscordFormattingTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.italic.DiscordItalicTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.quote.DiscordQuoteTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.spoiler.DiscordSpoilerTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.strikethrough.DiscordStrikethroughTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.underline.DiscordUnderlineTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.gradient.GradientTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.papi.PAPIPlaceholderTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.rosechat.RosechatFormattingTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.rosechat.RosechatPlaceholderTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.rainbow.RainbowTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.replacement.EmojiTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.replacement.RegexReplacementTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.replacement.ReplacementToken;
import dev.rosewood.rosechat.message.wrapper.tokenizer.tag.TagTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.whitespace.WhitespaceTokenizer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageTokenizer {

    private final Pattern DISCORD_MARKDOWN = Pattern.compile("\\*(.*)\\*|_(.*)_|~~(.*)~~|`(.*)`|\\|\\|(.*)\\|\\|", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    public static final RosechatFormattingTokenizer ROSECHAT_FORMATTING_TOKENIZER = new RosechatFormattingTokenizer(); // Ignores permissions.
    public static final DiscordFormattingTokenizer DISCORD_FORMATTING_TOKENIZER = new DiscordFormattingTokenizer(); // Formatting, but with optional other tokenizers.
    public static final GradientTokenizer GRADIENT_TOKENIZER = new GradientTokenizer();
    public static final RainbowTokenizer RAINBOW_TOKENIZER = new RainbowTokenizer();
    public static final ColorTokenizer COLOR_TOKENIZER = new ColorTokenizer();
    public static final PAPIPlaceholderTokenizer PAPI_PLACEHOLDER_TOKENIZER = new PAPIPlaceholderTokenizer();
    public static final RosechatPlaceholderTokenizer ROSECHAT_PLACEHOLDER_TOKENIZER = new RosechatPlaceholderTokenizer();
    public static final TagTokenizer TAG_TOKENIZER = new TagTokenizer();
    public static final RegexReplacementTokenizer REGEX_REPLACEMENT_TOKENIZER = new RegexReplacementTokenizer();
    public static final EmojiTokenizer EMOJI_TOKENIZER = new EmojiTokenizer();
    public static final WhitespaceTokenizer WHITESPACE_TOKENIZER = new WhitespaceTokenizer();
    public static final CharacterTokenizer CHARACTER_TOKENIZER = new CharacterTokenizer();

    public static final DiscordBoldTokenizer DISCORD_BOLD_TOKENIZER = new DiscordBoldTokenizer();
    public static final DiscordItalicTokenizer DISCORD_ITALIC_TOKENIZER = new DiscordItalicTokenizer();
    public static final DiscordUnderlineTokenizer DISCORD_UNDERLINE_TOKENIZER = new DiscordUnderlineTokenizer();
    public static final DiscordStrikethroughTokenizer DISCORD_STRIKETHROUGH_TOKENIZER = new DiscordStrikethroughTokenizer();
    public static final DiscordSpoilerTokenizer DISCORD_SPOILER_TOKENIZER = new DiscordSpoilerTokenizer();
    public static final DiscordCodeTokenizer DISCORD_CODE_TOKENIZER = new DiscordCodeTokenizer();
    public static final DiscordMultiCodeTokenizer DISCORD_MULTI_CODE_TOKENIZER = new DiscordMultiCodeTokenizer();
    public static final DiscordQuoteTokenizer DISCORD_QUOTE_TOKENIZER = new DiscordQuoteTokenizer();
    public static final DiscordEmojiTokenizer DISCORD_EMOJI_TOKENIZER = new DiscordEmojiTokenizer();

    public static final List<Tokenizer<?>> DEFAULT_TOKENIZERS = new ArrayList<Tokenizer<?>>() {
        {
            add(GRADIENT_TOKENIZER);
            add(RAINBOW_TOKENIZER);
            add(COLOR_TOKENIZER);
            add(ROSECHAT_PLACEHOLDER_TOKENIZER);
            add(PAPI_PLACEHOLDER_TOKENIZER);
            add(TAG_TOKENIZER);
            add(EMOJI_TOKENIZER);
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
            add(GRADIENT_TOKENIZER);
            add(RAINBOW_TOKENIZER);
            add(COLOR_TOKENIZER);
            add(ROSECHAT_PLACEHOLDER_TOKENIZER);
            add(PAPI_PLACEHOLDER_TOKENIZER);
            add(EMOJI_TOKENIZER);
            add(REGEX_REPLACEMENT_TOKENIZER);
            add(WHITESPACE_TOKENIZER);
            add(CHARACTER_TOKENIZER);
        }
    };

    public static final List<Tokenizer<?>> REPLACEMENT_TOKENIZERS = new ArrayList<Tokenizer<?>>() {
        {
            add(GRADIENT_TOKENIZER);
            add(RAINBOW_TOKENIZER);
            add(COLOR_TOKENIZER);
            add(ROSECHAT_PLACEHOLDER_TOKENIZER);
            add(PAPI_PLACEHOLDER_TOKENIZER);
            add(EMOJI_TOKENIZER);
            add(WHITESPACE_TOKENIZER);
            add(CHARACTER_TOKENIZER);
        }
    };

    public static final List<Tokenizer<?>> FROM_DISCORD_TOKENIZERS = new ArrayList<Tokenizer<?>>() {
        {
            add(PAPI_PLACEHOLDER_TOKENIZER);
            add(DISCORD_EMOJI_TOKENIZER);
            add(DISCORD_BOLD_TOKENIZER);
            add(DISCORD_UNDERLINE_TOKENIZER);
            add(DISCORD_STRIKETHROUGH_TOKENIZER);
            add(DISCORD_SPOILER_TOKENIZER);
            add(DISCORD_MULTI_CODE_TOKENIZER);
            add(DISCORD_CODE_TOKENIZER);
            add(DISCORD_QUOTE_TOKENIZER);
            add(DISCORD_ITALIC_TOKENIZER);
            add(DISCORD_FORMATTING_TOKENIZER);
            add(WHITESPACE_TOKENIZER);
            add(CHARACTER_TOKENIZER);
        }
    };

    public static final List<Tokenizer<?>> TO_DISCORD_TOKENIZERS = new ArrayList<Tokenizer<?>>() {
        {
            add(REGEX_REPLACEMENT_TOKENIZER);
            add(DISCORD_FORMATTING_TOKENIZER);
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

        this.tokenize(this.parseReplacements(message));
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

    public MessageTokenizer(RoseSender sender, RoseSender viewer, MessageLocation location, String message, List<Tokenizer<?>> tokenizers) {
        this(null, null, sender, viewer, location, message, tokenizers);
    }

    public MessageTokenizer(RoseSender sender, RoseSender viewer, MessageLocation location, String message) {
        this(null, null, sender, viewer, location, message, DEFAULT_TOKENIZERS);
    }

    private String parseReplacements(String message) {
        for (ChatReplacement replacement : RoseChatAPI.getInstance().getReplacements()) {
            if (replacement.isRegex()) continue;
            String groupPermission = this.group == null ? "" : "." + this.group.getLocationPermission();
            if (this.location != MessageLocation.NONE && !this.sender.hasPermission("rosechat.replacements." + this.location.toString().toLowerCase() + groupPermission)
                    || !this.sender.hasPermission("rosechat.replacement." + replacement.getId())) continue;
            message = message.replace(replacement.getText(), replacement.getReplacement());
        }

        return message;
    }

    private void tokenize(String message) {
        this.tokens.clear();
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

    public BaseComponent[] fromString(List<Tokenizer<?>> finalTokenizers) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Token token : this.tokens) {
            if (token != null) stringBuilder.append(token.asString());
        }

        String output = stringBuilder.toString();

        Matcher matcher = this.DISCORD_MARKDOWN.matcher(output);

        // Do it again! and again! and again!
        // Make sure the player has formatting permissions.
        if (matcher.find() && (this.location != MessageLocation.NONE && this.sender.hasPermission("rosechat.discord." + this.group.getLocationPermission()))) {
            this.tokens.clear();
            tokenize(output);
            return this.fromString(finalTokenizers);
        }

        return new MessageTokenizer(this.messageWrapper, this.group, this.sender, this.viewer, this.location, output, finalTokenizers).toComponents();
    }
}
