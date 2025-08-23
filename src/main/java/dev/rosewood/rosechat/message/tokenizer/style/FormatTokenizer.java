package dev.rosewood.rosechat.message.tokenizer.style;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.decorator.FormatDecorator;
import java.util.List;
import java.util.regex.Matcher;
import net.md_5.bungee.api.ChatColor;

public class FormatTokenizer extends Tokenizer {

    public FormatTokenizer() {
        super("format");
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        String rawInput = params.getInput();
        String input = rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR ? rawInput.substring(1) : rawInput;
        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR && !params.getSender().hasPermission("rosechat.escape"))
            return null;

        if (!input.startsWith("&"))
            return null;

        Matcher matcher = MessageUtils.VALID_LEGACY_REGEX_FORMATTING.matcher(input);
        if (!matcher.find() || matcher.start() != 0)
            return null;

        String content = matcher.group();
        char formatCharacter = content.charAt(1);
        char formatCharacterLowercase = Character.toLowerCase(formatCharacter);
        boolean hasPermission = this.hasTokenPermission(params, this.getPermissionForFormat(formatCharacterLowercase));
        if (!hasPermission)
            return List.of(new TokenizerResult(Token.text(Settings.REMOVE_COLOR_CODES.get() ? "" : content), content.length()));

        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR)
            return List.of(new TokenizerResult(Token.text(content), content.length() + 1));

        ChatColor formatCode = ChatColor.getByChar(formatCharacterLowercase);
        boolean enableFormat = Character.isLowerCase(formatCharacter); // Lowercase = enable format, uppercase = disable format
        if (formatCode == ChatColor.RESET) {
            if (!enableFormat) // Full format reset
                return List.of(new TokenizerResult(Token.decorator(new FormatDecorator(formatCode, false)), content.length()));

            // Reset reapplies the player's chat color
            PlayerData playerData = params.getSender().getPlayerData();
            String chatColor = playerData != null && params.containsPlayerInput() ? playerData.getColor() : "";
            return List.of(new TokenizerResult(Token.group("&R" + chatColor).build(), content.length()));
        }

        return List.of(new TokenizerResult(Token.decorator(new FormatDecorator(formatCode, enableFormat)), content.length()));
    }

    public String getPermissionForFormat(char format) {
        return switch (format) {
            case 'l' -> "rosechat.bold";
            case 'n' -> "rosechat.underline";
            case 'm' -> "rosechat.strikethrough";
            case 'o' -> "rosechat.italic";
            case 'k' -> "rosechat.magic";
            default -> null;
        };

    }

}
