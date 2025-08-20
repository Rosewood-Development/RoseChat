package dev.rosewood.rosechat.message.tokenizer.discord.spoiler;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;

public class ToDiscordSpoilerTokenizer extends Tokenizer {

    public ToDiscordSpoilerTokenizer() {
        super("to_discord_spoiler");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String rawInput = params.getInput();
        String input = rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR ? rawInput.substring(1) : rawInput;
        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR && !params.getSender().hasPermission("rosechat.escape"))
            return null;

        String spoiler = Settings.MARKDOWN_FORMAT_SPOILER.get();
        if (!spoiler.contains("%input_1%"))
            return null;

        int inputIndex = spoiler.indexOf("%input_1%");

        String prefix = spoiler.substring(0, inputIndex);
        String suffix = spoiler.substring(inputIndex + "%input_1%".length());
        if (!input.startsWith(prefix))
            return null;

        if (!input.endsWith(suffix))
            return null;

        if (!this.hasTokenPermission(params, "rosechat.spoiler"))
            return null;

        String originalContent = input.substring(0, input.lastIndexOf(suffix) + suffix.length());
        String content = input.substring(prefix.length(), input.lastIndexOf(suffix));

        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR)
            return new TokenizerResult(Token.text(input), input.length() + 1);

        return new TokenizerResult(Token.group("||" + content + "||")
                .ignoreTokenizer(this)
                .ignoreTokenizer(Tokenizers.COLOR)
                .ignoreTokenizer(Tokenizers.FORMAT)
                .build(), originalContent.length());
    }

}
