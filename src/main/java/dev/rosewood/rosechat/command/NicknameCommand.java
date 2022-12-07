package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.wrapper.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizers;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class NicknameCommand extends AbstractCommand {

    public NicknameCommand() {
        super(false, "nickname", "nick");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0 || (args.length == 1 && !(sender instanceof Player))) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        Player player = target == null ? (Player) sender : target;
        PlayerData playerData = this.getAPI().getPlayerData(player.getUniqueId());
        String nickname = (target == null ? getAllArgs(0, args) : getAllArgs(1, args));

        if (target != null && !sender.hasPermission("rosechat.nickname.others")) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "no-permission");
            return;
        }

        if (args.length == 1 && target != null) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        if (nickname.equalsIgnoreCase("off")) {
            playerData.setNickname(null);
            player.setDisplayName(null);
            playerData.save();
            if (target == null) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-success", StringPlaceholders.single("name", player.getName()));
            } else {
                this.getAPI().getLocaleManager().sendComponentMessage(player, "command-nickname-success", StringPlaceholders.single("name", player.getName()));
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-other",
                        StringPlaceholders.builder("name", player.getName()).addPlaceholder("player", player.getName()).build());
            }
            return;
        }

        if (this.isNicknameAllowed((Player) sender, nickname)) {
            setDisplayName(player, nickname + "&r");

            playerData.setNickname(nickname + "&r");
            playerData.save();

            if (target == null) {
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-success", StringPlaceholders.single("name", nickname));
            } else {
                this.getAPI().getLocaleManager().sendComponentMessage(player, "command-nickname-success", StringPlaceholders.single("name", nickname));
                this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-other",
                        StringPlaceholders.builder("name", nickname).addPlaceholder("player", player.getName()).build());
            }
        }
    }

    public static void setDisplayName(Player player, String nickname) {
        RoseSender roseSender = new RoseSender(player);

        MessageWrapper message = new MessageWrapper(roseSender, MessageLocation.NICKNAME, null, nickname).filterCaps().filterLanguage().filterURLs();
        if (!message.canBeSent()) {
            if (message.getFilterType() != null) message.getFilterType().sendWarning(roseSender);
            return;
        }

        // Remove emojis from the display name.
        String displayName = nickname;
        for (ChatReplacement replacement : RoseChatAPI.getInstance().getEmojis()) {
            if (!displayName.contains(replacement.getText()) || replacement.getFont() == null || replacement.getFont().equals("default")) continue;
            displayName = displayName.replace(replacement.getText(), "").trim();
        }

        String[] bundles = ConfigurationManager.Setting.USE_MARKDOWN_FORMATTING.getBoolean() ? new String[] { Tokenizers.DEFAULT_BUNDLE, Tokenizers.DISCORD_FORMATTING_BUNDLE, Tokenizers.MARKDOWN_BUNDLE } : new String[] { Tokenizers.DEFAULT_BUNDLE };
        BaseComponent[] nicknameComponent = new MessageTokenizer(message, roseSender, displayName, bundles).toComponents();

        player.setDisplayName(TextComponent.toLegacyText(nicknameComponent));
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

    private boolean isNicknameAllowed(Player player, String nickname) {
        if (!MessageUtils.canColor(player, nickname, "nickname")) return false;

        String formattedNickname = ChatColor.stripColor(HexUtils.colorify(PlaceholderAPIHook.applyPlaceholders(player, nickname)));

        if (formattedNickname.length() < ConfigurationManager.Setting.MINIMUM_NICKNAME_LENGTH.getInt()) {
            this.getAPI().getLocaleManager().sendComponentMessage(player, "command-nickname-too-short");
            return false;
        }

        if (formattedNickname.length() > ConfigurationManager.Setting.MAXIMUM_NICKNAME_LENGTH.getInt()) {
            this.getAPI().getLocaleManager().sendComponentMessage(player, "command-nickname-too-long");
            return false;
        }

        String colorified = HexUtils.colorify(nickname);
        if ((formattedNickname.contains(" ") && !ConfigurationManager.Setting.ALLOW_SPACES_IN_NICKNAMES.getBoolean())
                || (!MessageUtils.isAlphanumericSpace(formattedNickname) && !ConfigurationManager.Setting.ALLOW_NONALPHANUMERIC_CHARACTERS.getBoolean())
                || ChatColor.stripColor(colorified).isEmpty()) {
            this.getAPI().getLocaleManager().sendComponentMessage(player, "command-nickname-not-allowed");
            return false;
        }

        return true;
    }

}
