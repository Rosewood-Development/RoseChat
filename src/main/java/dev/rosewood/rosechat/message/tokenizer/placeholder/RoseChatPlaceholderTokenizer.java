package dev.rosewood.rosechat.message.tokenizer.placeholder;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.decorator.ClickDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

public class RoseChatPlaceholderTokenizer extends Tokenizer {

    public static final String MESSAGE_PLACEHOLDER = "{message}";
    public static final Pattern PATTERN = Pattern.compile("^\\{(.*?)}");

    public RoseChatPlaceholderTokenizer() {
        super("rosechat");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("{")) return null;

        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.find()) return null;

        String placeholder = matcher.group();
        String placeholderValue = matcher.group(1);
        if (!MessageUtils.hasExtendedTokenPermission(params, "rosechat.placeholders", "rosechat.placeholder.rosechat." + placeholderValue)) return null;

        // Hardcoded special {message} placeholder to inject the player's message
        // Do not let the {message} placeholder be used if the message contains player input
        if (placeholder.equals(MESSAGE_PLACEHOLDER) && !params.containsPlayerInput()) {
            String playerInput = params.getPlayerInput();
            if (playerInput == null || playerInput.isEmpty()) {
                RoseChat.getInstance().getLogger().warning("Parsed " + MESSAGE_PLACEHOLDER + " with no player input. This is likely a configuration error.");
                return new TokenizerResult(Token.text(""), matcher.group().length());
            }

            if (params.getSender().getPlayerData() == null)
                return new TokenizerResult(Token.group(params.getPlayerInput()).containsPlayerInput().build(), matcher.group().length());

            String color = params.getSender().getPlayerData().getColor();
            return new TokenizerResult(Token.group(color + params.getPlayerInput()).containsPlayerInput().build(), matcher.group().length());
        }

        CustomPlaceholder roseChatPlaceholder = RoseChatAPI.getInstance().getPlaceholderManager().getPlaceholder(placeholderValue);
        if (roseChatPlaceholder == null) return null;

        StringPlaceholders placeholders = MessageUtils.getSenderViewerPlaceholders(params.getSender(), params.getReceiver(), params.getChannel(), params.getPlaceholders()).build();
        String content = placeholders.apply(roseChatPlaceholder.get("text").parseToString(params.getSender(), params.getReceiver(), placeholders));
        String hover = roseChatPlaceholder.get("hover") == null ? null : placeholders.apply(roseChatPlaceholder.get("hover").parseToString(params.getSender(), params.getReceiver(), placeholders));
        String click = roseChatPlaceholder.get("click") == null ? null : placeholders.apply(roseChatPlaceholder.get("click").parseToString(params.getSender(), params.getReceiver(), placeholders));
        ClickEvent.Action clickAction = roseChatPlaceholder.get("click") == null ? null : roseChatPlaceholder.get("click").getClickAction();
        HoverEvent.Action hoverAction = roseChatPlaceholder.get("hover") == null ? null : roseChatPlaceholder.get("hover").getHoverAction();

        Token.Builder tokenBuilder = Token.group(content);
        if (hover != null) tokenBuilder.decorate(HoverDecorator.of(hoverAction, hover));
        if (click != null) tokenBuilder.decorate(ClickDecorator.of(clickAction, click));
        if (params.containsPlayerInput()) tokenBuilder.encapsulate();

        if (content.contains(placeholder)) {
            // If we contain ourselves, avoid infinite recursion by disallowing tokenizing this again.
            tokenBuilder.ignoreTokenizer(this);
        }

        return new TokenizerResult(tokenBuilder.build(), placeholder.length());
    }

}
