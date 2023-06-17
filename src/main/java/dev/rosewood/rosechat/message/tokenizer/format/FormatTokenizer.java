package dev.rosewood.rosechat.message.tokenizer.format;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;

public class FormatTokenizer implements Tokenizer<FormatToken> {

    @Override
    public FormatToken tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
        if (!input.startsWith("&")) return null;

        Matcher matcher = MessageUtils.VALID_LEGACY_REGEX_FORMATTING.matcher(input);
        if (matcher.find()) {
            if (matcher.start() != 0) return null;
            String content = matcher.group();
            char formatCharacter = content.charAt(1);
            char formatCharacterLowercase = Character.toLowerCase(formatCharacter);
            boolean hasPermission = ignorePermissions || MessageUtils.hasTokenPermission(roseMessage, this.getPermissionForFormat(formatCharacterLowercase));
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
