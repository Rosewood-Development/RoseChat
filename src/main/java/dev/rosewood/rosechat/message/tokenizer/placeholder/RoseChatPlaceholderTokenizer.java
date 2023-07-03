package dev.rosewood.rosechat.message.tokenizer.placeholder;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.placeholders.RoseChatPlaceholder;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoseChatPlaceholderTokenizer implements Tokenizer {

    public static final Pattern PATTERN = Pattern.compile("^(\\{(.*?)}).*");

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        Matcher matcher = PATTERN.matcher(params.getInput());
        if (matcher.find()) {
            String placeholder = matcher.group(2);
//            if (!ignorePermissions && !MessageUtils.hasExtendedTokenPermission(messageWrapper, "rosechat.placeholders", "rosechat.placeholder.rosechat." + placeholder))
//                return null;

            // Hardcoded special {message} placeholder to inject the player's message
            if (placeholder.equals("message"))
                return new TokenizerResult(Token.builder().content(params.getPlayerInput()).containsPlayerInput().build(), matcher.group(1).length());

            RoseChatPlaceholder roseChatPlaceholder = RoseChatAPI.getInstance().getPlaceholderManager().getPlaceholder(placeholder);
            if (roseChatPlaceholder == null)
                return null;

            StringPlaceholders placeholders = MessageUtils.getSenderViewerPlaceholders(params.getSender(), params.getReceiver(), params.getChannel(), params.getPlaceholders()).build();
            String content = placeholders.apply(roseChatPlaceholder.getText().parseToString(params.getSender(), params.getReceiver(), placeholders));
//            String hover = roseChatPlaceholder.getHover() == null ? null : placeholders.apply(roseChatPlaceholder.getHover().parseToString(messageWrapper.getSender(), viewer, placeholders));
//            String click = roseChatPlaceholder.getClick() == null ? null : placeholders.apply(roseChatPlaceholder.getClick().parseToString(messageWrapper.getSender(), viewer, placeholders));
//            ClickEvent.Action clickAction = roseChatPlaceholder.getClick() == null ? null : roseChatPlaceholder.getClick().parseToAction(messageWrapper.getSender(), viewer, placeholders);

//            Token.TokenSettings tokenSettings = new Token.TokenSettings(originalContent).content(content).hover(hover).click(click).clickAction(clickAction).noCaching();
//            if (originalContent.equals(content))
//                tokenSettings.ignoreTokenizer(this);
//            tokenSettings.retainColour(true);

            return new TokenizerResult(Token.builder().content(content).build(), matcher.group(1).length());
        }

        return null;
    }

}
