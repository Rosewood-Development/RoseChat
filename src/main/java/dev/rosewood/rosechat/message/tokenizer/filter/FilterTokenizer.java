package dev.rosewood.rosechat.message.tokenizer.filter;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.filter.Filter;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import dev.rosewood.rosechat.message.tokenizer.composer.ChatComposer;
import dev.rosewood.rosechat.message.tokenizer.decorator.FontDecorator;
import dev.rosewood.rosechat.message.tokenizer.decorator.HoverDecorator;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class FilterTokenizer extends Tokenizer {

    public FilterTokenizer() {
        super("filter");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (params.getLocation() != PermissionArea.CHANNEL && !params.getSender().hasPermission("rosechat.filters." + params.getLocationPermission()))
            return null;

        for (Filter filter : RoseChatAPI.getInstance().getFilters()) {
            if (filter.block())
                continue;

            if (filter.inlinePrefix() == null || filter.inlineSuffix() == null || filter.prefix() == null || filter.suffix() == null)
                continue;

            String regex = "(?:" + Pattern.quote(filter.prefix()) + "(.*?)" + Pattern.quote(filter.suffix()) + ")"
                    + Pattern.quote(filter.inlinePrefix()) + "(.*?)" + Pattern.quote(filter.inlineSuffix());
            Matcher matcher = Pattern.compile(regex).matcher(input);
            if (!matcher.find() || matcher.start() != 0)
                continue;

            return this.handleInlineMatch(params, filter, matcher);
        }

        for (Filter filter : RoseChatAPI.getInstance().getFilters()) {
            if (filter.block() || filter.prefix() == null)
                continue;

            if (filter.useRegex()) {
                List<Pattern> patterns = RoseChatAPI.getInstance().getFilterManager()
                        .getCompiledPatterns().get(filter.id() + "-prefix");
                if (patterns == null || patterns.isEmpty())
                    continue;

                Pattern pattern = patterns.getFirst();
                Matcher matcher = pattern.matcher(input);
                if (!matcher.find() || matcher.start() != 0)
                    continue;

                return this.handlePrefixMatch(params, filter, matcher.group());
            }

            String foundMatch = this.matches(filter, filter.prefix(), input);
            if (foundMatch == null)
                continue;

            if (filter.suffix() != null) {
                if (!input.contains(filter.suffix()))
                    continue;

                // Filter has a suffix, content is within.
                return this.handlePrefixSuffixMatch(params, filter);
            } else {

                // Filter does not have a suffix, content is after the prefix.
                return this.handlePrefixMatch(params, filter, foundMatch);
            }
        }

        // Handle normal matches.
        for (Filter filter : RoseChatAPI.getInstance().getFilters()) {
            if (filter.block() || filter.prefix() != null || filter.inlinePrefix() != null)
                continue;

            if (filter.useRegex()) {
                List<Pattern> patterns = RoseChatAPI.getInstance().getFilterManager()
                        .getCompiledPatterns().get(filter.id() + "-matches");
                if (patterns == null || patterns.isEmpty())
                    continue;

                for (Pattern pattern : patterns) {
                    Matcher matcher = pattern.matcher(input);
                    if (!matcher.find() || matcher.start() != 0)
                        continue;

                    // Found a regex match.
                    return this.handleRegexMatch(params, filter, matcher);
                }

                continue;
            }

            for (String match : filter.matches()) {
                String foundMatch = this.matches(filter, match, input);
                if (foundMatch == null || foundMatch.isEmpty())
                    continue;

                // Found a normal match.
                return this.handleMatch(params, filter, foundMatch);
            }
        }

        return null;
    }

    private String matches(Filter filter, String match, String input) {
        if (filter.sensitivity() == 0)
            return input.startsWith(match) ? match : null;

        String strippedMessage = ChatColor.stripColor(HexUtils.colorify(MessageUtils.stripAccents(input.toLowerCase())));
        String[] splitMessage = input.split(" ");
        String[] splitMessageStripped = strippedMessage.split(" ");
        for (int i = 0; i < splitMessageStripped.length; i++) {
            String word = splitMessage[i];
            String wordStripped = splitMessageStripped[i];

            double difference = MessageUtils.getLevenshteinDistancePercent(wordStripped, match);
            if ((1 - difference) <= filter.sensitivity() / 100.0) {
                return input.substring(input.indexOf(word), input.indexOf(word) + word.length());
            }
        }

        return null;
    }

    private TokenizerResult handleMatch(TokenizerParams params, Filter filter, String match) {
        if (!params.getInput().startsWith(match))
            return null;

        if (params.getIgnoredFilters().contains(filter.id()))
            return null;

        if (!filter.hasPermission(params.getSender()))
            return this.validateRemoval(match);

        if (this.checkToggled(params, filter))
            return null;

        String content = this.getReplacementForDirection(params, filter);
        content = this.applySignFix(params, filter, content);

        Token.Builder token = this.createFilterToken(params, filter, content);
        return new TokenizerResult(token.build(), match.length());
    }

    private TokenizerResult handleRegexMatch(TokenizerParams params, Filter filter, Matcher matcher) {
        String match = matcher.group();
        if (!params.getInput().startsWith(match))
            return null;

        if (params.getIgnoredFilters().contains(filter.id()))
            return null;

        if (!filter.hasPermission(params.getSender()))
            return this.validateRemoval(match);

        if (this.checkToggled(params, filter))
            return null;

        String content = this.getReplacementForDirection(params, filter);
        content = this.applySignFix(params, filter, content);

        Token.Builder token = this.createFilterToken(params, filter, content);

        // Apply placeholders for regex matches.
        StringPlaceholders.Builder placeholders = StringPlaceholders.builder();
        int groups = Math.max(9, matcher.groupCount() + 1);
        for (int i = 0; i < groups; i++) {
            String replacement = matcher.groupCount() < i || matcher.group(i) == null ?
                    matcher.group(0) : matcher.group(i);
            content = content.replace("%group_" + i + "%", replacement);
            placeholders.add("group_" + i, replacement);
        }

        token.placeholder("message", match)
                .placeholder("extra", match)
                .placeholder("tagged", "%group_1%")
                .placeholders(placeholders.build());

        token.ignoreTokenizer(this);

        return new TokenizerResult(token.build(), match.length());
    }

    private TokenizerResult handlePrefixMatch(TokenizerParams params, Filter filter, String prefix) {
        String input = params.getInput();
        int endIndex = input.indexOf(" ");
        String match;
        String content;

        if (endIndex == -1)
            endIndex = input.length();

        match = input.substring(0, endIndex);
        content = input.substring(prefix.length(), endIndex);

        if (!input.startsWith(match))
            return null;

        if (params.getIgnoredFilters().contains(filter.id()))
            return null;

        if (!filter.hasPermission(params.getSender()))
            return this.validateRemoval(match);

        if (this.checkToggled(params, filter))
            return null;

        String replacement = this.getReplacementForDirection(params, filter);
        replacement = this.applySignFix(params, filter, replacement);

        if (filter.matchLength()) {
            String colorless = RoseChatAPI.getInstance().parse(params.getSender(), params.getReceiver(), content).build(ChatComposer.plain());
            replacement = this.matchContentLength(replacement, colorless.length());
        }

        if (filter.tagPlayers())
            this.tagPlayers(params, filter, content);

        Token.Builder token = this.createFilterToken(params, filter, replacement);
        token.placeholder("message", match)
                .placeholder("tagged", match)
                .placeholder("group_0", match)
                .placeholder("group_1", content);

        return new TokenizerResult(token.build(), match.length());
    }

    private TokenizerResult handlePrefixSuffixMatch(TokenizerParams params, Filter filter) {
        String prefix = filter.prefix();
        String suffix = filter.suffix();

        String input = params.getInput();
        int endIndex = input.lastIndexOf(suffix) + suffix.length();
        String match = input.substring(0, endIndex);
        String content = input.substring(prefix.length(), input.lastIndexOf(suffix));

        if (!input.startsWith(match))
            return null;

        if (params.getIgnoredFilters().contains(filter.id()))
            return null;

        if (!filter.hasPermission(params.getSender()))
            return this.validateRemoval(match);

        if (this.checkToggled(params, filter))
            return null;

        String replacement = this.getReplacementForDirection(params, filter);
        replacement = this.applySignFix(params, filter, replacement);

        if (filter.matchLength()) {
            String colorless = RoseChatAPI.getInstance().parse(params.getSender(), params.getReceiver(), content).build(ChatComposer.plain());
            replacement = this.matchContentLength(replacement, colorless.length());
        }

        if (filter.tagPlayers())
            this.tagPlayers(params, filter, content);

        Token.Builder token = this.createFilterToken(params, filter, replacement);
        token.placeholder("message", match)
                .placeholder("group_0", match)
                .placeholder("group_1", content);

        token.encapsulate(true);

        return new TokenizerResult(token.build(), match.length());
    }

    public TokenizerResult handleInlineMatch(TokenizerParams params, Filter filter, Matcher matcher) {
        String originalContent = matcher.group();
        String content = matcher.group(1);
        String inline = matcher.group(2);

        if (!params.getInput().startsWith(originalContent))
            return null;

        if (filter.useRegex()) {
            boolean matchesContent = false;
            boolean matchesInline = false;

            List<Pattern> contentPatterns = RoseChatAPI.getInstance().getFilterManager()
                    .getCompiledPatterns().get(filter.id() + "-matches");
            if (contentPatterns != null && !contentPatterns.isEmpty()) {
                for (Pattern pattern : contentPatterns) {
                    Matcher contentMatcher = pattern.matcher(content);
                    if (!contentMatcher.find())
                        continue;

                    matchesContent = true;
                    break;
                }
            } else
                matchesContent = true;

            List<Pattern> inlinePatterns = RoseChatAPI.getInstance().getFilterManager()
                    .getCompiledPatterns().get(filter.id() + "-inline-matches");
            if (inlinePatterns != null && !inlinePatterns.isEmpty()) {
                for (Pattern pattern : inlinePatterns) {
                    Matcher inlineMatcher = pattern.matcher(inline);
                    if (!inlineMatcher.find())
                        continue;

                    matchesInline = true;
                    break;
                }
            } else
                matchesInline = true;

            if (!matchesContent || !matchesInline)
                return null;
        }

        if (params.getIgnoredFilters().contains(filter.id()))
            return null;

        if (!filter.hasPermission(params.getSender()))
            return this.validateRemoval(originalContent);

        if (this.checkToggled(params, filter))
            return null;

        String replacement = this.getReplacementForDirection(params, filter);
        replacement = this.applySignFix(params, filter, replacement);

        if (filter.matchLength()) {
            String colorless = RoseChatAPI.getInstance().parse(params.getSender(), params.getReceiver(), content).build(ChatComposer.plain());
            replacement = this.matchContentLength(replacement, colorless.length());
        }

        if (filter.tagPlayers())
            this.tagPlayers(params, filter, content);

        Token.Builder token = this.createFilterToken(params, filter, replacement);
        token.placeholder("group_0", originalContent)
                .placeholder("message", originalContent)
                .placeholder("group_1", content)
                .placeholder("group_2", inline);

        token.encapsulate(true);

        return new TokenizerResult(token.build(), originalContent.length());
    }

    private TokenizerResult validateRemoval(String match) {
        return Settings.REMOVE_FILTERS.get() ?
                new TokenizerResult(Token.text(" "), match.length()) :
                null;
    }
    
    private boolean checkToggled(TokenizerParams params, Filter filter) {
        if (!filter.canToggle())
            return false;
        
        PlayerData data = RoseChatAPI.getInstance().getPlayerData(params.getSender().getUUID());
        return data != null && !data.hasEmojis();
    }

    private String getReplacementForDirection(TokenizerParams params, Filter filter) {
        return (params.getDirection() == MessageDirection.MINECRAFT_TO_DISCORD) && (filter.discordOutput() != null) ?
                filter.discordOutput() :
                filter.replacement();
    }

    private String matchContentLength(String content, int length) {
        return content.repeat(length);
    }

    private String applySignFix(TokenizerParams params, Filter filter, String content) {
        return params.getLocation() == PermissionArea.SIGN && filter.isEmoji() ?
                "&f" + content + "&r" : content;
    }

    private void tagPlayers(TokenizerParams params, Filter filter, String content) {
        DetectedPlayer detectedPlayer = this.matchPartialPlayer(content, filter.id());
        if (detectedPlayer != null) {
            Player taggedPlayer = detectedPlayer.player;
            params.getOutputs().getTaggedPlayers().add(taggedPlayer.getUniqueId());
            params.getOutputs().setPlaceholderTarget(new RosePlayer(taggedPlayer));
        } else {
            params.getOutputs().setPlaceholderTarget(new RosePlayer(content, "default"));
        }
    }

    private Token.Builder createFilterToken(TokenizerParams params, Filter filter, String content) {
        Token.Builder token = Token.group(content)
                .decorate(new FontDecorator(filter.font()))
                .ignoreTokenizer(Tokenizers.SHADER_COLORS)
                .ignoreFilter(filter);

        if (filter.hover() != null)
            token.decorate(new HoverDecorator(filter.hover()));

        if (!filter.colorRetention())
            token.encapsulate();

        if (filter.sound() != null)
            params.getOutputs().setSound(filter.sound());

        if (filter.message() != null)
            params.getOutputs().setMessage(filter.message());

        params.getOutputs().getServerCommands().addAll(filter.serverCommands());
        params.getOutputs().getPlayerCommands().addAll(filter.playerCommands());

        return token;
    }

    private DetectedPlayer matchPartialPlayer(String input, String id) {
        // Check display names first
        for (Player player : Bukkit.getOnlinePlayers()) {
            int matchLength = this.getMatchLength(input, ChatColor.stripColor(player.getDisplayName()), id);
            if (matchLength != -1)
                return new DetectedPlayer(player, matchLength);
        }

        // Then usernames
        for (Player player : Bukkit.getOnlinePlayers()) {
            int matchLength = this.getMatchLength(input, player.getName(), id);
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
    private int getMatchLength(String input, String playerName, String id) {
        Pattern stopPattern;
        List<Pattern> stopPatterns = RoseChatAPI.getInstance().getFilterManager().getCompiledPatterns().get(id + "-stop");
        if (stopPatterns == null || stopPatterns.isEmpty()) {
            stopPattern = Pattern.compile(MessageUtils.PUNCTUATION_REGEX);
        } else {
            stopPattern = stopPatterns.getFirst();
        }

        int matchLength = 0;
        for (int i = 0, j = 0; i < input.length() && j < playerName.length(); i++, j++) {
            int inputChar = Character.toUpperCase(input.codePointAt(i));
            int playerChar = Character.toUpperCase(playerName.codePointAt(j));
            if (inputChar == playerChar) {
                matchLength++;
            } else if (i > 0 && (Character.isSpaceChar(inputChar) ||  Pattern.matches(stopPattern.pattern(), String.valueOf(Character.toChars(inputChar))))) {
                return matchLength;
            } else {
                return -1;
            }
        }

        return matchLength;
    }

    private record DetectedPlayer(Player player, int consumedTextLength) { }

}
