package dev.rosewood.rosechat.message.tokenizer.replacement;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.replacement.Replacement;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.decorator.FontDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import net.md_5.bungee.api.chat.HoverEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InlineReplacementTokenizer extends Tokenizer {

    public InlineReplacementTokenizer() {
        super("inline_replacement");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        for (Replacement replacement : RoseChatAPI.getInstance().getReplacements()) {
            // Ignore non-inline replacements
            if (replacement.getInput().getInlinePrefix() == null
                    || replacement.getInput().getInlineSuffix() == null
                    || replacement.getInput().getPrefix() == null
                    || replacement.getInput().getSuffix() == null)
                continue;

            String prefix = replacement.getInput().getPrefix();
            String suffix = replacement.getInput().getSuffix();
            String inlinePrefix = replacement.getInput().getInlinePrefix();
            String inlineSuffix = replacement.getInput().getInlineSuffix();
            if (!input.startsWith(prefix))
                continue;

            if (!this.hasExtendedTokenPermission(params, "rosechat.replacements", "rosechat.replacement." + replacement.getId()))
                return null;

            String outerRegex = "(?:" + Pattern.quote(prefix) + "(.*?)" + Pattern.quote(suffix) + ")"
                    + Pattern.quote(inlinePrefix) + "(.*?)" + Pattern.quote(inlineSuffix);
            Matcher matcher = Pattern.compile(outerRegex).matcher(input);
            if (!matcher.find() || matcher.start() != 0)
                return null;

            String originalContent = matcher.group();
            String content = matcher.group(1);
            String inline = matcher.group(2);

            if (replacement.getInput().isContentRegex()) {
                Matcher contentMatcher = RoseChatAPI.getInstance().getReplacementManager().getCompiledPatterns().get(replacement.getId() + "-text").matcher(content);
                if (!contentMatcher.find())
                    return null;
            }

            if (replacement.getInput().isInlineRegex()) {
                Matcher inlineMatcher = RoseChatAPI.getInstance().getReplacementManager().getCompiledPatterns().get(replacement.getId() + "-text").matcher(inline);
                if (!inlineMatcher.find())
                    return null;
            }

            Token.Builder token = Token.group(replacement.getOutput().getText())
                    .decorate(FontDecorator.of(replacement.getOutput().getFont()))
                    .placeholder("group_0", originalContent)
                    .placeholder("message", originalContent)
                    .placeholder("group_1", matcher.group(1))
                    .placeholder("group_2", matcher.group(2))
                    .encapsulate()
                    .ignoreTokenizer(this);

            if (replacement.getOutput().getHover() != null)
                token.decorate(HoverDecorator.of(HoverEvent.Action.SHOW_TEXT, replacement.getOutput().getHover()));

            return new TokenizerResult(token.build(), originalContent.length());
        }

        return null;
    }

}
