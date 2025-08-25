package dev.rosewood.rosechat.message.tokenizer.placeholder;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.message.MessageUtils;
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
    public static final Pattern PATTERN = Pattern.compile("\\{(.*?)}");

    public RoseChatPlaceholderTokenizer() {
        super("rosechat");
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        String input = params.getInput();

        List<TokenizerResult> results = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(input);
        while (matcher.find()) {
            String placeholder = matcher.group();
            String placeholderValue = matcher.group(1);

            // Hardcoded special {message} placeholder to inject the player's message
            // Do not let the {message} placeholder be used if the message contains player input
            if (placeholder.equals(MESSAGE_PLACEHOLDER) && !params.containsPlayerInput()) {
                String playerInput = params.getPlayerMessage();
                if (playerInput == null || playerInput.isEmpty()) {
                    RoseChat.getInstance().getLogger().warning("Parsed " + placeholder + " with no player message. This is likely a configuration error. Printing a stacktrace for help.");
                    new RuntimeException().printStackTrace();
                    results.add(new TokenizerResult(Token.text(""), matcher.start(), placeholder.length()));
                    continue;
                }

                if (params.getSender().getPlayerData() == null) {
                    results.add(new TokenizerResult(Token.group(params.getPlayerMessage()).containsPlayerInput().build(), matcher.start(), placeholder.length()));
                    continue;
                }

                String color = params.shouldUsePlayerChatColor() ? params.getSender().getPlayerData().getColor() : "";
                results.add(new TokenizerResult(Token.group(color + params.getPlayerMessage()).containsPlayerInput().build(), matcher.start(), placeholder.length()));
                continue;
            }

            if (!this.hasExtendedTokenPermission(params, "rosechat.placeholders", "rosechat.placeholder.rosechat." + placeholderValue))
                continue;

            RosePlayer receiver = params.getOutputs().getPlaceholderTarget() == null ?
                    params.getReceiver() : params.getOutputs().getPlaceholderTarget();
            params.getOutputs().setPlaceholderTarget(null);

            CustomPlaceholder roseChatPlaceholder = RoseChatAPI.getInstance().getPlaceholderManager().getPlaceholder(placeholderValue);
            if (roseChatPlaceholder == null)
                return null;

            if (matcher.start() != 0 && input.charAt(matcher.start() - 1) == MessageUtils.ESCAPE_CHAR && params.getSender().hasPermission("rosechat.escape")) {
                results.add(new TokenizerResult(Token.text(placeholder), matcher.start() - 1, placeholder.length() + 1));
                continue;
            }

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
                tokenBuilder.decorate(new HoverDecorator(formattedHover));

            if (click != null)
                tokenBuilder.decorate(new ClickDecorator(clickAction, click));

            if (params.containsPlayerInput())
                tokenBuilder.encapsulate();

            if (content.contains(placeholder)) {
                // If we contain ourselves, avoid infinite recursion by disallowing tokenizing this again.
                tokenBuilder.ignoreTokenizer(this);
            }

            results.add(new TokenizerResult(tokenBuilder.build(), matcher.start(), placeholder.length()));
        }

        return results;
    }

}
