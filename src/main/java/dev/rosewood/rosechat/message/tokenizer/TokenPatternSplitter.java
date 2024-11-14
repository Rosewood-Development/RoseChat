package dev.rosewood.rosechat.message.tokenizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Util for splitting a format string by a pattern with a replacement
 */
public final class TokenPatternSplitter {

    private final Map<String, TokenPattern> patterns;
    private Consumer<Token.Builder> matchTokenConsumer;
    private Consumer<Token.Builder> otherTokenConsumer;
    private Pattern compiledPattern;

    public TokenPatternSplitter() {
        this.patterns = new HashMap<>();
        this.matchTokenConsumer = null;
        this.otherTokenConsumer = null;
        this.compiledPattern = null;
    }

    /**
     * Adds a consumer that is applied to all matches of a pattern
     *
     * @param matchTokenConsumer The consumer with the token builder
     * @return this
     */
    public TokenPatternSplitter matchConsumer(Consumer<Token.Builder> matchTokenConsumer) {
        this.matchTokenConsumer = matchTokenConsumer;
        return this;
    }

    /**
     * Adds a consumer that is applied to all text that isn't a match of a pattern
     *
     * @param otherTokenConsumer The consumer with the token builder
     * @return this
     */
    public TokenPatternSplitter otherConsumer(Consumer<Token.Builder> otherTokenConsumer) {
        this.otherTokenConsumer = otherTokenConsumer;
        return this;
    }

    /**
     * Defines a pattern with a replacement
     *
     * @param pattern The pattern
     * @param replacement The replacement
     * @return this
     */
    public TokenPatternSplitter pattern(String pattern, String replacement) {
        return this.pattern(pattern, replacement, null);
    }

    /**
     * Defines a pattern with a replacement and a consumer to run when the pattern is matched
     *
     * @param pattern The pattern
     * @param replacement The replacement
     * @param matchTokenConsumer The consumer with the token builder
     * @return this
     */
    public TokenPatternSplitter pattern(String pattern, String replacement, Consumer<Token.Builder> matchTokenConsumer) {
        this.patterns.put(pattern, new TokenPattern(pattern, replacement, matchTokenConsumer));
        this.compiledPattern = null;
        return this;
    }

    /**
     * Applies this TokenPatternSplitter to the given format string and returns a Token of it split
     *
     * @param formatString The format string
     * @return a group Token split on all patterns with replacements applied and consumers run
     */
    public Token apply(String formatString) {
        Pattern pattern = this.compile();
        Matcher matcher = pattern.matcher(formatString);
        List<Token> chunks = new ArrayList<>();
        int contentIndex = 0;
        while (matcher.find()) {
            String match = matcher.group();
            TokenPattern replacement = this.patterns.get(match);
            if (replacement == null)
                throw new IllegalStateException("TokenPattern matched without a replacement");

            if (contentIndex != matcher.start()) {
                Token.Builder builder = Token.group(formatString.substring(contentIndex, matcher.start()));
                if (this.otherTokenConsumer != null)
                    this.otherTokenConsumer.accept(builder);
                chunks.add(builder.build());
            }

            Token.Builder builder = Token.group(replacement.replacement());
            if (this.matchTokenConsumer != null)
                this.matchTokenConsumer.accept(builder);
            if (replacement.matchTokenConsumer() != null)
                replacement.matchTokenConsumer().accept(builder);
            chunks.add(builder.build());

            contentIndex = matcher.end();
        }

        if (contentIndex < formatString.length()) {
            Token.Builder builder = Token.group(formatString.substring(contentIndex));
            if (this.otherTokenConsumer != null)
                this.otherTokenConsumer.accept(builder);
            chunks.add(builder.build());
        }

        return Token.group(chunks).build();
    }

    private Pattern compile() {
        if (this.compiledPattern == null) {
            StringBuilder patternBuilder = new StringBuilder();
            for (String pattern : this.patterns.keySet()) {
                if (!patternBuilder.isEmpty())
                    patternBuilder.append('|');
                patternBuilder.append(Pattern.quote(pattern));
            }
            this.compiledPattern = Pattern.compile(patternBuilder.toString());
        }
        return this.compiledPattern;
    }

    private record TokenPattern(String pattern, String replacement, Consumer<Token.Builder> matchTokenConsumer) { }

}
