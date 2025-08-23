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

        this.shaderColors = new ArrayList<>();
        for (String color : Settings.CORE_SHADER_COLORS.get()) {
            this.shaderColors.add(color.toLowerCase());
        }
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        String rawInput = params.getInput();
        String input = rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR ? rawInput.substring(1) : rawInput;
        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR && !params.getSender().hasPermission("rosechat.escape"))
            return null;

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

        if (rawInput.charAt(0) == MessageUtils.ESCAPE_CHAR)
            return List.of(new TokenizerResult(Token.text(match), match.length() + 1));

        String freeHex = findFreeHex(match.substring(1));
        return List.of(new TokenizerResult(Token.group("#" + freeHex).build(), match.length()));
    }

    public static String findFreeHex(String hex) {
        String nextHex = Integer.toHexString(Integer.parseInt(hex, 16) - 1);
        if (Settings.CORE_SHADER_COLORS.get().stream().anyMatch(x -> x.equalsIgnoreCase("#" + nextHex)))
            return findFreeHex(nextHex);

        return nextHex;
    }

}
