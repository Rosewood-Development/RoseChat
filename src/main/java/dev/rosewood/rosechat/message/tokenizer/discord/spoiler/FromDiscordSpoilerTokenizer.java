package dev.rosewood.rosechat.message.tokenizer.discord.spoiler;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;

public class FromDiscordSpoilerTokenizer extends Tokenizer {

    public FromDiscordSpoilerTokenizer() {
        super("from_discord");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("||"))
            return null;
        int lastIndex = 0;

        char[] chars = input.toCharArray();
        for (int i = 2; i < chars.length; i++) {
            if (chars.length - 1 > i && chars[i] == '|' && chars[i + 1] == '|') {
                lastIndex = i + 1;
                break;
            }
        }

        if (lastIndex == 0)
            return null;

        String originalContent = input.substring(0, lastIndex + 1);
        String content = input.substring(2, lastIndex - 1);

        String format = Settings.MARKDOWN_FORMAT_SPOILER.get();
        content = format.contains("%message%") ? format.replace("%message%", content) : format + content;

        return new TokenizerResult(Token.group(content).ignoreTokenizer(this).build(), originalContent.length());
    }

}
