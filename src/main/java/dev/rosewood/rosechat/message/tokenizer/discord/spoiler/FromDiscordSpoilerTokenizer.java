package dev.rosewood.rosechat.message.tokenizer.discord.spoiler;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import java.util.List;

public class FromDiscordSpoilerTokenizer extends Tokenizer {

    public FromDiscordSpoilerTokenizer() {
        super("from_discord");
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        if (true) return null;
        String rawInput = params.getInput();
        String input = rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR ? rawInput.substring(1) : rawInput;
        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR && !params.getSender().hasPermission("rosechat.escape"))
            return null;

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
        content = format.contains("%input_1%") ? format.replace("%input_1%", content) : format + content;

        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR)
            return List.of(new TokenizerResult(Token.text(input), input.length() + 1));

        return List.of(new TokenizerResult(Token.group(content).ignoreTokenizer(this).build(), originalContent.length()));
    }

}
