package dev.rosewood.rosechat.message.wrapper.tokenizer.format;

import dev.rosewood.rosechat.message.wrapper.tokenizer.FormattedColorGenerator;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizers;
import dev.rosewood.rosechat.message.wrapper.tokenizer.color.ColorToken;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

public class FormatToken extends Token {

    private final ChatColor format;

    public FormatToken(String originalText, ChatColor format) {
        super(new TokenSettings(originalText).content(""));

        this.format = format;
    }

    @Override
    public FormattedColorGenerator applyFormatCodes(FormattedColorGenerator colorGenerator, String baseColor, List<Token> futureTokens) {
        if (this.format == ChatColor.MAGIC) {
            colorGenerator.obfuscated();
        } else if (this.format == ChatColor.BOLD) {
            colorGenerator.bold();
        } else if (this.format == ChatColor.STRIKETHROUGH) {
            colorGenerator.strikethrough();
        } else if (this.format == ChatColor.UNDERLINE) {
            colorGenerator.underline();
        } else if (this.format == ChatColor.ITALIC) {
            colorGenerator.italic();
        } else if (this.format == ChatColor.RESET) {
            return this.getPlayerChatColor(baseColor, futureTokens);
        }
        return null;
    }

    @Override
    public boolean hasFormatCodes() {
        return true;
    }

    private FormattedColorGenerator getPlayerChatColor(String baseColor, List<Token> futureTokens) {
        List<Token> chatColorTokens = new ArrayList<>();
        for (int i = 0; i < baseColor.length(); i++) {
            String substring = baseColor.substring(i);
            for (Tokenizer<?> tokenizer : Tokenizers.getBundleValues(Tokenizers.COLORS_BUNDLE)) {
                Token token = tokenizer.tokenize(null, null, substring, false);
                if (token != null) {
                    i += token.getOriginalContent().length() - 1;
                    chatColorTokens.add(token);
                    break;
                }
            }
        }

        FormattedColorGenerator colorGenerator = new FormattedColorGenerator(null);
        for (Token token : chatColorTokens) {
            if (token.hasColorGenerator()) {
                FormattedColorGenerator newColorGenerator = new FormattedColorGenerator(token.getColorGenerator(futureTokens));
                colorGenerator.copyFormatsTo(newColorGenerator);
                colorGenerator = newColorGenerator;
            } else if (token.hasFormatCodes() && token instanceof FormatToken) {
                FormatToken formatToken = (FormatToken) token;
                if (formatToken.format == ChatColor.MAGIC) {
                    colorGenerator.obfuscated();
                } else if (formatToken.format == ChatColor.BOLD) {
                    colorGenerator.bold();
                } else if (formatToken.format == ChatColor.STRIKETHROUGH) {
                    colorGenerator.strikethrough();
                } else if (formatToken.format == ChatColor.UNDERLINE) {
                    colorGenerator.underline();
                } else if (formatToken.format == ChatColor.ITALIC) {
                    colorGenerator.italic();
                } else if (formatToken.format == ChatColor.RESET) {
                    colorGenerator = new FormattedColorGenerator(null);
                }
            }
        }

        return colorGenerator;
    }
    
}
