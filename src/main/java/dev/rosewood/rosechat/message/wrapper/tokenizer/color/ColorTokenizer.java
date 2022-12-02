package dev.rosewood.rosechat.message.wrapper.tokenizer.color;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import dev.rosewood.rosegarden.utils.HexUtils;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class ColorTokenizer implements Tokenizer<ColorToken> {

    private static final List<Character> COLOR_PREFIX_CHARACTERS = Arrays.asList('&', ChatColor.COLOR_CHAR, '#', '{', '<');

    @Override
    public ColorToken tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions) {
        if (!COLOR_PREFIX_CHARACTERS.contains(input.charAt(0))) // Fail fast if the input doesn't start with a color code
            return null;

        boolean color = MessageUtils.hasDefaultColor(input, messageWrapper);

        // Run this first since it can contain legacy tokens
        ColorToken spigotHexToken = this.parseMatcher(MessageUtils.SPIGOT_HEX_REGEX, input);
        if (spigotHexToken != null) return this.hasPermission(messageWrapper, ignorePermissions || color, "rosechat.color") ?
                spigotHexToken : new ColorToken(spigotHexToken.getOriginalContent(), null);

        ColorToken legacyToken = this.parseMatcher(MessageUtils.VALID_LEGACY_REGEX, input);
        if (legacyToken != null) return this.hasPermission(messageWrapper, ignorePermissions || color, "rosechat.color") ?
                legacyToken : new ColorToken(legacyToken.getOriginalContent(), null);

        ColorToken hexToken = this.parseMatcher(MessageUtils.HEX_REGEX, input);
        if (hexToken != null) return this.hasPermission(messageWrapper, ignorePermissions || color, "rosechat.hex") ?
                hexToken : new ColorToken(hexToken.getOriginalContent(), null);

        ColorToken spigotHexTokenParsed = this.parseMatcher(MessageUtils.SPIGOT_HEX_REGEX_PARSED, input);
        if (spigotHexTokenParsed != null) return spigotHexTokenParsed; // Bypass permission checks since players can't input this themselves

        return null;
    }

    private ColorToken parseMatcher(Pattern pattern, String input) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            if (matcher.start() != 0) return null;
            String content = input.substring(0, matcher.end());
            return new ColorToken(content, this.fromString(content));
        }

        return null;
    }

    private ChatColor fromString(String string) {
        if (string.toLowerCase().startsWith("&x") || string.toLowerCase().startsWith("ยงx"))
            return ChatColor.of("#" + string.substring(2).replace("&", "").replace("ยง", ""));

        int hashIndex = string.indexOf('#');
        if (hashIndex == -1)
            return ChatColor.getByChar(string.charAt(1));
        return HexUtils.translateHex(string.substring(hashIndex, hashIndex + 7));
    }

}