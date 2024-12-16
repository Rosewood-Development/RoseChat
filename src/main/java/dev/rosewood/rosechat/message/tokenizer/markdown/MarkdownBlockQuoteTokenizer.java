package dev.rosewood.rosechat.message.tokenizer.markdown;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.TokenPatternSplitter;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;

public class MarkdownBlockQuoteTokenizer extends Tokenizer {

    public MarkdownBlockQuoteTokenizer() {
        super("markdown_block_quote");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String playerInput = params.getPlayerInput();
        if (playerInput == null || !params.getPlayerInput().startsWith("> "))
            return null;

        String input = params.getInput();
        if (!input.startsWith("> "))
            return null;

        if (!this.hasTokenPermission(params, "rosechat.quote"))
            return null;

        String content = input.substring(2);
        String format = Settings.MARKDOWN_FORMAT_BLOCK_QUOTES.get();

        if (!format.contains("%message%")) {
            return new TokenizerResult(Token.group(
                    Token.group(format).ignoreTokenizer(this).build(),
                    Token.group(content).ignoreTokenizer(this).containsPlayerInput().build()
            ).build(), input.length());
        }

        Token token = new TokenPatternSplitter()
                .matchConsumer(match -> {
                    match.ignoreTokenizer(this);
                    match.containsPlayerInput();
                })
                .otherConsumer(other -> other.ignoreTokenizer(this))
                .pattern("%message%", content)
                .apply(format);

        return new TokenizerResult(token, input.length());
    }

}
