package dev.rosewood.rosechat.message.tokenizer.discord.spoiler;

import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;

public class ToDiscordSpoilerTokenizer extends Tokenizer {

    public ToDiscordSpoilerTokenizer() {
        super("to_discord_spoiler");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!MessageUtils.hasTokenPermission(params, "rosechat.spoiler")) return null;
        String spoiler = ConfigurationManager.Setting.MARKDOWN_FORMAT_SPOILER.getString();
        String prefix = spoiler.substring(0, spoiler.indexOf("%message%"));
        String suffix = spoiler.substring(spoiler.indexOf("%message%") + "%message%".length());
        if (!input.startsWith(prefix)) return null;
        if (!input.endsWith(suffix)) return null;

        String originalContent = input.substring(0, input.lastIndexOf(suffix) + suffix.length());
        String content = input.substring(prefix.length(), input.lastIndexOf(suffix));

        return new TokenizerResult(Token.group("||" + content + "||")
                .ignoreTokenizer(this)
                .ignoreTokenizer(Tokenizers.COLOR)
                .ignoreTokenizer(Tokenizers.FORMAT)
                .build(), originalContent.length());
    }

}
