package dev.rosewood.rosechat.message.wrapper.tokenizer.color;

import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.wrapper.ComponentColorizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import dev.rosewood.rosegarden.utils.HexUtils;
import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorTokenizer implements Tokenizer<ColorToken> {

    @Override
    public ColorToken tokenize(MessageWrapper messageWrapper, String input) {
        ColorToken legacyToken = this.parseMatcher(ComponentColorizer.VALID_LEGACY_REGEX, input);
        if (legacyToken != null) return legacyToken;

        ColorToken legacyFormattingToken = this.parseMatcher(ComponentColorizer.VALID_LEGACY_REGEX_FORMATTING, input);
        if (legacyFormattingToken != null) return legacyFormattingToken;

        return this.parseMatcher(ComponentColorizer.HEX_REGEX, input);
    }
    
    private ColorToken parseMatcher(Pattern pattern, String input) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String match = input.substring(matcher.start(), matcher.end());
            if (input.startsWith(match)) {
                String content = input.substring(matcher.start(), matcher.end());
                return new ColorToken(content, this.fromString(content));
            }
        }

        return null;
    }

    private ChatColor fromString(String string) {
        int hashIndex = string.indexOf('#');
        if (hashIndex == -1)
            return ChatColor.getByChar(string.charAt(1));
        return HexUtils.translateHex(string.substring(hashIndex, hashIndex + 7));
    }

}
