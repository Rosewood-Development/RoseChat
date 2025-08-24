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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class ColorTokenizer extends Tokenizer {

    public ColorTokenizer() {
        super("color");
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        List<TokenizerResult> results = new ArrayList<>();

        // Handle hex and legacy color codes
        this.collectMatches(MessageUtils.SPIGOT_HEX_REGEX, params, results, "color", false, true);
        this.collectMatches(MessageUtils.VALID_LEGACY_REGEX, params, results, "color", true, true);
        this.collectMatches(MessageUtils.HEX_REGEX, params, results, "hex", false, true);

        // Handle colors that are already parsed
        this.collectMatches(MessageUtils.SPIGOT_HEX_REGEX_PARSED, params, results, null, false, false);
        this.collectMatches(MessageUtils.VALID_LEGACY_REGEX_PARSED, params, results, null, false, false);

        results.sort(Comparator.comparingInt(TokenizerResult::index));
        return results;
    }

    private void collectMatches(Pattern pattern, TokenizerParams params, List<TokenizerResult> results, String colorPermission, boolean perColorPermission, boolean parseShadow) {
        String input = params.getInput();
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            int start = matcher.start();
            String match = matcher.group();
            if (this.overlaps(results, start, matcher.end()))
                continue;

            boolean shadow = parseShadow && ShadowColorDecorator.VALID_VERSION && start > 0 && input.charAt(start - 1) == MessageUtils.SHADOW_PREFIX;
            boolean escape = (start > (shadow ? 1 : 0)) && input.charAt(start - (shadow ? 2 : 1)) == MessageUtils.ESCAPE_CHAR && params.getSender().hasPermission("rosechat.escape");

            int offset = (shadow ? 1 : 0) + (escape ? 1 : 0);
            int realStart = start - offset;
            int consumed = match.length() + offset;
            if (escape) {
                String rawInput = input.substring(realStart + 1, matcher.end());
                results.add(new TokenizerResult(Token.text(rawInput), realStart, consumed));
                continue;
            }

            ChatColor chatColor = this.fromString(match);

            if (colorPermission != null) {
                String shadowPrefix = shadow ? "shadow." : "";
                boolean canUseColors = this.hasTokenPermission(params, "rosechat." + shadowPrefix + colorPermission);
                boolean hasColorPerm = !perColorPermission || this.hasTokenPermission(params, "rosechat." + shadowPrefix + chatColor.getName());
                if (!canUseColors || !hasColorPerm) {
                    String text = Settings.REMOVE_COLOR_CODES.get() ? "" : input.substring(realStart, matcher.end());
                    results.add(new TokenizerResult(Token.text(text), realStart, consumed));
                    continue;
                }
            }

            TokenDecorator decorator = shadow ? new ShadowColorDecorator(chatColor) : new ColorDecorator(chatColor);
            results.add(new TokenizerResult(Token.decorator(decorator), realStart, consumed));
        }
    }

    private boolean overlaps(List<TokenizerResult> results, int start, int end) {
        for (TokenizerResult result : results) {
            int resultStart = result.index();
            int resultEnd = resultStart + result.consumed();
            if (end > resultStart && start < resultEnd)
                return true;
        }
        return false;
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

}
