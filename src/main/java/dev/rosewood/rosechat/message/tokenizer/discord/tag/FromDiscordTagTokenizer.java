package dev.rosewood.rosechat.message.tokenizer.discord.tag;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.replacement.Replacement;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.message.tokenizer.Token;
import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.TokenizerResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            for (Replacement replacement : RoseChatAPI.getInstance().getReplacements()) {
                if (replacement.getInput().getPrefix() == null)
                    continue;

                if (!replacement.getInput().getPrefix().equals(prefix))
                    continue;

                if (!replacement.getOutput().shouldTagOnlinePlayers())
                    break;

                if (replacement.getOutput().getSound() != null)
                    params.getOutputs().setTagSound(replacement.getOutput().getSound());
            }
        }

        // TODO: Allow spaces in names - Requires tokenizer recursion fix
        String finalTag = prefix + (isRole ? discord.getRoleFromId(content) : discord.getUserFromId(content));
        return new TokenizerResult(Token.group(finalTag).build(), originalContent.length());
    }

}
