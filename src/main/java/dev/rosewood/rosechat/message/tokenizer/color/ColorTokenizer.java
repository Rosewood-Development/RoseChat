package dev.rosewood.rosechat.message.tokenizer.color;

import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosegarden.utils.HexUtils;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class ColorTokenizer implements Tokenizer<ColorToken> {

    private static final List<Character> COLOR_PREFIX_CHARACTERS = Arrays.asList('&', ChatColor.COLOR_CHAR, '#', '{', '<');

    @Override
    public ColorToken tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
        if (!COLOR_PREFIX_CHARACTERS.contains(input.charAt(0))) // Fail fast if the input doesn't start with a color code
            return null;

        boolean isDefaultColor = MessageUtils.hasDefaultColor(input, roseMessage);

        // Run this first since it can contain legacy tokens
        ColorToken spigotHexToken = this.parseMatcher(MessageUtils.SPIGOT_HEX_REGEX, input);
        if (spigotHexToken != null)
            return (ignorePermissions || isDefaultColor || MessageUtils.hasTokenPermission(roseMessage, "rosechat.color")) ?
                    spigotHexToken : new ColorToken(spigotHexToken.getOriginalContent(), null);

        ColorToken legacyToken = this.parseMatcher(MessageUtils.VALID_LEGACY_REGEX, input);
        if (legacyToken != null) {
            if (ConfigurationManager.Setting.USE_PER_COLOR_PERMISSIONS.getBoolean()) {
                char colorCode = legacyToken.getOriginalContent().charAt(1);

                return ignorePermissions || isDefaultColor || (MessageUtils.hasTokenPermission(roseMessage, "rosechat.color")
                        && MessageUtils.hasTokenPermission(roseMessage, "rosechat." + ChatColor.getByChar(Character.toLowerCase(colorCode)).getName().toLowerCase())) ?
                        legacyToken : new ColorToken(legacyToken.getOriginalContent(), null);
            } else {
                return ignorePermissions || isDefaultColor || MessageUtils.hasTokenPermission(roseMessage, "rosechat.color") ?
                        legacyToken : new ColorToken(legacyToken.getOriginalContent(), null);
            }
        }

        ColorToken hexToken = this.parseMatcher(MessageUtils.HEX_REGEX, input);
        if (hexToken != null)
            return ignorePermissions || isDefaultColor || MessageUtils.hasTokenPermission(roseMessage, "rosechat.hex") ?
                    hexToken : new ColorToken(hexToken.getOriginalContent(), null);

        ColorToken spigotHexTokenParsed = this.parseMatcher(MessageUtils.SPIGOT_HEX_REGEX_PARSED, input);
        if (spigotHexTokenParsed != null)
            return spigotHexTokenParsed; // Bypass permission checks since players can't input this themselves

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