package dev.rosewood.rosechat.message.tokenizer.shader;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class ShaderTokenizer extends Tokenizer {

    private List<String> shaderColors;

    public ShaderTokenizer() {
        super("shader");

        this.shaderColors = new ArrayList<>();
        for (String color : Setting.CORE_SHADER_COLORS.getStringList()) {
            this.shaderColors.add(color.toLowerCase());
        }
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("#"))
            return null;

        Matcher matcher = MessageUtils.HEX_REGEX.matcher(input);
        if (!matcher.find() || matcher.start() != 0)
            return null;

        String match = input.substring(0, matcher.end());
        if (!input.startsWith(match))
            return null;

        if (!this.shaderColors.contains(match.toLowerCase()))
            return null;

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
