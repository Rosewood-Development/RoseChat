package dev.rosewood.rosechat.message.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.replacement.Replacement;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.decorator.ClickDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.FontDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrefixedReplacementTokenizer extends Tokenizer {

    public PrefixedReplacementTokenizer() {
        super("prefixed_replacement");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        for (Replacement replacement : RoseChatAPI.getInstance().getReplacements()) {
            // Ignore non-prefixed and inline replacements.
            if (replacement.getInput().getPrefix() == null) continue;
            if (replacement.getInput().getInlinePrefix() != null) continue;

            String prefix = replacement.getInput().getPrefix();
            String suffix = replacement.getInput().getSuffix();
            if (!input.startsWith(prefix)) {
                if (replacement.getInput().isRegex()) {
                    // Match for the prefix.
                    Matcher matcher = RoseChatAPI.getInstance().getReplacementManager().getCompiledPatterns().get(replacement.getId() + "-prefix").matcher(input);
                    if (!matcher.find() || !input.startsWith(matcher.group())) continue;
                    prefix = matcher.group();

                    Matcher suffixMatcher = RoseChatAPI.getInstance().getReplacementManager().getCompiledPatterns().get(replacement.getId() + "-suffix").matcher(input);
                    if (!suffixMatcher.find()) continue;
                    suffix = suffixMatcher.group();
                } else {
                    continue;
                }
            }

            if (!MessageUtils.hasExtendedTokenPermission(params, "rosechat.replacements", "rosechat.replacement." + replacement.getId()))
                return null;

            if (suffix != null) {
                if (!input.contains(suffix)) continue;
                int endIndex = input.lastIndexOf(suffix) + suffix.length();
                String originalContent = input.substring(0, endIndex);
                String content = input.substring(prefix.length(), input.lastIndexOf(suffix));
                return this.createTagToken(params, originalContent, content, replacement, prefix);
            }

            String originalContent = null;
            String tagContent;
            if (replacement.getOutput().shouldTagOnlinePlayers()) {
                tagContent = input.substring(prefix.length());
            } else {
                int endIndex = input.indexOf(" ");
                if (endIndex == -1) endIndex = input.length();
                originalContent = input.substring(0, endIndex);
                tagContent = input.substring(prefix.length(), endIndex);
            }

            return this.createTagToken(params, originalContent, tagContent, replacement, prefix);
        }

        return null;
    }

    private TokenizerResult createTagToken(TokenizerParams params, String originalContent, String content, Replacement replacement, String prefix) {
        String output = replacement.getOutput().getText();
        CustomPlaceholder placeholder = RoseChatAPI.getInstance().getPlaceholderManager().getPlaceholder(output.substring(1, output.length() - 1));

        RosePlayer placeholderViewer = null;
        if (replacement.getOutput().shouldTagOnlinePlayers()) {
            // Try to find a player, if we don't find a player, stop at the first space, otherwise consume the entire player tag string.
            DetectedPlayer detectedTaggedPlayer = this.matchPartialPlayer(content, replacement.getId());
            if (detectedTaggedPlayer != null) {
                originalContent = prefix + content.substring(0, detectedTaggedPlayer.consumedTextLength());
                Player taggedPlayer = detectedTaggedPlayer.player();
                placeholderViewer = new RosePlayer(taggedPlayer);
                params.getOutputs().getTaggedPlayers().add(taggedPlayer.getUniqueId());
                if (replacement.getOutput().getSound() != null) params.getOutputs().setTagSound(replacement.getOutput().getSound());
            } else {
                // No player was found, use the tag string up until the first space instead.
                int endIndex = content.indexOf(" ");
                if (endIndex == -1) endIndex = content.length();
                content = content.substring(0, endIndex);
                originalContent = prefix + content;
            }
        }

        if (placeholderViewer == null)
            placeholderViewer = new RosePlayer(content, "default");

        StringPlaceholders placeholders = MessageUtils.getSenderViewerPlaceholders(params.getSender(), placeholderViewer, params.getChannel())
                .add("tagged", content)
                .add("group_0", originalContent)
                .add("group_1", content).build();

        if (placeholder != null) {
            List<String> formattedHover = new ArrayList<>();
            if (placeholder.get("hover") != null) {
                List<String> hover = placeholder.get("hover").parseToStringList(params.getSender(), params.getReceiver(), placeholders);
                for (String s : hover) {
                    formattedHover.add(placeholders.apply(s));
                }
            }

            HoverEvent.Action hoverAction = formattedHover.isEmpty() ? null : placeholder.get("hover").getHoverAction();

            StringBuilder contentBuilder = new StringBuilder();
            content = placeholders.apply(placeholder.get("text").parseToString(params.getSender(), placeholderViewer, placeholders));
            if (replacement.getOutput().shouldMatchLength()) {
                if (!formattedHover.isEmpty()) {
                    String colorlessHover = TextComponent.toPlainText(RoseChatAPI.getInstance().parse(params.getSender(), params.getReceiver(), formattedHover.get(0)));
                    contentBuilder.append(String.valueOf(content).repeat(colorlessHover.length()));
                }
            } else {
                contentBuilder.append(content);
            }

            content = contentBuilder.toString();

            String click = placeholder.get("click") == null ? null : placeholders.apply(placeholder.get("click").parseToString(params.getSender(), params.getReceiver(), placeholders));
            ClickEvent.Action clickAction = placeholder.get("click") == null ? null : placeholder.get("click").getClickAction();

            StringPlaceholders.Builder groupPlaceholders = StringPlaceholders.builder();
            if (replacement.getInput().isRegex()) {
                Matcher matcher = RoseChatAPI.getInstance().getReplacementManager().getCompiledPatterns().get(replacement.getId() + "-prefix").matcher(replacement.getInput().getPrefix());
                if (matcher.find() && matcher.groupCount() != 0) {
                    for (int i = 0; i < matcher.groupCount() + 1; i++) {
                        if (matcher.group(i) == null) continue;
                        content = content.replace("%group_" + i + "%", matcher.group(i));
                        groupPlaceholders.add("group_" + i, matcher.group(i));
                    }
                }
            }

            // "tagged" placeholder
            Token.Builder tokenBuilder = Token.group(content)
                    .decorate(FontDecorator.of(replacement.getOutput().getFont()))
                    .placeholder("message", originalContent)
                    .placeholder("extra", originalContent)
                    .placeholder("tagged", "%group_1%")
                    .placeholders(groupPlaceholders.build())
                    .encapsulate()
                    .ignoreTokenizer(this);

            if (!formattedHover.isEmpty())
                tokenBuilder.decorate(HoverDecorator.of(hoverAction, formattedHover));
            if (click != null)
                tokenBuilder.decorate(ClickDecorator.of(clickAction, click));

            return new TokenizerResult(tokenBuilder.build(), originalContent.length());
        }

        content = replacement.getOutput().getText();
        String hover = replacement.getOutput().getHover();

        StringBuilder contentBuilder = new StringBuilder();
        content = placeholders.apply(content);
        if (replacement.getOutput().shouldMatchLength()) {
            if (hover != null) {
                String colorlessHover = TextComponent.toPlainText(RoseChatAPI.getInstance().parse(params.getSender(), params.getReceiver(), hover));
                contentBuilder.append(String.valueOf(content).repeat(colorlessHover.length()));
            }
        } else {
            contentBuilder.append(content);
        }

        content = contentBuilder.toString();

        StringPlaceholders.Builder groupPlaceholders = StringPlaceholders.builder();
        if (replacement.getInput().isRegex()) {
            Matcher matcher = RoseChatAPI.getInstance().getReplacementManager().getCompiledPatterns().get(replacement.getId() + "-prefix").matcher(replacement.getInput().getPrefix());
            if (matcher.find() && matcher.groupCount() != 0) {
                for (int i = 0; i < matcher.groupCount() + 1; i++) {
                    if (matcher.group(i) == null) continue;
                    content = content.replace("%group_" + i + "%", matcher.group(i));
                    groupPlaceholders.add("group_" + i, matcher.group(i));
                }
            }
        }

        return new TokenizerResult(Token.group(content)
                .decorate(HoverDecorator.of(HoverEvent.Action.SHOW_TEXT, hover))
                .decorate(FontDecorator.of(replacement.getOutput().getFont()))
                .placeholder("message", originalContent)
                .placeholder("extra", originalContent)
                .placeholder("tagged", "%group_1%")
                .placeholders(groupPlaceholders.build())
                .encapsulate()
                .ignoreTokenizer(this)
                .build(), originalContent.length());
    }

    private DetectedPlayer matchPartialPlayer(String input, String id) {
        // Check displaynames first
        for (Player player : Bukkit.getOnlinePlayers()) {
            int matchLength = this.getMatchLength(input, ChatColor.stripColor(player.getDisplayName()), id);
            if (matchLength != -1)
                return new PrefixedReplacementTokenizer.DetectedPlayer(player, matchLength);
        }

        // Then usernames
        for (Player player : Bukkit.getOnlinePlayers()) {
            int matchLength = this.getMatchLength(input, player.getName(), id);
            if (matchLength != -1)
                return new PrefixedReplacementTokenizer.DetectedPlayer(player, matchLength);
        }

        return null;
    }

    /**
     * Tries to find a match for a player in the input string.
     * Skips over color codes in the player name.
     *
     * @param input The input string to search for a player
     * @param playerName The name of the player to match
     * @return The length of the content that matches, or -1 if a match wasn't found
     */
    private int getMatchLength(String input, String playerName, String id) {
        Pattern stopPattern = RoseChatAPI.getInstance().getReplacementManager().getCompiledPatterns().getOrDefault(id + "-stop", Pattern.compile(MessageUtils.PUNCTUATION_REGEX));

        int matchLength = 0;
        for (int i = 0, j = 0; i < input.length() && j < playerName.length(); i++, j++) {
            int inputChar = Character.toUpperCase(input.codePointAt(i));
            int playerChar = Character.toUpperCase(playerName.codePointAt(j));
            if (inputChar == playerChar) {
                matchLength++;
            } else if (i > 0 && (Character.isSpaceChar(inputChar) ||  Pattern.matches(stopPattern.pattern(), String.valueOf(Character.toChars(inputChar))))) {
                return matchLength;
            } else {
                return -1;
            }
        }

        return matchLength;
    }

    private record DetectedPlayer(Player player, int consumedTextLength) { }

}
