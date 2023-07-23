package dev.rosewood.rosechat.message.tokenizer.tag;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.Tag;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.decorator.ClickDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import dev.rosewood.rosechat.placeholders.RoseChatPlaceholder;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.regex.Pattern;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TagTokenizer extends Tokenizer {

    public TagTokenizer() {
        super("tag");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        for (Tag tag : RoseChatAPI.getInstance().getTags()) {
            if (input.startsWith(tag.getPrefix())) {
                if (!MessageUtils.hasExtendedTokenPermission(params, "rosechat.tags", "rosechat.tag." + tag.getId()))
                    return null;

                if (tag.getSuffix() != null) {
                    if (!input.contains(tag.getSuffix())) continue;
                    int endIndex = input.lastIndexOf(tag.getSuffix()) + tag.getSuffix().length();
                    String originalContent = input.substring(0, endIndex);
                    String content = input.substring(tag.getPrefix().length(), input.lastIndexOf(tag.getSuffix()));
                    return this.createTagToken(params, originalContent, content, tag);
                }

                String originalContent = null;
                String tagContent;
                if (tag.shouldTagOnlinePlayers()) {
                    tagContent = input.substring(tag.getPrefix().length());
                } else {
                    int endIndex = input.indexOf(" ");
                    if (endIndex == -1) endIndex = input.length();
                    originalContent = input.substring(0, endIndex);
                    tagContent = input.substring(tag.getPrefix().length(), endIndex);
                }
                return this.createTagToken(params, originalContent, tagContent, tag);
            }
        }

        return null;
    }

    private TokenizerResult createTagToken(TokenizerParams params, String originalContent, String content, Tag tag) {
        RoseChatPlaceholder placeholder = RoseChatAPI.getInstance().getPlaceholderManager().getPlaceholder(tag.getFormat());
        if (placeholder == null) return null;

        RosePlayer placeholderViewer = null;
        if (tag.shouldTagOnlinePlayers()) {
            // Try to find a player, if we don't find a player, stop at the first space, otherwise consume the entire player tag string
            DetectedPlayer detectedTaggedPlayer = this.matchPartialPlayer(content);
            if (detectedTaggedPlayer != null) {
                originalContent = tag.getPrefix() + content.substring(0, detectedTaggedPlayer.consumedTextLength());
                Player taggedPlayer = detectedTaggedPlayer.player();
                placeholderViewer = new RosePlayer(taggedPlayer);
                params.getOutputs().getTaggedPlayers().add(taggedPlayer.getUniqueId());
                if (tag.getSound() != null) params.getOutputs().setTagSound(tag.getSound());
            } else {
                // No player was found, use the tag string up until the first space instead
                int endIndex = content.indexOf(" ");
                if (endIndex == -1) endIndex = content.length();
                content = content.substring(0, endIndex);
                originalContent = tag.getPrefix() + content;
            }
        }

        if (placeholderViewer == null)
            placeholderViewer = new RosePlayer(content, "default");

        StringPlaceholders placeholders = MessageUtils.getSenderViewerPlaceholders(params.getSender(), placeholderViewer, params.getChannel())
                .add("tagged", content).build();

        String hover = placeholder.getHover() == null ? null : placeholders.apply(placeholder.getHover().parseToString(params.getSender(), params.getReceiver(), placeholders));

        StringBuilder contentBuilder = new StringBuilder();
        content = placeholders.apply(placeholder.getText().parseToString(params.getSender(), placeholderViewer, placeholders));
        if (tag.shouldMatchLength()) {
            if (hover != null) {
                String colorlessHover = TextComponent.toPlainText(RoseChatAPI.getInstance().parse(params.getSender(), params.getReceiver(), hover));
                contentBuilder.append(String.valueOf(content).repeat(colorlessHover.length()));
            }
        } else {
            contentBuilder.append(content);
        }

        content = contentBuilder.toString();

        String click = placeholder.getClick() == null ? null : placeholders.apply(placeholder.getClick().parseToString(params.getSender(), params.getReceiver(), placeholders));
        ClickEvent.Action clickAction = placeholder.getClick() == null ? null : placeholder.getClick().parseToAction(params.getSender(), params.getReceiver(), placeholders);

        Token.Builder tokenBuilder = Token.group(content).encapsulate().ignoreTokenizer(this);
        if (hover != null)
            tokenBuilder.decorate(HoverDecorator.of(HoverEvent.Action.SHOW_TEXT, hover));
        if (click != null)
            tokenBuilder.decorate(ClickDecorator.of(clickAction, click));

        return new TokenizerResult(tokenBuilder.build(), originalContent.length());
    }

    private DetectedPlayer matchPartialPlayer(String input) {
        // Check displaynames first
        for (Player player : Bukkit.getOnlinePlayers()) {
            int matchLength = this.getMatchLength(input, ChatColor.stripColor(player.getDisplayName()));
            if (matchLength != -1)
                return new DetectedPlayer(player, matchLength);
        }

        // Then usernames
        for (Player player : Bukkit.getOnlinePlayers()) {
            int matchLength = this.getMatchLength(input, player.getName());
            if (matchLength != -1)
                return new DetectedPlayer(player, matchLength);
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
    private int getMatchLength(String input, String playerName) {
        int matchLength = 0;
        for (int i = 0, j = 0; i < input.length() && j < playerName.length(); i++, j++) {
            int inputChar = Character.toUpperCase(input.codePointAt(i));
            int playerChar = Character.toUpperCase(playerName.codePointAt(j));
            if (inputChar == playerChar) {
                matchLength++;
            } else if (i > 0 && (Character.isSpaceChar(inputChar) || Pattern.matches(MessageUtils.PUNCTUATION_REGEX, String.valueOf(Character.toChars(inputChar))))) {
                return matchLength;
            } else {
                return -1;
            }
        }
        return matchLength;
    }

    private record DetectedPlayer(Player player, int consumedTextLength) { }

}
