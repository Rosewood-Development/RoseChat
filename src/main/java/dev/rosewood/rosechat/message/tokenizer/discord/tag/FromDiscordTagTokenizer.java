//package dev.rosewood.rosechat.message.tokenizer.discord.tag;
//
//import dev.rosewood.rosechat.api.RoseChatAPI;
//import dev.rosewood.rosechat.chat.Tag;
//import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
//import dev.rosewood.rosechat.message.MessageUtils;
//import dev.rosewood.rosechat.message.wrapper.RoseMessage;
//import dev.rosewood.rosechat.message.RosePlayer;
//import dev.rosewood.rosechat.message.tokenizer.Token;
//import dev.rosewood.rosechat.message.tokenizer.Tokenizer;
//import org.bukkit.Bukkit;
//import org.bukkit.entity.Player;
//import java.util.regex.Matcher;
//
//public class FromDiscordTagTokenizer implements Tokenizer<Token> {
//
//    @Override
//    public Token tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions) {
//        if (!input.startsWith("<")) return null;
//        Matcher matcher = MessageUtils.DISCORD_TAG_PATTERN.matcher(input);
//        Matcher roleMatcher = MessageUtils.DISCORD_ROLE_TAG_PATTERN.matcher(input);
//
//        String originalContent = null;
//        String content = null;
//        boolean isRole = false;
//
//        if (matcher.find() && matcher.start() == 0) {
//            originalContent = input.substring(0, matcher.end());
//            content = matcher.group(1);
//        }
//
//        if (roleMatcher.find() && roleMatcher.start() == 0) {
//            originalContent = input.substring(0, roleMatcher.end());
//            content = roleMatcher.group(1);
//            isRole = true;
//        }
//
//        if (originalContent == null) return null;
//
//        DiscordChatProvider discord = RoseChatAPI.getInstance().getDiscord();
//        Player player = Bukkit.getPlayer(discord.getUserFromId(content));
//        String taggedName = discord.getUserFromId(content);
//        String prefix = "@";
//
//        for (Tag tag : RoseChatAPI.getInstance().getTags()) {
//            if (tag.getPrefix().equals(prefix)) {
//                if (!tag.shouldTagOnlinePlayers() || !tag.getPrefix().equals(prefix)) continue;
//                roseMessage.setTagSound(tag.getSound());
//                if (player == null) break;
//                return new Token(new Token.TokenSettings(originalContent).content(tag.getPrefix() + taggedName + (tag.getSuffix() != null ? tag.getSuffix() : "")));
//            }
//        }
//
//        if (isRole) {
//            roseMessage.getTaggedPlayers().addAll(discord.getPlayersWithRole(content));
//        }
//
//        String finalTag = prefix + (isRole ? discord.getRoleFromId(content).replaceFirst(" ", "_") : discord.getUserFromId(content));
//
//        return new Token(new Token.TokenSettings(originalContent).content(finalTag));
//    }
//
//}
