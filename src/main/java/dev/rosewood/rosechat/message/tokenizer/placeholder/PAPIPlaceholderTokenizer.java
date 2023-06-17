package dev.rosewood.rosechat.message.tokenizer.placeholder;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PAPIPlaceholderTokenizer implements Tokenizer<Token> {

    private static final Pattern PAPI_PATTERN = Pattern.compile("\\%(.*?)\\%");
    private final boolean isBungee;

    public PAPIPlaceholderTokenizer(boolean isBungee) {
        this.isBungee = isBungee;
    }

    @Override
    public Token tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
        if (!input.startsWith("%")) return null;

        Matcher matcher = PAPI_PATTERN.matcher(input);
        if (matcher.find()) {
            if (matcher.start() != 0) return null;
            String placeholder = input.substring(1, matcher.end() - 1);
            String placeholderPermission = placeholder.replaceFirst("_", ".");
            if (!ignorePermissions
                    && !MessageUtils.hasExtendedTokenPermission(roseMessage, "rosechat.placeholders", "rosechat.placeholder." + placeholderPermission))
                return null;

            String originalContent = matcher.group();

            String content;
            if (originalContent.startsWith("%other_") && !this.isBungee) {
                if (!viewer.isPlayer()) content = originalContent;
                else content = PlaceholderAPIHook.applyPlaceholders(viewer.asPlayer(), originalContent.replaceFirst("other_", ""));
            } else {
                content = PlaceholderAPIHook.applyPlaceholders(roseMessage.getSender().asPlayer(), originalContent);
            }

            content = content.replace(ChatColor.COLOR_CHAR, '&');

            Token.TokenSettings tokenSettings = new Token.TokenSettings(originalContent).content(content).noCaching();
            if (originalContent.equals(content))
                tokenSettings.ignoreTokenizer(this);

            return new Token(tokenSettings);
        }

        return null;
    }

}
