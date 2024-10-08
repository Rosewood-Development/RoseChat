package dev.rosewood.rosechat.message.tokenizer.style;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.decorator.ColorDecorator;
import dev.rosewood.rosegarden.utils.HexUtils;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class ColorTokenizer extends Tokenizer {

    private static final List<Character> COLOR_PREFIX_CHARACTERS = Arrays.asList('&', ChatColor.COLOR_CHAR, '#', '{', '<');

    public ColorTokenizer() {
        super("color");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!COLOR_PREFIX_CHARACTERS.contains(input.charAt(0))) // Fail fast if the input doesn't start with a color code
            return null;

        // Run this first since it can contain legacy tokens
        ColorToken spigotHexToken = this.parseMatcher(MessageUtils.SPIGOT_HEX_REGEX, input);
        if (spigotHexToken != null) {
            int length = spigotHexToken.content().length();
            return (this.hasTokenPermission(params, "rosechat.color"))
                    ? new TokenizerResult(Token.decorator(ColorDecorator.of(spigotHexToken.color())), length)
                    : new TokenizerResult(Token.text(Settings.REMOVE_COLOR_CODES.get() ? "" : spigotHexToken.content()), length);
        }

        ColorToken legacyToken = this.parseMatcher(MessageUtils.VALID_LEGACY_REGEX, input);
        if (legacyToken != null) {
            int length = legacyToken.content().length();
            boolean canUseColors = this.hasTokenPermission(params, "rosechat.color");
            boolean hasColorPerm = !Settings.USE_PER_COLOR_PERMISSIONS.get()
                    || this.hasTokenPermission(params, "rosechat." + legacyToken.color().getName().toLowerCase());

            return canUseColors && hasColorPerm
                    ? new TokenizerResult(Token.decorator(ColorDecorator.of(legacyToken.color())), length)
                    : new TokenizerResult(Token.text(Settings.REMOVE_COLOR_CODES.get() ? "" : legacyToken.content()), length);
        }

        ColorToken hexToken = this.parseMatcher(MessageUtils.HEX_REGEX, input);
        if (hexToken != null) {
            int length = hexToken.content().length();
            return this.hasTokenPermission(params, "rosechat.hex")
                    ? new TokenizerResult(Token.decorator(ColorDecorator.of(hexToken.color())), length)
                    : new TokenizerResult(Token.text(Settings.REMOVE_COLOR_CODES.get() ? "" : hexToken.content()), length);
        }

        // Handle color codes that are already parsed
        ColorToken legacyTokenParsed = this.parseMatcher(MessageUtils.VALID_LEGACY_REGEX_PARSED, input);
        if (legacyTokenParsed != null)
            return new TokenizerResult(Token.decorator(ColorDecorator.of(legacyTokenParsed.color())), legacyTokenParsed.content().length());

        // Handle hex codes that are already parsed
        ColorToken spigotHexTokenParsed = this.parseMatcher(MessageUtils.SPIGOT_HEX_REGEX_PARSED, input);
        if (spigotHexTokenParsed != null)
            return new TokenizerResult(Token.decorator(ColorDecorator.of(spigotHexTokenParsed.color())), spigotHexTokenParsed.content().length());

        return null;
    }

    private ColorToken parseMatcher(Pattern pattern, String input) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            if (matcher.start() != 0)
                return null;

            String content = input.substring(0, matcher.end());
            return new ColorToken(content, this.fromString(content));
        }

        return null;
    }

    private ChatColor fromString(String string) {
        String lower = string.toLowerCase();
        if (lower.startsWith("&x") || lower.startsWith(ChatColor.COLOR_CHAR + "x"))
            return ChatColor.of("#" + string.substring(2).replace("&", "").replace(ChatColor.COLOR_CHAR + "", ""));

        int hashIndex = string.indexOf('#');
        if (hashIndex == -1)
            return ChatColor.getByChar(string.charAt(1));

        return HexUtils.translateHex(string.substring(hashIndex, hashIndex + 7));
    }

    private record ColorToken(String content, ChatColor color) { }

}
