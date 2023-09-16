package dev.rosewood.rosechat.message.tokenizer;

import dev.rosewood.rosechat.message.tokenizer.character.CharacterTokenizer;
import dev.rosewood.rosechat.message.tokenizer.discord.channel.FromDiscordChannelTokenizer;
import dev.rosewood.rosechat.message.tokenizer.discord.channel.ToDiscordChannelTokenizer;
import dev.rosewood.rosechat.message.tokenizer.markdown.MarkdownCodeTokenizer;
import dev.rosewood.rosechat.message.tokenizer.markdown.MarkdownCodeBlockTokenizer;
import dev.rosewood.rosechat.message.tokenizer.discord.emoji.DiscordCustomEmojiTokenizer;
import dev.rosewood.rosechat.message.tokenizer.discord.emoji.DiscordEmojiTokenizer;
import dev.rosewood.rosechat.message.tokenizer.markdown.MarkdownBlockQuoteTokenizer;
import dev.rosewood.rosechat.message.tokenizer.discord.spoiler.FromDiscordSpoilerTokenizer;
import dev.rosewood.rosechat.message.tokenizer.discord.spoiler.ToDiscordSpoilerTokenizer;
import dev.rosewood.rosechat.message.tokenizer.discord.tag.FromDiscordTagTokenizer;
import dev.rosewood.rosechat.message.tokenizer.discord.tag.ToDiscordTagTokenizer;
import dev.rosewood.rosechat.message.tokenizer.markdown.MarkdownBoldTokenizer;
import dev.rosewood.rosechat.message.tokenizer.markdown.MarkdownItalicTokenizer;
import dev.rosewood.rosechat.message.tokenizer.markdown.MarkdownStrikethroughTokenizer;
import dev.rosewood.rosechat.message.tokenizer.markdown.MarkdownUnderlineTokenizer;
import dev.rosewood.rosechat.message.tokenizer.placeholder.PAPIPlaceholderTokenizer;
import dev.rosewood.rosechat.message.tokenizer.placeholder.RoseChatPlaceholderTokenizer;
import dev.rosewood.rosechat.message.tokenizer.replacement.InlineReplacementTokenizer;
import dev.rosewood.rosechat.message.tokenizer.replacement.PrefixedReplacementTokenizer;
import dev.rosewood.rosechat.message.tokenizer.replacement.ReplacementTokenizer;
import dev.rosewood.rosechat.message.tokenizer.shader.ShaderTokenizer;
import dev.rosewood.rosechat.message.tokenizer.style.ColorTokenizer;
import dev.rosewood.rosechat.message.tokenizer.style.FormatTokenizer;
import dev.rosewood.rosechat.message.tokenizer.style.GradientTokenizer;
import dev.rosewood.rosechat.message.tokenizer.style.RainbowTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tokenizers {

    public static final Tokenizer TO_DISCORD_SPOILER = new ToDiscordSpoilerTokenizer();
    public static final Tokenizer FROM_DISCORD_SPOILER = new FromDiscordSpoilerTokenizer();
    public static final Tokenizer DISCORD_EMOJI = new DiscordEmojiTokenizer();
    public static final Tokenizer TO_DISCORD_TAG = new ToDiscordTagTokenizer();
    public static final Tokenizer FROM_DISCORD_TAG = new FromDiscordTagTokenizer();
    public static final Tokenizer TO_DISCORD_CHANNEL = new ToDiscordChannelTokenizer();
    public static final Tokenizer FROM_DISCORD_CHANNEL = new FromDiscordChannelTokenizer();
    public static final Tokenizer MARKDOWN_CODE_BLOCK = new MarkdownCodeBlockTokenizer();
    public static final Tokenizer MARKDOWN_CODE = new MarkdownCodeTokenizer();
    public static final Tokenizer MARKDOWN_BLOCK_QUOTE = new MarkdownBlockQuoteTokenizer();
    public static final Tokenizer DISCORD_CUSTOM_EMOJI = new DiscordCustomEmojiTokenizer();
    public static final Tokenizer MARKDOWN_BOLD = new MarkdownBoldTokenizer();
    public static final Tokenizer MARKDOWN_ITALIC = new MarkdownItalicTokenizer();
    public static final Tokenizer MARKDOWN_UNDERLINE = new MarkdownUnderlineTokenizer();
    public static final Tokenizer MARKDOWN_STRIKETHROUGH = new MarkdownStrikethroughTokenizer();
    public static final Tokenizer GRADIENT = new GradientTokenizer();
    public static final Tokenizer RAINBOW = new RainbowTokenizer();
    public static final Tokenizer SHADER_COLORS = new ShaderTokenizer();
    public static final Tokenizer COLOR = new ColorTokenizer();
    public static final Tokenizer FORMAT = new FormatTokenizer();
    public static final Tokenizer ROSECHAT_PLACEHOLDER = new RoseChatPlaceholderTokenizer();
    public static final Tokenizer PAPI_PLACEHOLDER = new PAPIPlaceholderTokenizer(false);
    public static final Tokenizer BUNGEE_PAPI_PLACEHOLDER = new PAPIPlaceholderTokenizer(true);
    public static final Tokenizer INLINE_REPLACEMENT = new InlineReplacementTokenizer();
    public static final Tokenizer PREFIXED_REPLACEMENT = new PrefixedReplacementTokenizer();
    public static final Tokenizer REPLACEMENT = new ReplacementTokenizer();
    public static final Tokenizer CHARACTER = new CharacterTokenizer();

    public static final TokenizerBundle DEFAULT_BUNDLE = new TokenizerBundle("default",
            ROSECHAT_PLACEHOLDER,
            PAPI_PLACEHOLDER,
            GRADIENT,
            RAINBOW,
            SHADER_COLORS,
            COLOR,
            FORMAT,
            INLINE_REPLACEMENT,
            PREFIXED_REPLACEMENT,
            REPLACEMENT,
            CHARACTER);
    public static final TokenizerBundle COLORS_BUNDLE = new TokenizerBundle("colors",
            GRADIENT,
            RAINBOW,
            COLOR,
            FORMAT);
    public static final TokenizerBundle MARKDOWN_BUNDLE = new TokenizerBundle("markdown",
            MARKDOWN_BOLD,
            MARKDOWN_ITALIC,
            MARKDOWN_UNDERLINE,
            MARKDOWN_STRIKETHROUGH);
    public static final TokenizerBundle BUNGEE_BUNDLE = new TokenizerBundle("bungee",
            ROSECHAT_PLACEHOLDER,
            BUNGEE_PAPI_PLACEHOLDER,
            GRADIENT,
            RAINBOW,
            SHADER_COLORS,
            COLOR,
            FORMAT,
            INLINE_REPLACEMENT,
            PREFIXED_REPLACEMENT,
            REPLACEMENT,
            CHARACTER);
    public static final TokenizerBundle DEFAULT_DISCORD_BUNDLE = new TokenizerBundle("default_discord",
            ROSECHAT_PLACEHOLDER,
            PAPI_PLACEHOLDER,
            GRADIENT,
            RAINBOW,
            SHADER_COLORS,
            COLOR,
            FORMAT,
            INLINE_REPLACEMENT,
            PREFIXED_REPLACEMENT,
            REPLACEMENT,
            CHARACTER);
    public static final TokenizerBundle DISCORD_FORMATTING_BUNDLE = new TokenizerBundle("discord_formatting",
            MARKDOWN_CODE_BLOCK,
            MARKDOWN_CODE,
            MARKDOWN_BLOCK_QUOTE);
    public static final TokenizerBundle TO_DISCORD_BUNDLE = new TokenizerBundle("to_discord",
            TO_DISCORD_SPOILER,
            TO_DISCORD_TAG,
            TO_DISCORD_CHANNEL,
            DISCORD_CUSTOM_EMOJI);
    public static final TokenizerBundle FROM_DISCORD_BUNDLE = new TokenizerBundle("from_discord",
            FROM_DISCORD_SPOILER,
            FROM_DISCORD_TAG,
            FROM_DISCORD_CHANNEL);
    public static final TokenizerBundle DISCORD_EMOJI_BUNDLE = new TokenizerBundle("discord_emoji",
            DISCORD_EMOJI);

    public record TokenizerBundle(String name, List<Tokenizer> tokenizers) {
        public TokenizerBundle(String name, Tokenizer... tokenizers) {
            this(name, new ArrayList<>(Arrays.asList(tokenizers)));
        }

        public void registerBefore(Tokenizer target, Tokenizer tokenizer) {
            int index = this.tokenizers.indexOf(target);
            if (index == -1)
                throw new IllegalArgumentException("Could not find tokenizer with name " + target + " in bundle " + this.name);
            this.tokenizers.add(index, tokenizer);
        }

        public void registerBefore(String before, Tokenizer tokenizer) {
            this.registerBefore(this.tokenizers.stream().filter(x -> x.getName().equals(before)).findFirst().orElse(null), tokenizer);
        }

        public void registerAfter(Tokenizer after, Tokenizer tokenizer) {
            int index = this.tokenizers.indexOf(after);
            if (index == -1)
                throw new IllegalArgumentException("Could not find tokenizer with name " + after + " in bundle " + this.name);
            this.tokenizers.add(index + 1, tokenizer);
        }

        public void registerAfter(String after, Tokenizer tokenizer) {
            this.registerAfter(this.tokenizers.stream().filter(x -> x.getName().equals(after)).findFirst().orElse(null), tokenizer);
        }
    }

}
