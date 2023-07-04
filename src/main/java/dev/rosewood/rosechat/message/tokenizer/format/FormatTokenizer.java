package dev.rosewood.rosechat.message.tokenizer.format;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.decorator.FormatDecorator;
import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;

public class FormatTokenizer implements Tokenizer {

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        if (!params.getInput().startsWith("&")) return null;

        Matcher matcher = MessageUtils.VALID_LEGACY_REGEX_FORMATTING.matcher(params.getInput());
        if (matcher.find()) {
            if (matcher.start() != 0) return null;
            String content = matcher.group();
            char formatCharacter = content.charAt(1);
            char formatCharacterLowercase = Character.toLowerCase(formatCharacter);
            boolean hasPermission = MessageUtils.hasTokenPermission(params, this.getPermissionForFormat(formatCharacterLowercase));
            ChatColor formatCode = ChatColor.getByChar(formatCharacterLowercase);
            boolean value = Character.isLowerCase(formatCharacter); // Lowercase = enable format, uppercase = disable format
            return hasPermission
                    ? new TokenizerResult(Token.builder().addDecorator(FormatDecorator.of(formatCode, value)).resolve().build(), content.length())
                    : new TokenizerResult(Token.builder().content(content).resolve().build(), content.length());
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
