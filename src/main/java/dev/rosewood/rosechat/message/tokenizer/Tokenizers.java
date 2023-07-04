package dev.rosewood.rosechat.message.tokenizer;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dev.rosewood.rosechat.message.tokenizer.character.CharacterTokenizer;
import dev.rosewood.rosechat.message.tokenizer.color.ColorTokenizer;
import dev.rosewood.rosechat.message.tokenizer.format.FormatTokenizer;
import dev.rosewood.rosechat.message.tokenizer.placeholder.PAPIPlaceholderTokenizer;
import dev.rosewood.rosechat.message.tokenizer.placeholder.RoseChatPlaceholderTokenizer;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Tokenizers {

    public static final String DEFAULT_BUNDLE = "default";
    public static final String DEFAULT_DISCORD_BUNDLE = "default_discord";
    public static final String COLORS_BUNDLE = "colors";
    public static final String MARKDOWN_BUNDLE = "markdown";
    public static final String DISCORD_FORMATTING_BUNDLE = "discord";
    public static final String TO_DISCORD_BUNDLE = "to_discord";
    public static final String FROM_DISCORD_BUNDLE = "from_discord";
    public static final String DISCORD_EMOJI_BUNDLE = "discord_emoji";
    public static final String BUNGEE_BUNDLE = "bungee_bundle";

    private static final Multimap<String, TokenizerEntry> TOKENIZERS = MultimapBuilder.hashKeys().arrayListValues().build();

//    public static final Tokenizer TO_DISCORD_SPOILER = register("to_discord_spoiler", new ToDiscordSpoilerTokenizer(), TO_DISCORD_BUNDLE);
//    public static final Tokenizer FROM_DISCORD_SPOILER = register("from_discord_spoiler", new FromDiscordSpoilerTokenizer(), FROM_DISCORD_BUNDLE);
//    public static final Tokenizer DISCORD_EMOJI = register("discord_emoji", new DiscordEmojiTokenizer(), DISCORD_EMOJI_BUNDLE);
//    public static final Tokenizer TO_DISCORD_TAG = register("to_discord_tag", new ToDiscordTagTokenizer(), TO_DISCORD_BUNDLE);
//    public static final Tokenizer FROM_DISCORD_TAG = register("from_discord_tag", new FromDiscordTagTokenizer(), FROM_DISCORD_BUNDLE);
//    public static final Tokenizer TO_DISCORD_CHANNEL = register("to_discord_channel", new ToDiscordChannelTokenizer(), TO_DISCORD_BUNDLE);
//    public static final Tokenizer FROM_DISCORD_CHANNEL = register("from_discord_channel", new FromDiscordChannelTokenizer(), FROM_DISCORD_BUNDLE);
//    public static final Tokenizer DISCORD_MULTICODE = register("discord_multicode", new DiscordMultiCodeTokenizer(), DISCORD_FORMATTING_BUNDLE);
//    public static final Tokenizer DISCORD_CODE = register("discord_code", new DiscordCodeTokenizer(), DISCORD_FORMATTING_BUNDLE);
//    public static final Tokenizer DISCORD_QUOTE = register("discord_quote", new DiscordQuoteTokenizer(), DISCORD_FORMATTING_BUNDLE);
//    public static final Tokenizer DISCORD_CUSTOM_EMOJI = register("discord_custom_emoji", new DiscordCustomEmojiTokenizer(), TO_DISCORD_BUNDLE);
//    public static final Tokenizer MARKDOWN_BOLD = register("markdown_bold", new MarkdownBoldTokenizer(), MARKDOWN_BUNDLE);
//    public static final Tokenizer MARKDOWN_ITALIC = register("markdown_italic", new MarkdownItalicTokenizer(), MARKDOWN_BUNDLE);
//    public static final Tokenizer MARKDOWN_UNDERLINE = register("markdown_underline", new MarkdownUnderlineTokenizer(), MARKDOWN_BUNDLE);
//    public static final Tokenizer MARKDOWN_STRIKETHROUGH = register("markdown_strikethrough", new MarkdownStrikethroughTokenizer(), MARKDOWN_BUNDLE);
//    public static final Tokenizer GRADIENT = register("gradient", new GradientTokenizer(), DEFAULT_BUNDLE, COLORS_BUNDLE, DEFAULT_DISCORD_BUNDLE, BUNGEE_BUNDLE);
//    public static final Tokenizer RAINBOW = register("rainbow", new RainbowTokenizer(), DEFAULT_BUNDLE, COLORS_BUNDLE, BUNGEE_BUNDLE);
//    public static final Tokenizer SHADER_COLORS = register("shader_colors", new ShaderTokenizer(), DEFAULT_BUNDLE, DEFAULT_DISCORD_BUNDLE, BUNGEE_BUNDLE);
    public static final Tokenizer COLOR = register("color", new ColorTokenizer(), DEFAULT_BUNDLE, COLORS_BUNDLE, DEFAULT_DISCORD_BUNDLE, BUNGEE_BUNDLE);
    public static final Tokenizer FORMAT = register("format", new FormatTokenizer(), DEFAULT_BUNDLE, COLORS_BUNDLE, BUNGEE_BUNDLE);
//    public static final Tokenizer URL = register("url", new URLTokenizer(), DEFAULT_BUNDLE, DEFAULT_DISCORD_BUNDLE, BUNGEE_BUNDLE);
    public static final Tokenizer ROSECHAT_PLACEHOLDER = register("rosechat", new RoseChatPlaceholderTokenizer(), DEFAULT_BUNDLE, DEFAULT_DISCORD_BUNDLE, BUNGEE_BUNDLE);
    public static final Tokenizer PAPI_PLACEHOLDER = register("papi", new PAPIPlaceholderTokenizer(false), DEFAULT_BUNDLE, DEFAULT_DISCORD_BUNDLE);
    public static final Tokenizer BUNGEE_PAPI_PLACEHOLDER = register("bungee_papi", new PAPIPlaceholderTokenizer(true), BUNGEE_BUNDLE);
//    public static final Tokenizer EMOJI = register("emoji", new EmojiTokenizer(), DEFAULT_BUNDLE, DEFAULT_DISCORD_BUNDLE, BUNGEE_BUNDLE);
//    public static final Tokenizer TAG = register("tag", new TagTokenizer(), DEFAULT_BUNDLE, DEFAULT_DISCORD_BUNDLE, BUNGEE_BUNDLE);
//    public static final Tokenizer REGEX_REPLACEMENT = register("regex", new RegexReplacementTokenizer(), DEFAULT_BUNDLE, DEFAULT_DISCORD_BUNDLE, BUNGEE_BUNDLE);
//    public static final Tokenizer REPLACEMENT = register("replacement", new ReplacementTokenizer(), DEFAULT_BUNDLE, DEFAULT_DISCORD_BUNDLE, BUNGEE_BUNDLE);
    public static final Tokenizer CHARACTER = register("character", new CharacterTokenizer(), DEFAULT_BUNDLE, DEFAULT_DISCORD_BUNDLE, BUNGEE_BUNDLE);

    public static List<Tokenizer> getBundleValues(String bundle) {
        return Collections.unmodifiableList(TOKENIZERS.get(bundle).stream().map(TokenizerEntry::getTokenizer).collect(Collectors.toList()));
    }

    /**
     * Registers a new tokenizer
     * @param name The id of the tokenizer.
     * @param tokenizer The {@link Tokenizer} tokenizer to register.
     * @param bundles The bundles that contain this tokenizer.
     * @return The registered Tokenizer.
     */
    public static Tokenizer register(String name, Tokenizer tokenizer, String... bundles) {
        if (bundles.length == 0)
            bundles = new String[] { DEFAULT_BUNDLE };
        for (String bundle : bundles)
            TOKENIZERS.put(bundle, new TokenizerEntry(name, tokenizer));
        return tokenizer;
    }

    public static Tokenizer registerAfter(String after, String name, Tokenizer tokenizer, String... bundles) {
        if (bundles.length == 0)
            bundles = new String[] { DEFAULT_BUNDLE };
        for (String bundle : bundles) {
            List<TokenizerEntry> tokenizerEntries = (List<TokenizerEntry>) TOKENIZERS.get(bundle);
            int index = tokenizerEntries.indexOf(tokenizerEntries.stream().filter(tokenizerEntry -> tokenizerEntry.getName().equals(after)).findFirst().orElse(null));
            if (index == -1)
                throw new IllegalArgumentException("Could not find tokenizer with name " + after + " in bundle " + bundle);
            tokenizerEntries.add(index + 1, new TokenizerEntry(name, tokenizer));
        }
        return tokenizer;
    }

    public static Tokenizer registerBefore(String before, String name, Tokenizer tokenizer, String... bundles) {
        if (bundles.length == 0)
            bundles = new String[] { DEFAULT_BUNDLE };
        for (String bundle : bundles) {
            List<TokenizerEntry> tokenizerEntries = (List<TokenizerEntry>) TOKENIZERS.get(bundle);
            int index = tokenizerEntries.indexOf(tokenizerEntries.stream().filter(tokenizerEntry -> tokenizerEntry.getName().equals(before)).findFirst().orElse(null));
            if (index == -1)
                throw new IllegalArgumentException("Could not find tokenizer with name " + before + " in bundle " + bundle);
            tokenizerEntries.add(index, new TokenizerEntry(name, tokenizer));
        }
        return tokenizer;
    }

    public static class TokenizerEntry {

        private final String name;
        private final Tokenizer tokenizer;

        public TokenizerEntry(String name, Tokenizer tokenizer) {
            this.name = name;
            this.tokenizer = tokenizer;
        }

        public String getName() {
            return this.name;
        }

        public Tokenizer getTokenizer() {
            return this.tokenizer;
        }

    }

}
