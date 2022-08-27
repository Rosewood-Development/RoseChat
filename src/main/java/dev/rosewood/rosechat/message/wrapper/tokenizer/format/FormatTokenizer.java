package dev.rosewood.rosechat.message.wrapper.tokenizer.format;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import java.util.regex.Matcher;
import net.md_5.bungee.api.ChatColor;

public class FormatTokenizer implements Tokenizer<FormatToken> {

    @Override
    public FormatToken tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions) {
        Matcher matcher = MessageUtils.VALID_LEGACY_REGEX_FORMATTING.matcher(input);
        if (matcher.find()) {
            String match = input.substring(matcher.start(), matcher.end());
            if (input.startsWith(match)) {
                String content = input.substring(matcher.start(), matcher.end());
                char formatCharacter = content.charAt(1);
                char formatCharacterLowercase = Character.toLowerCase(formatCharacter);
                boolean hasPermission = hasPermission(messageWrapper, ignorePermissions, formatCharacterLowercase == 'k' ? "rosechat.magic" : "rosechat.format");
                return hasPermission ? new FormatToken(content, ChatColor.getByChar(formatCharacterLowercase), Character.isLowerCase(formatCharacter)) : new FormatToken(content, null);
            }
        }
        return null;
    }

}
