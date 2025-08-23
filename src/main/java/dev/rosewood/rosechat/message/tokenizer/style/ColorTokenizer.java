package dev.rosewood.rosechat.message.tokenizer.style;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.decorator.ColorDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.ShadowColorDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
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
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        String rawInput = params.getInput();
        String input = rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR ? rawInput.substring(1) : rawInput;
        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR && !params.getSender().hasPermission("rosechat.escape"))
            return null;

        boolean shadow;
        if (ShadowColorDecorator.VALID_VERSION && input.charAt(0) == MessageUtils.SHADOW_PREFIX && input.length() >= 3) {
            input = input.substring(1);
            shadow = true;
        } else shadow = false;

        if (!COLOR_PREFIX_CHARACTERS.contains(input.charAt(0))) // Fail fast if the input doesn't start with a color code
            return null;

        // Run this first since it can contain legacy tokens
        ColorToken spigotHexToken = this.parseMatcher(MessageUtils.SPIGOT_HEX_REGEX, input);
        if (spigotHexToken != null) {
            int length = spigotHexToken.content().length();
            if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR) {
                String content = shadow ? MessageUtils.SHADOW_PREFIX + spigotHexToken.content() : spigotHexToken.content;
                return List.of(new TokenizerResult(Token.text(content), length + 1 + this.length(shadow)));
            }

            return (this.hasTokenPermission(params, "rosechat." + this.shadow(shadow) + "color"))
                    ? List.of(new TokenizerResult(Token.decorator(this.decorator(spigotHexToken.color(), shadow)), length + this.length(shadow)))
                    : List.of(new TokenizerResult(Token.text(Settings.REMOVE_COLOR_CODES.get() ? "" : (shadow ? MessageUtils.SHADOW_PREFIX : "") + spigotHexToken.content()), length + this.length(shadow)));
        }

        ColorToken legacyToken = this.parseMatcher(MessageUtils.VALID_LEGACY_REGEX, input);
        if (legacyToken != null) {
            int length = legacyToken.content().length();
            if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR) {
                String content = shadow ? MessageUtils.SHADOW_PREFIX + legacyToken.content() : legacyToken.content;
                return List.of(new TokenizerResult(Token.text(content), length + 1 + this.length(shadow)));
            }

            boolean canUseColors = this.hasTokenPermission(params, "rosechat." + this.shadow(shadow) + "color");
            boolean hasColorPerm = !Settings.USE_PER_COLOR_PERMISSIONS.get()
                    || this.hasTokenPermission(params, "rosechat." + this.shadow(shadow) + legacyToken.color().getName().toLowerCase());

            if (canUseColors && hasColorPerm) {
                return List.of(new TokenizerResult(Token.decorator(this.decorator(legacyToken.color(), shadow)), length + this.length(shadow)));
            } else {
                return List.of(new TokenizerResult(Token.text(Settings.REMOVE_COLOR_CODES.get() ? "" : (shadow ? MessageUtils.SHADOW_PREFIX : "") + legacyToken.content()), length + this.length(shadow)));
            }
        }

        ColorToken hexToken = this.parseMatcher(MessageUtils.HEX_REGEX, input);
        if (hexToken != null) {
            int length = hexToken.content().length();
            if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR) {
                String content = shadow ? MessageUtils.SHADOW_PREFIX + hexToken.content() : hexToken.content;
                return List.of(new TokenizerResult(Token.text(content), length + 1 + this.length(shadow)));
            }

            return this.hasTokenPermission(params, "rosechat." + this.shadow(shadow) + "hex")
                    ? List.of(new TokenizerResult(Token.decorator(this.decorator(hexToken.color(), shadow)), length + this.length(shadow)))
                    : List.of(new TokenizerResult(Token.text(Settings.REMOVE_COLOR_CODES.get() ? "" : (shadow ? MessageUtils.SHADOW_PREFIX : "") + hexToken.content()), length + this.length(shadow)));
        }

        // Don't continue from here, they are pre-parsed colors, of which there is no format for shadows
        if (shadow)
            return null;

        // Handle color codes that are already parsed
        ColorToken legacyTokenParsed = this.parseMatcher(MessageUtils.VALID_LEGACY_REGEX_PARSED, input);
        if (legacyTokenParsed != null)
            return List.of(new TokenizerResult(Token.decorator(new ColorDecorator(legacyTokenParsed.color())), legacyTokenParsed.content().length()));

        // Handle hex codes that are already parsed
        ColorToken spigotHexTokenParsed = this.parseMatcher(MessageUtils.SPIGOT_HEX_REGEX_PARSED, input);
        if (spigotHexTokenParsed != null)
            return List.of(new TokenizerResult(Token.decorator(new ColorDecorator(spigotHexTokenParsed.color())), spigotHexTokenParsed.content().length()));

        return null;
    }

    private String shadow(boolean shadow) {
        return shadow ? "shadow." : "";
    }

    private int length(boolean shadow) {
        return shadow ? 1 : 0;
    }

    private TokenDecorator decorator(ChatColor color, boolean shadow) {
        return !shadow ? new ColorDecorator(color) : new ShadowColorDecorator(color);
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
