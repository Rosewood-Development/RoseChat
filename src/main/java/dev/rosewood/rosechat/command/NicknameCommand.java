package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.tokenizer.shader.ShaderTokenizer;
import dev.rosewood.rosechat.message.wrapper.MessageRules;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.wrapper.RoseMessageComponents;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class NicknameCommand extends AbstractCommand {

    public NicknameCommand() {
        super(false, "nickname", "nick");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0 || (args.length == 1 && !(sender instanceof Player))) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.of("syntax", getSyntax()));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        String nickname;

        // If the target exists and isn't the player, make sure they have permission.
        if (target != null && target != sender && !sender.hasPermission("rosechat.nickname.others")) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "no-permission");
            return;
        }

        // If the player only entered a player name, show the syntax.
        if (args.length == 1 && target != null && target != sender) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.of("syntax", getSyntax()));
            return;
        }

        if ((args.length == 1 && (args[0].equalsIgnoreCase("off")) || (args.length == 2 && (args[1].equals("off"))) || target == sender)) {
            Player player = target == null ? (Player) sender : target;
            PlayerData data = this.getAPI().getPlayerData(player.getUniqueId());
            data.setNickname(null);
            player.setDisplayName(null);
            data.save();

            if (player == sender) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-success", StringPlaceholders.of("name", player.getName()));
            } else {
                this.getAPI().getLocaleManager().sendComponentMessage(player, "command-nickname-success", StringPlaceholders.of("name", player.getName()));
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-other",
                        StringPlaceholders.builder("name", player.getName()).add("player", player.getName()).build());
            }

            return;
        }

        nickname = getAllArgs(1, args);
        if (target == null) {
            // Try to get partial name first.
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                if (sender instanceof Player) {
                    target = (Player) sender;
                    nickname = getAllArgs(0, args);
                }
                else {
                    this.getAPI().getLocaleManager().sendComponentMessage(sender, "player-not-found", StringPlaceholders.of("syntax", getSyntax()));
                    return;
                }
            } else {
                nickname = getAllArgs(1, args);
            }
        }

        // Ignore shader colours in the nickname.
        if (nickname.contains("#")) {
            Matcher matcher = MessageUtils.HEX_REGEX.matcher(nickname);
            if (matcher.find()) {
                String match = nickname.substring(matcher.start(), matcher.end());
                if (ConfigurationManager.Setting.CORE_SHADER_COLORS.getStringList().contains(match)) {
                    String freeHex = ShaderTokenizer.findFreeHex(match.substring(1));
                    nickname = nickname.replace(match, "#" + freeHex);
                }
            }
        }

        RosePlayer roseSender = new RosePlayer(sender);
        RosePlayer roseTarget = new RosePlayer(target);

        RoseMessage message = RoseMessage.forLocation(roseSender, MessageLocation.NICKNAME);

        MessageRules rules = new MessageRules().applyAllFilters().ignoreMessageLogging();
        MessageRules.RuleOutputs outputs = rules.apply(message, nickname);

        // Block the message if it breaks the rules.
        if (outputs.isBlocked()) {
            if (outputs.getWarning() != null)
                outputs.getWarning().send(roseSender);
            return;
        }

        RoseChat.MESSAGE_THREAD_POOL.submit(() -> {
            if (roseSender.isConsole() || this.isNicknameAllowed(roseSender, roseTarget, message)) {
                RoseMessageComponents components = message.parse(roseTarget, outputs.getFilteredMessage());

                PlayerData data = this.getAPI().getPlayerData(roseTarget.getUUID());

                roseTarget.asPlayer().setDisplayName(TextComponent.toLegacyText(components.components()));
                data.setNickname(outputs.getFilteredMessage());
                data.save();

                if (roseTarget.getUUID().equals(roseSender.getUUID())) {
                    this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-success", StringPlaceholders.of("name", data.getNickname()));
                } else {
                    this.getAPI().getLocaleManager().sendComponentMessage(roseTarget, "command-nickname-success", StringPlaceholders.of("name", data.getNickname()));
                    this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-other",
                            StringPlaceholders.builder("name", data.getNickname()).add("player", roseTarget.getName()).build());
                }
            }
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            tab.add("<nickname>");
            tab.add("off");
            if (sender.hasPermission("rosechat.nickname.others")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player != sender) tab.add(player.getName());
                }
            }
        } else if (args.length == 2 && sender.hasPermission("rosechat.nickname.others")) {
            tab.add("<nickname>");
            tab.add("off");
        }

        return tab;
    }

    @Override
    public String getPermission() {
        return "rosechat.nickname";
    }

    @Override
    public String getSyntax() {
        return this.getAPI().getLocaleManager().getLocaleMessage("command-nickname-usage");
    }

    private boolean isNicknameAllowed(RosePlayer sender, RosePlayer target, RoseMessage message) {
        // TODO
//        String nickname = message.getMessage();
//
//        if (!MessageUtils.canColor(sender, nickname, "nickname")) return false;
//
//        String formattedNickname = ChatColor.stripColor(HexUtils.colorify(PlaceholderAPIHook.applyPlaceholders(target.asPlayer(), nickname)));
//
//        if (formattedNickname.length() < ConfigurationManager.Setting.MINIMUM_NICKNAME_LENGTH.getInt()) {
//            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-too-short");
//            return false;
//        }
//
//        if (formattedNickname.length() > ConfigurationManager.Setting.MAXIMUM_NICKNAME_LENGTH.getInt()) {
//            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-too-long");
//            return false;
//        }
//
//        String colorified = HexUtils.colorify(nickname);
//        if ((formattedNickname.contains(" ") && !ConfigurationManager.Setting.ALLOW_SPACES_IN_NICKNAMES.getBoolean())
//                || (!MessageUtils.isAlphanumericSpace(formattedNickname) && !ConfigurationManager.Setting.ALLOW_NONALPHANUMERIC_CHARACTERS.getBoolean())
//                || ChatColor.stripColor(colorified).isEmpty()) {
//            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
//            return false;
//        }
//
//        // Check every permission.
//        if (nickname.contains("[")) {
//            Matcher matcher = MessageUtils.URL_MARKDOWN_PATTERN.matcher(nickname);
//            if (matcher.find() && !MessageUtils.hasTokenPermission(message, "rosechat.url")) {
//                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
//                return false;
//            }
//        }
//
//        for (ChatReplacement emoji : this.getAPI().getEmojis()) {
//            if (nickname.contains(emoji.getText())
//                    && !MessageUtils.hasExtendedTokenPermission(message, "rosechat.emojis", "rosechat.emoji." + emoji.getId())) {
//                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
//                return false;
//            }
//        }
//
//        for (ChatReplacement replacement : this.getAPI().getReplacements()) {
//            if (!replacement.isRegex()) continue;
//            Matcher matcher = Pattern.compile(replacement.getText()).matcher(nickname);
//            if (matcher.find()
//                    && !MessageUtils.hasExtendedTokenPermission(message, "rosechat.replacements", "rosechat.replacement." + replacement.getId())) {
//                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
//                return false;
//            }
//        }
//
//        for (ChatReplacement replacement : this.getAPI().getReplacements()) {
//            if (replacement.isRegex()) continue;
//            if (nickname.contains(replacement.getText())
//                    && !MessageUtils.hasExtendedTokenPermission(message, "rosechat.replacements", "rosechat.replacement." + replacement.getId())) {
//                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
//                return false;
//            }
//        }
//
//        if (nickname.contains("%")) {
//            Matcher matcher = PlaceholderAPI.getPlaceholderPattern().matcher(nickname);
//            if (matcher.find()) {
//                String placeholder = matcher.group().replaceFirst("_", "");
//                if (!MessageUtils.hasExtendedTokenPermission(message, "rosechat.placeholders", "rosechat.placeholder." + placeholder)) {
//                    this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
//                    return false;
//                }
//            }
//        }
//
//        if (nickname.contains("{")) {
//            Matcher matcher = RoseChatPlaceholderTokenizer.RC_PATTERN.matcher(nickname);
//            if (matcher.find()) {
//                String placeholder = matcher.group();
//                if (!MessageUtils.hasExtendedTokenPermission(message, "rosechat.placeholders", "rosechat.placeholder.rosechat." + placeholder)) {
//                    this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
//                    return false;
//                }
//            }
//        }
//
//        if (nickname.contains("__")) {
//            Matcher matcher = MessageUtils.UNDERLINE_MARKDOWN_PATTERN.matcher(nickname);
//            if (matcher.find() && !MessageUtils.hasTokenPermission(message, "rosechat.underline")) {
//                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
//                return false;
//            }
//        }
//
//        if (nickname.contains("~~")) {
//            Matcher matcher = MessageUtils.STRIKETHROUGH_MARKDOWN_PATTERN.matcher(nickname);
//            if (matcher.find() && !MessageUtils.hasTokenPermission(message, "rosechat.strikethrough")) {
//                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
//                return false;
//            }
//        }
//
//        if (nickname.contains("**")) {
//            Matcher matcher = MessageUtils.BOLD_MARKDOWN_PATTERN.matcher(nickname);
//            if (matcher.find() && !MessageUtils.hasTokenPermission(message, "rosechat.bold")) {
//                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
//                return false;
//            }
//        }
//
//        if (nickname.contains("*")) {
//            Matcher matcher = MessageUtils.ITALIC_MARKDOWN_PATTERN.matcher(nickname);
//            if (matcher.find() && !MessageUtils.hasTokenPermission(message, "rosechat.italic")) {
//                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
//                return false;
//            }
//        }
//
//        if (nickname.startsWith("> ") && !MessageUtils.hasTokenPermission(message, "rosechat.quote")) {
//            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
//            return false;
//        }
//
//        DiscordChatProvider discord = RoseChatAPI.getInstance().getDiscord();
//        for (int i = 0; i < nickname.length(); i++) {
//            String substring = nickname.substring(i);
//            for (Tag tag : this.getAPI().getTags()) {
//                if (substring.startsWith(tag.getPrefix())
//                        && !MessageUtils.hasExtendedTokenPermission(message, "rosechat.tags", "rosechat.tag." + tag.getId())) {
//                    this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
//                    return false;
//                }
//            }
//
//            if (substring.startsWith("```")) {
//                if (substring.substring(3).contains("```") && !MessageUtils.hasTokenPermission(message, "rosechat.multicode")) {
//                    this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
//                    return false;
//                }
//            }
//
//            if (substring.startsWith("`")) {
//                if (substring.substring(1).contains("`") && !MessageUtils.hasTokenPermission(message, "rosechat.code")) {
//                    this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
//                    return false;
//                }
//            }
//
//            String spoiler = ConfigurationManager.Setting.MARKDOWN_FORMAT_SPOILER.getString();
//            String prefix = spoiler.substring(0, spoiler.indexOf("%message%"));
//            String suffix = spoiler.substring(spoiler.indexOf("%message%") + "%message%".length());
//            if (suffix.startsWith(prefix)) {
//                if (suffix.substring(prefix.length()).contains(suffix) && !MessageUtils.hasTokenPermission(message, "rosechat.spoiler")) {
//                    this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
//                    return false;
//                }
//            }
//
//            if (discord != null) {
//                if (substring.startsWith("@") && ConfigurationManager.Setting.CAN_TAG_MEMBERS.getBoolean()) {
//                    DiscordChatProvider.DetectedMention member = discord.matchPartialMember(substring.substring(1));
//                    if (member != null && !MessageUtils.hasTokenPermission(message, "rosechat.tag")) {
//                        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
//                        return false;
//                    }
//                }
//
//                if (substring.startsWith("#")) {
//                    DiscordChatProvider.DetectedMention channel = discord.matchPartialChannel(substring.substring(1));
//                    if (channel != null && !MessageUtils.hasTokenPermission(message, "rosechat.discordchannel")) {
//                        this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
//                        return false;
//                    }
//                }
//            }
//        }

        return true;
    }

}
