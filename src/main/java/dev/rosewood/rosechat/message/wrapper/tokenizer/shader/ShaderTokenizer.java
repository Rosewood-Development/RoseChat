package dev.rosewood.rosechat.message.wrapper.tokenizer.shader;

import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import org.bukkit.Bukkit;
import java.util.regex.Matcher;

public class ShaderTokenizer implements Tokenizer<Token> {

    @Override
    public Token tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions) {
        if (input.startsWith("#")) {
            Matcher matcher = MessageUtils.HEX_REGEX.matcher(input);
            if (!matcher.find()) return null;
            String match = input.substring(matcher.start(), matcher.end());
            if (input.startsWith(match)) {
                if (!Setting.CORE_SHADER_COLORS.getStringList().contains(match)) return null;
                String freeHex = this.findFreeHex(match.substring(1));

                return new Token(new Token.TokenSettings(match).content("#" + freeHex).ignoreTokenizer(this));
            }
        }

        return null;
    }

    private String findFreeHex(String hex) {
        String nextHex = Integer.toHexString(Integer.parseInt(hex, 16) - 1);
        if (Setting.CORE_SHADER_COLORS.getStringList().stream().anyMatch(x -> x.equalsIgnoreCase("#" + nextHex))) return findFreeHex(nextHex);
        return nextHex;
    }
}
