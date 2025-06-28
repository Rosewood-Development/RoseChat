package dev.rosewood.rosechat.message.tokenizer.discord.tag;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.filter.Filter;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class FromDiscordTagTokenizer extends Tokenizer {

    public static final Pattern TAG_PATTERN = Pattern.compile("<@([0-9]{18,19})>");
    public static final Pattern ROLE_TAG_PATTERN = Pattern.compile("<@&([0-9]{18,19})>");

    public FromDiscordTagTokenizer() {
        super("from_discord_tag");
    }

    @Override
    public TokenizerResult tokenize(TokenizerParams params) {
        String input = params.getInput();
        if (!input.startsWith("<"))
            return null;

        Matcher matcher = TAG_PATTERN.matcher(input);
        Matcher roleMatcher = ROLE_TAG_PATTERN.matcher(input);

        String originalContent = null;
        String content = null;
        boolean isRole = false;

        if (matcher.find() && matcher.start() == 0) {
            originalContent = input.substring(0, matcher.end());
            content = matcher.group(1);
        }

        if (roleMatcher.find() && roleMatcher.start() == 0) {
            originalContent = input.substring(0, roleMatcher.end());
            content = roleMatcher.group(1);
            isRole = true;
        }

        if (originalContent == null)
            return null;

        DiscordChatProvider discord = RoseChatAPI.getInstance().getDiscord();
        String prefix = "@";

        if (isRole) {
            params.getOutputs().getTaggedPlayers().addAll(discord.getPlayersWithRole(content));

            // Format and play the tag sound appropriately if a role is tagged.
            for (Filter filter : RoseChatAPI.getInstance().getFilters()) {
                if (filter.prefix() == null)
                    continue;

                if (!filter.tagPlayers())
                    continue;

                if (!filter.prefix().equals(prefix))
                    continue;

                if (filter.sound() != null)
                    params.getOutputs().setSound(filter.sound());
            }
        }

        Token.Builder token;
        if (isRole) {
            token = Token.group(prefix + discord.getRoleFromId(content))
                    .ignoreTokenizer(Tokenizers.FILTER)
                    .ignoreTokenizer(Tokenizers.BUNGEE_PAPI_PLACEHOLDER)
                    .ignoreTokenizer(Tokenizers.PAPI_PLACEHOLDER)
                    .ignoreTokenizer(Tokenizers.ROSECHAT_PLACEHOLDER);
        } else {
            UUID uuid = discord.getUUIDFromId(content);
            if (uuid == null) {
                token = Token.group(prefix + discord.getUserFromId(content))
                        .ignoreTokenizer(Tokenizers.FILTER)
                        .ignoreTokenizer(Tokenizers.BUNGEE_PAPI_PLACEHOLDER)
                        .ignoreTokenizer(Tokenizers.PAPI_PLACEHOLDER)
                        .ignoreTokenizer(Tokenizers.ROSECHAT_PLACEHOLDER)
                        .ignoreTokenizer(Tokenizers.FORMAT)
                        .ignoreTokenizer(Tokenizers.RAINBOW)
                        .ignoreTokenizer(Tokenizers.GRADIENT);
            } else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                RosePlayer player = new RosePlayer(offlinePlayer);

                if (player.isOffline())
                    return new TokenizerResult(Token.text(prefix + discord.getUserFromId(content)), originalContent.length());

                token = Token.group(prefix + player.getName());
            }
        }

        token.encapsulate();
        return new TokenizerResult(token.build(), originalContent.length());
    }

}
