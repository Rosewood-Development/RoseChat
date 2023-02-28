package dev.rosewood.rosechat.message.tokenizer.discord.code;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;

public class DiscordMultiCodeTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
        if (!ignorePermissions && !MessageUtils.hasTokenPermission(roseMessage, "rosechat.multicode")) return null;
        if (!input.startsWith("```")) return null;
        int lastIndex = 0;

        char[] chars = input.toCharArray();
        for (int i = 3; i < chars.length; i++) {
            if (chars.length - 2 > i && chars[i] == '`' && chars[i + 1] == '`' && chars[i + 2] == '`') {
                lastIndex = i + 3;
                break;
            }
        }

        if (lastIndex == 0) return null;
        String originalContent = input.substring(0, lastIndex);
        String content = input.substring(3, lastIndex - 3);

        String format = Setting.MARKDOWN_FORMAT_CODE_BLOCK_MULTIPLE.getString();
        content = format.contains("%message%") ? format.replace("%message%", content) : format + content;

        return new Token(new Token.TokenSettings(originalContent).content(content).ignoreTokenizer(this).ignoreTokenizer(Tokenizers.DISCORD_CODE));
    }

}
