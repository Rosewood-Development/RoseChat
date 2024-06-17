package dev.rosewood.rosechat.message.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.replacement.Replacement;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.decorator.FontDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.HoverEvent;
import java.util.regex.Matcher;

public class ReplacementTokenizer extends Tokenizer {

    public ReplacementTokenizer() {
        super("replacement");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        for (Replacement replacement : RoseChatAPI.getInstance().getReplacements()) {
            // Ignore prefixed and inline replacements.
            if (replacement.getInput().getPrefix() != null || replacement.getInput().getInlinePrefix() != null)
                continue;

            if (replacement.getInput().isRegex()) {
                Matcher matcher = RoseChatAPI.getInstance().getReplacementManager().getCompiledPatterns().get(replacement.getId() + "-text").matcher(input);
                if (!matcher.find())
                    continue;

                // Check permissions
                if (!this.hasExtendedTokenPermission(params, "rosechat.replacements", "rosechat.replacement." + replacement.getId()))
                    return null;

                String originalContent = matcher.group();
                if (!input.startsWith(originalContent))
                    continue;

                String content = replacement.getOutput().getText();

                StringPlaceholders.Builder groupPlaceholders = StringPlaceholders.builder();
                int groups = Math.max(9, matcher.groupCount() + 1);
                for (int i = 0; i < groups; i++) {
                    String groupReplacement = matcher.groupCount() < i || matcher.group(i) == null ? matcher.group(0) : matcher.group(i);
                    content = content.replace("%group_" + i + "%", groupReplacement);
                    groupPlaceholders.add("group_" + i, groupReplacement);
                }

                Token.Builder token = Token.group(content)
                        .decorate(FontDecorator.of(replacement.getOutput().getFont()))
                        .placeholder("message", originalContent)
                        .placeholder("extra", originalContent)
                        .placeholder("tagged", "%group_1%")
                        .placeholders(groupPlaceholders.build());

                if (replacement.getOutput().getHover() != null)
                    token.decorate(HoverDecorator.of(HoverEvent.Action.SHOW_TEXT, replacement.getOutput().getHover()));

                if (!replacement.getOutput().hasColorRetention()) token.encapsulate();

                return new TokenizerResult(token.build(), originalContent.length());
            } else {
                // Continue if the input is not the replacement
                if (!params.getInput().startsWith(replacement.getInput().getText()))
                    continue;

                // Check permissions
                if (!this.hasExtendedTokenPermission(params, "rosechat.replacements", "rosechat.replacement." + replacement.getId()))
                    return null;

                // Return if the replacement is an emoji, and the player has emoji formatting disabled.
                if (replacement.getInput().isEmoji()) {
                    PlayerData playerData = RoseChatAPI.getInstance().getPlayerData(params.getSender().getUUID());
                    if (playerData != null && !playerData.hasEmojis())
                        return null;
                }

                String originalContent = replacement.getInput().getText();
                String content = replacement.getOutput().getText();

                Token.Builder token = Token.group(content)
                        .decorate(FontDecorator.of(replacement.getOutput().getFont()));

                if (replacement.getOutput().getHover() != null)
                    token.decorate(HoverDecorator.of(HoverEvent.Action.SHOW_TEXT, replacement.getOutput().getHover()));

                if (!replacement.getOutput().hasColorRetention())
                    token.encapsulate();

                return new TokenizerResult(token.build(), originalContent.length());
            }
        }

        return null;
    }

}
