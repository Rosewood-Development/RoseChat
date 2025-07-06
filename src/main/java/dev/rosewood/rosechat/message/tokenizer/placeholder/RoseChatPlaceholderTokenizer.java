package dev.rosewood.rosechat.message.tokenizer.placeholder;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.decorator.ClickDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import dev.rosewood.rosechat.placeholder.CustomPlaceholder;
import dev.rosewood.rosechat.placeholder.DefaultPlaceholders;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoseChatPlaceholderTokenizer extends Tokenizer {

    public static final String MESSAGE_PLACEHOLDER = "{message}";
    public static final Pattern PATTERN = Pattern.compile("^\\{(.*?)}");

    public RoseChatPlaceholderTokenizer() {
        super("rosechat");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("{"))
            return null;

        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.find())
            return null;

        String placeholder = matcher.group();
        String placeholderValue = matcher.group(1);
        if (!this.hasExtendedTokenPermission(params, "rosechat.placeholders", "rosechat.placeholder.rosechat." + placeholderValue))
            return null;

        RosePlayer receiver = params.getOutputs().getPlaceholderTarget() == null ?
                params.getReceiver() : params.getOutputs().getPlaceholderTarget();
        params.getOutputs().setPlaceholderTarget(null);

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

            String color = params.shouldUsePlayerChatColor() ? params.getSender().getPlayerData().getColor() : "";
            return new TokenizerResult(Token.group(color + params.getPlayerInput()).containsPlayerInput().build(), matcher.group().length());
        }

        CustomPlaceholder roseChatPlaceholder = RoseChatAPI.getInstance().getPlaceholderManager().getPlaceholder(placeholderValue);
        if (roseChatPlaceholder == null)
            return null;

        StringPlaceholders placeholders = DefaultPlaceholders.getFor(params.getSender(), receiver, params.getChannel(), params.getPlaceholders()).build();
        String content = placeholders.apply(roseChatPlaceholder.get("text").parseToString(params.getSender(), receiver, placeholders));

        List<String> formattedHover = new ArrayList<>();
        if (roseChatPlaceholder.get("hover") != null) {
            List<String> hover = roseChatPlaceholder.get("hover").parseToStringList(params.getSender(), receiver, placeholders);
            for (String s : hover) {
                formattedHover.add(placeholders.apply(s));
            }
        }

        String click = roseChatPlaceholder.get("click") == null ?
                null : placeholders.apply(roseChatPlaceholder.get("click").parseToString(params.getSender(), receiver, placeholders));
        ClickDecorator.Action clickAction = roseChatPlaceholder.get("click") == null ?
                null : roseChatPlaceholder.get("click").getClickAction();

        Token.Builder tokenBuilder = Token.group(content);
        if (!formattedHover.isEmpty())
            tokenBuilder.decorate(params.decorators().hover(formattedHover));

        if (click != null)
            tokenBuilder.decorate(params.decorators().click(clickAction, click));

        if (params.containsPlayerInput())
            tokenBuilder.encapsulate();

        if (content.contains(placeholder)) {
            // If we contain ourselves, avoid infinite recursion by disallowing tokenizing this again.
            tokenBuilder.ignoreTokenizer(this);
        }

        return new TokenizerResult(tokenBuilder.build(), placeholder.length());
    }

}
