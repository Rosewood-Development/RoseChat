package dev.rosewood.rosechat.message.tokenizer.shader;

import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class ShaderTokenizer extends Tokenizer {

    private final List<String> shaderColors;

    public ShaderTokenizer() {
        super("shader");

        this.shaderColors = Settings.CORE_SHADER_COLORS.get().stream().map(String::toLowerCase).toList();
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        String input = params.getInput();
        Matcher matcher = MessageUtils.HEX_REGEX.matcher(input);

        List<TokenizerResult> results = new ArrayList<>();

        while (matcher.find()) {
            int start = matcher.start();
            String match = matcher.group();

            boolean escape = (start > 0) && input.charAt(start - 1) == MessageUtils.ESCAPE_CHAR && params.getSender().hasPermission("rosechat.escape");
            if (escape)
                continue;

            if (!this.shaderColors.contains(match.toLowerCase()))
                continue;

            String freeHex = findFreeHex(match.substring(1));
            results.add(new TokenizerResult(Token.group("#" + freeHex).build(), start, match.length()));
        }

        return results;
    }

    public static String findFreeHex(String hex) {
        String nextHex = Integer.toHexString(Integer.parseInt(hex, 16) - 1);
        if (Settings.CORE_SHADER_COLORS.get().stream().anyMatch(x -> x.equalsIgnoreCase("#" + nextHex)))
            return findFreeHex(nextHex);

        return nextHex;
    }

}
