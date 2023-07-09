package dev.rosewood.rosechat.message.tokenizer.markdown;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosegarden.utils.HexUtils;
import org.bukkit.ChatColor;

public class MarkdownBlockQuoteTokenizer extends Tokenizer {

    public MarkdownBlockQuoteTokenizer() {
        super("markdown_block_quote");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!ChatColor.stripColor(HexUtils.colorify(params.getPlayerInput())).startsWith("> ")) return null;
        if (!input.startsWith("> ")) return null;
        if (!MessageUtils.hasTokenPermission(params, "rosechat.quote")) return null;

        String content = input.substring(2);

        String format = Setting.MARKDOWN_FORMAT_BLOCK_QUOTES.getString();
        content = format.contains("%message%") ? format.replace("%message%", content) : format + content;

        return new TokenizerResult(Token.group(content).ignoreTokenizer(this).build(), input.length());
    }

}
