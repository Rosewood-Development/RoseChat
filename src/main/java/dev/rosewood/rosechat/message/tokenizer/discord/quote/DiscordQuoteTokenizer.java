package dev.rosewood.rosechat.message.tokenizer.discord.quote;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosegarden.utils.HexUtils;
import org.bukkit.ChatColor;

public class DiscordQuoteTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
        if (!this.hasPermission(roseMessage, ignorePermissions, "rosechat.quote")) return null;
        if (!ChatColor.stripColor(HexUtils.colorify(roseMessage.getMessage())).startsWith("> ")) return null;
        if (!input.startsWith("> ")) return null;
        String content = input.substring(2);

        String format = Setting.MARKDOWN_FORMAT_BLOCK_QUOTES.getString();
        content = format.contains("%message%") ? format.replace("%message%", content) : format + content;

        return new Token(new Token.TokenSettings(input).content(content).ignoreTokenizer(this));
    }

}
