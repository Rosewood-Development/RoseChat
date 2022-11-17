package dev.rosewood.rosechat.message.wrapper.tokenizer.format;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;

public class FormatTokenizer implements Tokenizer<FormatToken> {

    @Override
    public FormatToken tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions) {
        Matcher matcher = MessageUtils.VALID_LEGACY_REGEX_FORMATTING.matcher(input);
        if (matcher.find()) {
            if (matcher.start() != 0) return null;
            String content = input.substring(0, matcher.end());
            char formatCharacter = content.charAt(1);
            char formatCharacterLowercase = Character.toLowerCase(formatCharacter);
            boolean hasPermission = this.hasPermission(messageWrapper, ignorePermissions, this.getPermissionForFormat(formatCharacterLowercase));
            return hasPermission ? new FormatToken(content, ChatColor.getByChar(formatCharacterLowercase), Character.isLowerCase(formatCharacter)) : new FormatToken(content, null);
        }
        return null;
    }

    public String getPermissionForFormat(char format) {
        switch (format) {
            case 'l':
                return "rosechat.bold";
            case 'n':
                return "rosechat.underline";
            case 'm':
                return "rosechat.strikethrough";
            case 'o':
                return "rosechat.italic";
            case 'k':
                return "rosechat.magic";
        }

        return null;
    }

}
