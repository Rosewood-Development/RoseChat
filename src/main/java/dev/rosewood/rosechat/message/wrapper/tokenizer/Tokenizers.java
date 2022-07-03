package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosechat.message.wrapper.tokenizer.character.CharacterTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.color.ColorToken;
import dev.rosewood.rosechat.message.wrapper.tokenizer.color.ColorTokenizer;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Tokenizers {

    private static final List<TokenizerEntry<?>> TOKENIZERS = new ArrayList<>();

    public static final Tokenizer<GradientToken> GRADIENT = register("gradient", new GradientTokenizer());
    public static final Tokenizer<RainbowToken> RAINBOW = register("rainbow", new RainbowTokenizer());
    public static final Tokenizer<ColorToken> COLOR = register("color", new ColorTokenizer());
    public static final Tokenizer<FormatToken> FORMAT = register("format", new FormatTokenizer());
    public static final Tokenizer<Token> URL = register("url", new URLTokenizer());
    public static final Tokenizer<Token> ROSECHAT_PLACEHOLDER = register("rosechat", new RoseChatPlaceholderTokenizer());
    public static final Tokenizer<Token> PAPI_PLACEHOLDER = register("papi", new PAPIPlaceholderTokenizer());
    public static final Tokenizer<Token> EMOJI = register("emoji", new EmojiTokenizer());
    public static final Tokenizer<Token> TAG = register("tag", new TagTokenizer());
    public static final Tokenizer<Token> REGEX_REPLACEMENT = register("regex", new RegexReplacementTokenizer());
    public static final Tokenizer<Token> CHARACTER = register("character", new CharacterTokenizer());

    public static List<Tokenizer<?>> values() {
        return Collections.unmodifiableList(TOKENIZERS.stream().map(TokenizerEntry::getTokenizer).collect(Collectors.toList()));
    }

    public static <T extends Token> Tokenizer<T> register(String name, Tokenizer<T> tokenizer) {
        TOKENIZERS.add(new TokenizerEntry<>(name, tokenizer));
        return tokenizer;
    }

    public static <T extends Token> Tokenizer<T> registerAfter(String after, String name, Tokenizer<T> tokenizer) {
        int index = TOKENIZERS.indexOf(TOKENIZERS.stream().filter(entry -> entry.getName().equals(after)).findFirst().orElse(null));
        if (index == -1)
            throw new IllegalArgumentException("Tokenizer " + after + " not found");
        TOKENIZERS.add(index + 1, new TokenizerEntry<>(name, tokenizer));
        return tokenizer;
    }

    public static <T extends Token> Tokenizer<T> registerBefore(String before, String name, Tokenizer<T> tokenizer) {
        int index = TOKENIZERS.indexOf(TOKENIZERS.stream().filter(entry -> entry.getName().equals(before)).findFirst().orElse(null));
        if (index == -1)
            throw new IllegalArgumentException("Tokenizer " + before + " not found");
        TOKENIZERS.add(index, new TokenizerEntry<>(name, tokenizer));
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
