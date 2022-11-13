package dev.rosewood.rosechat.message.wrapper.tokenizer.discord.quote;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import dev.rosewood.rosegarden.utils.HexUtils;
import org.bukkit.ChatColor;

public class DiscordQuoteTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions) {
        if (!this.hasPermission(messageWrapper, ignorePermissions, "rosechat.quote")) return null;
        if (!ChatColor.stripColor(HexUtils.colorify(messageWrapper.getMessage())).startsWith("> ")) return null;
        if (!input.startsWith("> ")) return null;
        String content = input.substring(2);

        String format = Setting.MARKDOWN_FORMAT_BLOCK_QUOTES.getString();
        content = format.contains("%message%") ? format.replace("%message%", content) : format + content;

        return new Token(new Token.TokenSettings(input).content(content).ignoreTokenizer(this));
    }

}
