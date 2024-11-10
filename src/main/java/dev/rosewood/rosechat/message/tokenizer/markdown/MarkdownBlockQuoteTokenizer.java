package dev.rosewood.rosechat.message.tokenizer.markdown;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

        String[] splits = format.split(Pattern.quote("%message%"));
        if (splits.length == 1) {
            return new TokenizerResult(Token.group(
                    Token.group(format).ignoreTokenizer(this).build(),
                    Token.group(content).ignoreTokenizer(this).containsPlayerInput().build()
            ).build(), input.length());
        } else {
            List<Token> chunks = new ArrayList<>(splits.length * 2 - 1);
            for (int i = 0; i < splits.length; i++) {
                chunks.add(Token.group(splits[i]).ignoreTokenizer(this).build());
                if (i != splits.length - 1)
                    chunks.add(Token.group(content).ignoreTokenizer(this).containsPlayerInput().build());
            }
            return new TokenizerResult(Token.group(chunks).build(), input.length());
        }
    }

}
