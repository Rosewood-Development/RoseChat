package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.spoiler;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizers;

public class FromDiscordSpoilerTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions) {
        if (!input.startsWith("||")) return null;
        int lastIndex = 0;

        char[] chars = input.toCharArray();
        for (int i = 2; i < chars.length; i++) {
            if (chars.length - 1 > i && chars[i] == '|' && chars[i+1] == '|') {
                lastIndex = i + 1;
                break;
            }
        }

        if (lastIndex == 0) return null;
        String originalContent = input.substring(0, lastIndex + 1);
        String content = input.substring(2, lastIndex - 1);

        String format = Setting.DISCORD_FORMAT_SPOILER.getString();
        content = format.contains("%message%") ? format.replace("%message%", content) : format + content;

        return new Token(new Token.TokenSettings(originalContent).content(content).ignoreTokenizer(this).ignoreTokenizer(Tokenizers.EMOJI));
    }
}
