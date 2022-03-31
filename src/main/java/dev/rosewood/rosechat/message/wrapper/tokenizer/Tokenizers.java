package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosechat.message.wrapper.tokenizer.character.CharacterTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.color.ColorTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.channel.DiscordChannelTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.code.DiscordCodeTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.code.DiscordMultiCodeTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.emoji.DiscordCustomEmojiTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.emoji.DiscordEmojiTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.format.DiscordFormattingTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.quote.DiscordQuoteTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.spoiler.DiscordSpoilerTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.tag.DiscordTagTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.gradient.GradientTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.papi.PAPIPlaceholderTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.rosechat.RosechatFormattingTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.rosechat.RosechatPlaceholderTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.rainbow.RainbowTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.replacement.EmojiTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.replacement.RegexReplacementTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.tag.TagTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.whitespace.WhitespaceTokenizer;
import java.util.ArrayList;
import java.util.List;

public class Tokenizers {

    // Regular Tokenizers
    public static final RosechatFormattingTokenizer ROSECHAT_FORMATTING_TOKENIZER = new RosechatFormattingTokenizer(); // Formatting tokenizer, but without checking perms.
    public static final DiscordFormattingTokenizer DISCORD_FORMATTING_TOKENIZER = new DiscordFormattingTokenizer();
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

    // Discord Tokenizers
    public static final DiscordSpoilerTokenizer DISCORD_SPOILER_TOKENIZER = new DiscordSpoilerTokenizer();
    public static final DiscordCodeTokenizer DISCORD_CODE_TOKENIZER = new DiscordCodeTokenizer();
    public static final DiscordMultiCodeTokenizer DISCORD_MULTI_CODE_TOKENIZER = new DiscordMultiCodeTokenizer();
    public static final DiscordQuoteTokenizer DISCORD_QUOTE_TOKENIZER = new DiscordQuoteTokenizer();
    public static final DiscordEmojiTokenizer DISCORD_EMOJI_TOKENIZER = new DiscordEmojiTokenizer();
    public static final DiscordChannelTokenizer DISCORD_CHANNEL_TOKENIZER = new DiscordChannelTokenizer();
    public static final DiscordTagTokenizer DISCORD_TAG_TOKENIZER = new DiscordTagTokenizer();
    public static final DiscordCustomEmojiTokenizer DISCORD_CUSTOM_EMOJI_TOKENIZER = new DiscordCustomEmojiTokenizer();

    public static final List<Tokenizer<?>> DEFAULT_TOKENIZERS = new ArrayList<Tokenizer<?>>() {
        {
            add(GRADIENT_TOKENIZER);
            add(RAINBOW_TOKENIZER);
            add(COLOR_TOKENIZER);
            add(ROSECHAT_PLACEHOLDER_TOKENIZER);
            add(PAPI_PLACEHOLDER_TOKENIZER);
            add(EMOJI_TOKENIZER);
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
            add(GRADIENT_TOKENIZER);
            add(RAINBOW_TOKENIZER);
            add(COLOR_TOKENIZER);
            add(ROSECHAT_PLACEHOLDER_TOKENIZER);
            add(PAPI_PLACEHOLDER_TOKENIZER);
            add(DISCORD_EMOJI_TOKENIZER);
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
            add(DISCORD_CHANNEL_TOKENIZER);
            add(DISCORD_TAG_TOKENIZER);
            add(DISCORD_EMOJI_TOKENIZER);
            add(DISCORD_SPOILER_TOKENIZER);
            add(DISCORD_MULTI_CODE_TOKENIZER);
            add(DISCORD_CODE_TOKENIZER);
            add(DISCORD_QUOTE_TOKENIZER);
        }
    };

    public static final List<Tokenizer<?>> BLOCK_QUOTES_TOKENIZERS = new ArrayList<Tokenizer<?>>() {
        {
            add(DISCORD_CHANNEL_TOKENIZER);
            add(DISCORD_TAG_TOKENIZER);
            add(DISCORD_EMOJI_TOKENIZER);
            add(DISCORD_SPOILER_TOKENIZER);
            add(DISCORD_MULTI_CODE_TOKENIZER);
            add(DISCORD_CODE_TOKENIZER);
            addAll(DEFAULT_TOKENIZERS);
        }
    };

    public static final List<Tokenizer<?>> DEFAULT_WITH_DISCORD_TOKENIZERS = new ArrayList<Tokenizer<?>>() {
        {
            addAll(FROM_DISCORD_TOKENIZERS);
            add(GRADIENT_TOKENIZER);
            add(RAINBOW_TOKENIZER);
            add(COLOR_TOKENIZER);
            add(ROSECHAT_PLACEHOLDER_TOKENIZER);
            add(PAPI_PLACEHOLDER_TOKENIZER);
            add(EMOJI_TOKENIZER);
            add(TAG_TOKENIZER);
            add(REGEX_REPLACEMENT_TOKENIZER);
            add(WHITESPACE_TOKENIZER);
            add(CHARACTER_TOKENIZER);
        }
    };

    public static final List<Tokenizer<?>> DISCORD_FORMATTING_TOKENIZERS = new ArrayList<Tokenizer<?>>() {
        {
            add(DISCORD_FORMATTING_TOKENIZER);
            addAll(DEFAULT_WITH_DISCORD_TOKENIZERS);
        }
    };
    
    public static final List<Tokenizer<?>> TO_DISCORD_TOKENIZERS = new ArrayList<Tokenizer<?>>() {
        {
            add(DISCORD_FORMATTING_TOKENIZER);
            add(DISCORD_CUSTOM_EMOJI_TOKENIZER);
            add(GRADIENT_TOKENIZER);
            add(RAINBOW_TOKENIZER);
            add(ROSECHAT_PLACEHOLDER_TOKENIZER);
            add(PAPI_PLACEHOLDER_TOKENIZER);
            add(REGEX_REPLACEMENT_TOKENIZER);
            add(TAG_TOKENIZER);
            add(WHITESPACE_TOKENIZER);
            add(CHARACTER_TOKENIZER);
        }
    };
}
