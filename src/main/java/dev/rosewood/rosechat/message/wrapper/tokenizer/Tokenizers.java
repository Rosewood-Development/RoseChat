package dev.rosewood.rosechat.message.wrapper.tokenizer;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dev.rosewood.rosechat.message.wrapper.tokenizer.character.CharacterTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.color.ColorToken;
import dev.rosewood.rosechat.message.wrapper.tokenizer.color.ColorTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.discord.channel.DiscordChannelTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.format.FormatToken;
import dev.rosewood.rosechat.message.wrapper.tokenizer.format.FormatTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.gradient.GradientToken;
import dev.rosewood.rosechat.message.wrapper.tokenizer.gradient.GradientTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.PAPIPlaceholderTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder.RoseChatPlaceholderTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.rainbow.RainbowToken;
import dev.rosewood.rosechat.message.wrapper.tokenizer.rainbow.RainbowTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.replacement.EmojiTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.replacement.RegexReplacementTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.tag.TagTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.url.URLTokenizer;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Tokenizers {

    public static final String DEFAULT_BUNDLE = "default";
    public static final String COLORS_BUNDLE = "colors";
    public static final String DISCORD_FORMATTING_BUNDLE = "discord";

    private static final Multimap<String, TokenizerEntry<?>> TOKENIZERS = MultimapBuilder.hashKeys().arrayListValues().build();

    public static final Tokenizer<GradientToken> GRADIENT = register("gradient", new GradientTokenizer(), DEFAULT_BUNDLE, COLORS_BUNDLE);
    public static final Tokenizer<RainbowToken> RAINBOW = register("rainbow", new RainbowTokenizer(), DEFAULT_BUNDLE, COLORS_BUNDLE);
    public static final Tokenizer<ColorToken> COLOR = register("color", new ColorTokenizer(), DEFAULT_BUNDLE, COLORS_BUNDLE);
    public static final Tokenizer<FormatToken> FORMAT = register("format", new FormatTokenizer(), DEFAULT_BUNDLE, COLORS_BUNDLE);
    public static final Tokenizer<Token> URL = register("url", new URLTokenizer(), DEFAULT_BUNDLE);
    public static final Tokenizer<Token> ROSECHAT_PLACEHOLDER = register("rosechat", new RoseChatPlaceholderTokenizer(), DEFAULT_BUNDLE);
    public static final Tokenizer<Token> PAPI_PLACEHOLDER = register("papi", new PAPIPlaceholderTokenizer(), DEFAULT_BUNDLE);
    public static final Tokenizer<Token> EMOJI = register("emoji", new EmojiTokenizer(), DEFAULT_BUNDLE);
    public static final Tokenizer<Token> TAG = register("tag", new TagTokenizer(), DEFAULT_BUNDLE);
    public static final Tokenizer<Token> REGEX_REPLACEMENT = register("regex", new RegexReplacementTokenizer(), DEFAULT_BUNDLE);
    public static final Tokenizer<Token> CHARACTER = register("character", new CharacterTokenizer(), DEFAULT_BUNDLE);

    public static List<Tokenizer<?>> getBundleValues(String bundle) {
        return Collections.unmodifiableList(TOKENIZERS.get(bundle).stream().map(TokenizerEntry::getTokenizer).collect(Collectors.toList()));
    }

    public static <T extends Token> Tokenizer<T> register(String name, Tokenizer<T> tokenizer, String... bundles) {
        if (bundles.length == 0)
            bundles = new String[] { DEFAULT_BUNDLE };
        for (String bundle : bundles)
            TOKENIZERS.put(bundle, new TokenizerEntry<>(name, tokenizer));
        return tokenizer;
    }

    public static <T extends Token> Tokenizer<T> registerAfter(String after, String name, Tokenizer<T> tokenizer, String... bundles) {
        if (bundles.length == 0)
            bundles = new String[] { DEFAULT_BUNDLE };
        for (String bundle : bundles) {
            List<TokenizerEntry<?>> tokenizerEntries = (List<TokenizerEntry<?>>) TOKENIZERS.get(bundle);
            int index = tokenizerEntries.indexOf(tokenizerEntries.stream().filter(tokenizerEntry -> tokenizerEntry.getName().equals(after)).findFirst().orElse(null));
            if (index == -1)
                throw new IllegalArgumentException("Could not find tokenizer with name " + after + " in bundle " + bundle);
            tokenizerEntries.add(index + 1, new TokenizerEntry<>(name, tokenizer));
        }
        return tokenizer;
    }

    public static <T extends Token> Tokenizer<T> registerBefore(String before, String name, Tokenizer<T> tokenizer, String... bundles) {
        if (bundles.length == 0)
            bundles = new String[] { DEFAULT_BUNDLE };
        for (String bundle : bundles) {
            List<TokenizerEntry<?>> tokenizerEntries = (List<TokenizerEntry<?>>) TOKENIZERS.get(bundle);
            int index = tokenizerEntries.indexOf(tokenizerEntries.stream().filter(tokenizerEntry -> tokenizerEntry.getName().equals(before)).findFirst().orElse(null));
            if (index == -1)
                throw new IllegalArgumentException("Could not find tokenizer with name " + before + " in bundle " + bundle);
            tokenizerEntries.add(index, new TokenizerEntry<>(name, tokenizer));
        }
        return tokenizer;
    }

    public static class TokenizerEntry<T extends Token> {

        private final String name;
        private final Tokenizer<T> tokenizer;

        public TokenizerEntry(String name, Tokenizer<T> tokenizer) {
            this.name = name;
            this.tokenizer = tokenizer;
        }

        public String getName() {
            return this.name;
        }

        public Tokenizer<T> getTokenizer() {
            return this.tokenizer;
        }

    }

}
