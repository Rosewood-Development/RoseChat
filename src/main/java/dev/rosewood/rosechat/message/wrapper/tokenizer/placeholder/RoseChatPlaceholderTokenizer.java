package dev.rosewood.rosechat.message.wrapper.tokenizer.placeholder;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Token;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizer;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoseChatPlaceholderTokenizer implements Tokenizer<Token> {

    private static final Pattern RC_PATTERN = Pattern.compile("\\{(.*?)\\}");

    @Override
    public Token tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions) {
        if (!input.startsWith("{")) return null;

        Matcher matcher = RC_PATTERN.matcher(input);
        if (matcher.find()) {
            String placeholder = input.substring(matcher.start() + 1, matcher.end() - 1);
            if (!hasExtendedPermission(messageWrapper, ignorePermissions, "rosechat.placeholders", "rosechat.placeholder.rosechat." + placeholder)) return null;

            String originalContent = input.substring(matcher.start(), matcher.end());
            CustomPlaceholder customPlaceholder = RoseChatAPI.getInstance().getPlaceholderManager().getPlaceholder(originalContent.substring(1, originalContent.length() - 1));
            if (customPlaceholder == null) return null;

            StringPlaceholders placeholders = MessageUtils.getSenderViewerPlaceholders(messageWrapper.getSender(), viewer, messageWrapper.getGroup(), messageWrapper.getPlaceholders()).build();
            String content = placeholders.apply(customPlaceholder.getText().parse(messageWrapper.getSender(), viewer, placeholders));
            String hover = customPlaceholder.getHover() == null ? null : placeholders.apply(customPlaceholder.getHover().parse(messageWrapper.getSender(), viewer, placeholders));
            String click = customPlaceholder.getClick() == null ? null : placeholders.apply(customPlaceholder.getClick().parse(messageWrapper.getSender(), viewer, placeholders));
            ClickEvent.Action clickAction = customPlaceholder.getClick() == null ? null : customPlaceholder.getClick().parseToAction(messageWrapper.getSender(), viewer, placeholders);

            Token.TokenSettings tokenSettings = new Token.TokenSettings(originalContent).content(content).hover(hover).hoverAction(HoverEvent.Action.SHOW_TEXT).click(click).clickAction(clickAction);
            if (originalContent.equals(content))
                tokenSettings.ignoreTokenizer(this);

            return new Token(tokenSettings);
        }
        return null;
    }

}
