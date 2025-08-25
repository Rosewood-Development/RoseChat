package dev.rosewood.rosechat.message.tokenizer.placeholder;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class PAPIPlaceholderTokenizer extends Tokenizer {

    private static final Pattern PATTERN = Pattern.compile("%(.*?)%");
    private final boolean isBungee;

    public PAPIPlaceholderTokenizer(boolean isBungee) {
        super(isBungee ? "bungee_papi" : "papi");
        this.isBungee = isBungee;
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        String input = params.getInput();

        List<TokenizerResult> results = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(input);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            String placeholder = matcher.group();
            String placeholderPermission = placeholder.replaceFirst("_", ".");
            if (!this.hasExtendedTokenPermission(params, "rosechat.placeholders", "rosechat.placeholder." + placeholderPermission))
                continue;

            if (start > 0 && input.charAt(start - 1) == MessageUtils.ESCAPE_CHAR && params.getSender().hasPermission("rosechat.escape")) {
                results.add(new TokenizerResult(Token.text(placeholder), start - 1, placeholder.length() + 1));
                continue;
            }

            String content;
            if (placeholder.startsWith("%other_") && !this.isBungee) {
                OfflinePlayer offlineReceiver = Bukkit.getOfflinePlayer(params.getReceiver().getRealName());
                content = PlaceholderAPIHook.applyRelationalPlaceholders(params.getSender().asPlayer(), params.getReceiver().asPlayer(), placeholder.replaceFirst("other_", ""));
                content = PlaceholderAPIHook.applyPlaceholders(offlineReceiver, content.replaceFirst("other_", ""));
            } else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(params.getSender().getRealName());
                content = PlaceholderAPIHook.applyRelationalPlaceholders(params.getSender().asPlayer(), params.getReceiver().asPlayer(), placeholder);
                content = PlaceholderAPIHook.applyPlaceholders(offlinePlayer, content);
            }

            // If we haven't changed, don't allow tokenizing this text anymore
            if (Objects.equals(content, placeholder)) {
                results.add(new TokenizerResult(Token.text(placeholder), start, end));
                continue;
            }

            // Encapsulate if the placeholder only contains a colour or ends with a colour
            boolean encapsulate = true;

            // Ignore everything that definitely isn't a colour.
            if (content.contains(ChatColor.COLOR_CHAR + "") || content.contains("&") || content.contains("#") || content.contains("<") || content.contains("{")) {
                Matcher legacyMatcher = MessageUtils.LEGACY_REGEX_COMBINED.matcher(content);

                while (legacyMatcher.find()) {
                    if (content.trim().equalsIgnoreCase(legacyMatcher.group()) || content.trim().endsWith(legacyMatcher.group())) {
                        encapsulate = false;
                    }
                }

                if (encapsulate) {
                    Matcher hexMatcher = MessageUtils.SPIGOT_HEX_REGEX_COMBINED.matcher(content);
                    while (hexMatcher.find()) {
                        if (content.trim().equalsIgnoreCase(hexMatcher.group()) || content.trim().endsWith(hexMatcher.group())) {
                            encapsulate = false;
                        }
                    }
                }

                if (encapsulate) {
                    Matcher gradientMatcher = MessageUtils.GRADIENT_PATTERN.matcher(content);
                    while (gradientMatcher.find()) {
                        if (content.trim().equalsIgnoreCase(gradientMatcher.group()) || content.trim().endsWith(gradientMatcher.group())) {
                            encapsulate = false;
                        }
                    }
                }

                if (encapsulate) {
                    Matcher rainbowMatcher = MessageUtils.RAINBOW_PATTERN.matcher(content);
                    while (rainbowMatcher.find()) {
                        if (content.trim().equals(rainbowMatcher.group()) || content.trim().endsWith(rainbowMatcher.group())) {
                            encapsulate = false;
                        }
                    }
                }
            }

            Token.Builder token = Token.group(content);
            if (encapsulate)
                token.encapsulate();

            results.add(new TokenizerResult(token.build(), start, placeholder.length()));
        }

        return results;
    }

}
