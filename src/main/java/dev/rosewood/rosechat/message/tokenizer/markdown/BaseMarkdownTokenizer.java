package dev.rosewood.rosechat.message.tokenizer.markdown;

import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosegarden.config.RoseSetting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseMarkdownTokenizer extends Tokenizer {

    private final Pattern pattern;
    private final String permission;
    private final RoseSetting<String> formatSetting;
    private final List<Tokenizer> ignoreTokenizers;

    public BaseMarkdownTokenizer(String name, Pattern pattern, String permission, RoseSetting<String> formatSetting, Tokenizer... ignoreTokenizers) {
        super(name);
        this.pattern = pattern;
        this.permission = permission;
        this.formatSetting = formatSetting;
        this.ignoreTokenizers = Arrays.asList(ignoreTokenizers);
    }

    @Override
    public List<TokenizerResult> tokenize(TokenizerParams params) {
        String input = params.getInput();
        Matcher matcher = this.pattern.matcher(input);

        List<TokenizerResult> results = new ArrayList<>();

        while (matcher.find()) {
            int start = matcher.start();
            String match = matcher.group();
            String content = this.getContent(matcher);

            if (!this.hasTokenPermission(params, this.permission))
                return null;

            if (this.isPlayerName(content))
                continue;

            if (start > 0 && input.charAt(start - 1) == MessageUtils.ESCAPE_CHAR && params.getSender().hasPermission("rosechat.escape")) {
                results.add(new TokenizerResult(Token.text(match), start - 1, match.length() + 1));
                continue;
            }

            String format = this.formatSetting.get();
            if (!format.contains("%input_1%")) {
                results.add(new TokenizerResult(Token.group(
                        this.createToken(format, false).build(),
                        this.createToken(content, true).build()
                ).build(), start, match.length()));
                continue;
            }

            results.add(new TokenizerResult(this.createToken(format, false)
                    .placeholder("input_1", content)
                    .build(), start, match.length()));
        }

        return results;
    }

    protected String getContent(Matcher matcher) {
        return matcher.group(1);
    }

    protected boolean isPlayerName(String content) {
        return false;
    }

    private Token.Builder createToken(String content, boolean containsPlayerInput) {
        Token.Builder builder = Token.group(content).ignoreTokenizer(this);
        if (containsPlayerInput)
            builder.containsPlayerInput();
        for (Tokenizer ignoreTokenizer : this.ignoreTokenizers)
            builder.ignoreTokenizer(ignoreTokenizer);
        return builder;
    }

}
