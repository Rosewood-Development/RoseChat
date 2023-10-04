package dev.rosewood.rosechat.message.tokenizer.placeholder;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import net.md_5.bungee.api.ChatColor;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PAPIPlaceholderTokenizer extends Tokenizer {

    private static final Pattern PATTERN = Pattern.compile("%(.*?)%");
    private final boolean isBungee;

    public PAPIPlaceholderTokenizer(boolean isBungee) {
        super(isBungee ? "bungee_papi" : "papi");
        this.isBungee = isBungee;
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("%")) return null;

        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.find() || matcher.start() != 0) return null;

        String placeholder = input.substring(1, matcher.end() - 1);
        String placeholderPermission = placeholder.replaceFirst("_", ".");
        if (!MessageUtils.hasExtendedTokenPermission(params, "rosechat.placeholders", "rosechat.placeholder." + placeholderPermission))
            return null;

        String originalContent = matcher.group();

        String content;
        if (originalContent.startsWith("%other_") && !this.isBungee) {
            if (!params.getReceiver().isPlayer()) content = originalContent;
            else {
                content = PlaceholderAPIHook.applyRelationalPlaceholders(params.getSender().asPlayer(), params.getReceiver().asPlayer(), originalContent.replaceFirst("other_", ""));
                content = PlaceholderAPIHook.applyPlaceholders(params.getReceiver().asPlayer(), content.replaceFirst("other_", ""));
            }
        } else {
            content = PlaceholderAPIHook.applyRelationalPlaceholders(params.getSender().asPlayer(), params.getReceiver().asPlayer(), originalContent);
            content = PlaceholderAPIHook.applyPlaceholders(params.getSender().asPlayer(), content);
        }

        // Encapsulate if the placeholder only contains a colour
        boolean encapsulate = true;

        // Ignore everything that definitely isn't a colour.
        if (content.startsWith(ChatColor.COLOR_CHAR + "") || content.startsWith("&") || content.startsWith("#") || content.startsWith("<") || !content.startsWith("{")) {
            Matcher legacyMatcher = MessageUtils.VALID_LEGACY_REGEX_COMBINED.matcher(content);
            if (legacyMatcher.find() && content.equalsIgnoreCase(legacyMatcher.group()))
                encapsulate = false;

            if (encapsulate) {
                Matcher hexMatcher = MessageUtils.SPIGOT_HEX_REGEX_COMBINED.matcher(content);
                if (hexMatcher.find() && content.equalsIgnoreCase(hexMatcher.group()))
                    encapsulate = false;
            }

            if (encapsulate) {
                Matcher gradientMatcher = MessageUtils.GRADIENT_PATTERN.matcher(content);
                if (gradientMatcher.find() && content.equalsIgnoreCase(gradientMatcher.group()))
                    encapsulate = false;
            }

            if (encapsulate) {
                Matcher rainbowMatcher = MessageUtils.RAINBOW_PATTERN.matcher(content);
                if (rainbowMatcher.find() && content.equalsIgnoreCase(rainbowMatcher.group()))
                    encapsulate = false;
            }
        }

        if (Objects.equals(content, originalContent)) {
            Token.Builder token = Token.text(content);
            if (encapsulate) token.encapsulate();

            return new TokenizerResult(token.build(), originalContent.length());
        } else {
            Token.Builder token = Token.group(content);
            if (encapsulate) token.encapsulate();

            return new TokenizerResult(token.build(), originalContent.length());
        }
    }

}
