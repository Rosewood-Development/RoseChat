package dev.rosewood.rosechat.message.tokenizer.style;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.decorator.FormatDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.TokenDecorator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class FormatTokenizer extends Tokenizer {

    public FormatTokenizer() {
        super("format");
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        List<TokenizerResult> results = new ArrayList<>();

        this.collectMatches(MessageUtils.LEGACY_REGEX_FORMATTING, params, results, true, true);
        this.collectMatches(MessageUtils.LEGACY_REGEX_FORMATTING_PARSED, params, results, false, false);

        Collections.sort(results);
        return results;
    }

    private void collectMatches(Pattern pattern, TokenizerParams params, List<TokenizerResult> results, boolean checkPermission, boolean parseShadow) {
        String input = params.getInput();
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String match = matcher.group();

            boolean escape = (start > 0) && input.charAt(start - 1) == MessageUtils.ESCAPE_CHAR && params.getSender().hasPermission("rosechat.escape");

            int offset = (escape ? 1 : 0);
            int realStart = start - offset;
            int consumed = match.length() + offset;
            if (escape) {
                String rawInput = input.substring(realStart + 1, end);
                results.add(new TokenizerResult(Token.text(rawInput), realStart, consumed));
                continue;
            }

            char formatChar = match.charAt(1);
            char formatCharLower = Character.toLowerCase(formatChar);
            boolean enableFormat = formatChar == formatCharLower;
            ChatColor formatCode = ChatColor.getByChar(formatCharLower);

            if (checkPermission && !this.hasTokenPermission(params, "rosechat." + this.getFormatName(formatCharLower))) {
                String text = Settings.REMOVE_COLOR_CODES.get() ? "" : input.substring(realStart, end);
                results.add(new TokenizerResult(Token.text(text), realStart, consumed));
                continue;
            }

            // &r reapplies the player's chat color
            if (formatChar == 'r') {
                PlayerData playerData = params.getSender().getPlayerData();
                String chatColor = playerData != null && params.containsPlayerInput() ? playerData.getColor() : "";
                results.add(new TokenizerResult(Token.group("&R" + chatColor).build(), realStart, consumed));
                continue;
            }

            TokenDecorator decorator = new FormatDecorator(formatCode, enableFormat);
            results.add(new TokenizerResult(Token.decorator(decorator), realStart, consumed));
        }
    }

    private String getFormatName(char format) {
        return switch (format) {
            case 'k' -> "magic";
            case 'l' -> "bold";
            case 'm' -> "strikethrough";
            case 'n' -> "underline";
            case 'o' -> "italic";
            case 'r' -> "reset";
            default -> throw new IllegalStateException("Unhandled format code: " + format);
        };
    }

}
