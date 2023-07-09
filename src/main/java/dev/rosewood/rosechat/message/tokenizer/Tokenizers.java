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
import dev.rosewood.rosechat.message.tokenizer.markdown.MarkdownURLTokenizer;
import dev.rosewood.rosechat.message.tokenizer.markdown.MarkdownUnderlineTokenizer;
import dev.rosewood.rosechat.message.tokenizer.placeholder.PAPIPlaceholderTokenizer;
import dev.rosewood.rosechat.message.tokenizer.placeholder.RoseChatPlaceholderTokenizer;
import dev.rosewood.rosechat.message.tokenizer.replacement.EmojiTokenizer;
import dev.rosewood.rosechat.message.tokenizer.replacement.RegexReplacementTokenizer;
import dev.rosewood.rosechat.message.tokenizer.replacement.ReplacementTokenizer;
import dev.rosewood.rosechat.message.tokenizer.shader.ShaderTokenizer;
import dev.rosewood.rosechat.message.tokenizer.style.ColorTokenizer;
import dev.rosewood.rosechat.message.tokenizer.style.FormatTokenizer;
import dev.rosewood.rosechat.message.tokenizer.style.GradientTokenizer;
import dev.rosewood.rosechat.message.tokenizer.style.RainbowTokenizer;
import dev.rosewood.rosechat.message.tokenizer.tag.TagTokenizer;
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
    public static final Tokenizer MARKDOWN_URL = new MarkdownURLTokenizer();
    public static final Tokenizer GRADIENT = new GradientTokenizer();
    public static final Tokenizer RAINBOW = new RainbowTokenizer();
    public static final Tokenizer SHADER_COLORS = new ShaderTokenizer();
    public static final Tokenizer COLOR = new ColorTokenizer();
    public static final Tokenizer FORMAT = new FormatTokenizer();
    public static final Tokenizer ROSECHAT_PLACEHOLDER = new RoseChatPlaceholderTokenizer();
    public static final Tokenizer PAPI_PLACEHOLDER = new PAPIPlaceholderTokenizer(false);
    public static final Tokenizer BUNGEE_PAPI_PLACEHOLDER = new PAPIPlaceholderTokenizer(true);
    public static final Tokenizer EMOJI = new EmojiTokenizer();
    public static final Tokenizer TAG = new TagTokenizer();
    public static final Tokenizer REGEX_REPLACEMENT = new RegexReplacementTokenizer();
    public static final Tokenizer REPLACEMENT = new ReplacementTokenizer();
    public static final Tokenizer CHARACTER = new CharacterTokenizer();

    public static final TokenizerBundle DEFAULT_BUNDLE = new TokenizerBundle("default",
            GRADIENT,
            RAINBOW,
            SHADER_COLORS,
            COLOR,
            FORMAT,
            ROSECHAT_PLACEHOLDER,
            PAPI_PLACEHOLDER,
            EMOJI,
            TAG,
            REGEX_REPLACEMENT,
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
            MARKDOWN_STRIKETHROUGH,
            MARKDOWN_URL);
    public static final TokenizerBundle BUNGEE_BUNDLE = new TokenizerBundle("bungee",
            GRADIENT,
            RAINBOW,
            SHADER_COLORS,
            COLOR,
            FORMAT,
            ROSECHAT_PLACEHOLDER,
            BUNGEE_PAPI_PLACEHOLDER,
            EMOJI,
            TAG,
            REGEX_REPLACEMENT,
            REPLACEMENT,
            CHARACTER);
    public static final TokenizerBundle DEFAULT_DISCORD_BUNDLE = new TokenizerBundle("default_discord",
            GRADIENT,
            RAINBOW,
            SHADER_COLORS,
            COLOR,
            FORMAT,
            ROSECHAT_PLACEHOLDER,
            PAPI_PLACEHOLDER,
            EMOJI,
            TAG,
            REGEX_REPLACEMENT,
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

//    public static Tokenizer registerAfter(String after, String name, Tokenizer tokenizer, TokenizerBundle... bundles) {
//        if (bundles.length == 0)
//            bundles = new TokenizerBundle[] { DEFAULT_BUNDLE };
//        for (TokenizerBundle bundle : bundles) {
//            List<TokenizerEntry> tokenizerEntries = (List<TokenizerEntry>) TOKENIZERS.get(bundle);
//            int index = tokenizerEntries.indexOf(tokenizerEntries.stream().filter(tokenizerEntry -> tokenizerEntry.name().equals(after)).findFirst().orElse(null));
//            if (index == -1)
//                throw new IllegalArgumentException("Could not find tokenizer with name " + after + " in bundle " + bundle);
//            tokenizerEntries.add(index + 1, new TokenizerEntry(name, tokenizer));
//        }
//        return tokenizer;
//    }
//
//    public static Tokenizer registerBefore(String before, String name, Tokenizer tokenizer, TokenizerBundle... bundles) {
//        if (bundles.length == 0)
//            bundles = new TokenizerBundle[] { DEFAULT_BUNDLE };
//        for (TokenizerBundle bundle : bundles) {
//            List<TokenizerEntry> tokenizerEntries = (List<TokenizerEntry>) TOKENIZERS.get(bundle);
//            int index = tokenizerEntries.indexOf(tokenizerEntries.stream().filter(tokenizerEntry -> tokenizerEntry.name().equals(before)).findFirst().orElse(null));
//            if (index == -1)
//                throw new IllegalArgumentException("Could not find tokenizer with name " + before + " in bundle " + bundle);
//            tokenizerEntries.add(index, new TokenizerEntry(name, tokenizer));
//        }
//        return tokenizer;
//    }

    public record TokenizerBundle(String name, List<Tokenizer> tokenizers) {
        public TokenizerBundle(String name, Tokenizer... tokenizers) {
            this(name, new ArrayList<>(Arrays.asList(tokenizers)));
        }
    }

}
