package dev.rosewood.rosechat.message.tokenizer.shader;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import java.util.regex.Matcher;

public class ShaderTokenizer extends Tokenizer {

    public ShaderTokenizer() {
        super("shader");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("#")) return null;

        Matcher matcher = MessageUtils.HEX_REGEX.matcher(input);
        if (!matcher.find() || matcher.start() != 0) return null;

        String match = input.substring(0, matcher.end());
        if (!input.startsWith(match)) return null;

        if (!Setting.CORE_SHADER_COLORS.getStringList().contains(match)) return null;
        String freeHex = findFreeHex(match.substring(1));

        return new TokenizerResult(Token.group("#" + freeHex).build(), match.length());
    }

    public static String findFreeHex(String hex) {
        String nextHex = Integer.toHexString(Integer.parseInt(hex, 16) - 1);
        if (Setting.CORE_SHADER_COLORS.getStringList().stream().anyMatch(x -> x.equalsIgnoreCase("#" + nextHex)))
            return findFreeHex(nextHex);

        return nextHex;
    }

}
