package dev.rosewood.rosechat.message.wrapper.tokenizer.format;

import dev.rosewood.rosechat.message.wrapper.tokenizer.FormattedColorGenerator;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizers;
import net.md_5.bungee.api.ChatColor;
import java.util.ArrayList;
import java.util.List;

public class FormatToken extends Token {

    private final ChatColor format;
    private final boolean value;

    public FormatToken(String originalText, ChatColor format, boolean value) {
        super(new TokenSettings(originalText).content(""));

        this.format = format;
        this.value = value;
    }

    public FormatToken(String originalText, ChatColor format) {
        this(originalText, format, true);
    }

    @Override
    public FormattedColorGenerator applyFormatCodes(FormattedColorGenerator colorGenerator, String baseColor, List<Token> futureTokens) {
        if (this.format == ChatColor.MAGIC) {
            colorGenerator.obfuscated(this.value);
        } else if (this.format == ChatColor.BOLD) {
            colorGenerator.bold(this.value);
        } else if (this.format == ChatColor.STRIKETHROUGH) {
            colorGenerator.strikethrough(this.value);
        } else if (this.format == ChatColor.UNDERLINE) {
            colorGenerator.underline(this.value);
        } else if (this.format == ChatColor.ITALIC) {
            colorGenerator.italic(this.value);
        } else if (this.format == ChatColor.RESET) {
            return this.getPlayerChatColor(baseColor, futureTokens);
        }
        return null;
    }

    @Override
    public boolean hasFormatCodes() {
        return this.format != null;
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
                    colorGenerator.obfuscated(formatToken.value);
                } else if (formatToken.format == ChatColor.BOLD) {
                    colorGenerator.bold(formatToken.value);
                } else if (formatToken.format == ChatColor.STRIKETHROUGH) {
                    colorGenerator.strikethrough(formatToken.value);
                } else if (formatToken.format == ChatColor.UNDERLINE) {
                    colorGenerator.underline(formatToken.value);
                } else if (formatToken.format == ChatColor.ITALIC) {
                    colorGenerator.italic(formatToken.value);
                } else if (formatToken.format == ChatColor.RESET) {
                    colorGenerator = new FormattedColorGenerator(null);
                }
            }
        }

        return colorGenerator;
    }

    @Override
    public boolean hasColorGenerator() {
        return this.format != null;
    }

}
