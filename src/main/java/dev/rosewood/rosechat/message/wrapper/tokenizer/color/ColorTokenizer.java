package dev.rosewood.rosechat.message.wrapper.tokenizer.color;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import dev.rosewood.rosegarden.utils.HexUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class ColorTokenizer implements Tokenizer<ColorToken> {

    @Override
    public ColorToken tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions) {
        ColorToken legacyToken = this.parseMatcher(MessageUtils.VALID_LEGACY_REGEX, input);
        if (legacyToken != null) return legacyToken;

        return this.parseMatcher(MessageUtils.HEX_REGEX, input);
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
