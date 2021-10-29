package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
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
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        Player player = target == null ? (Player) sender : target;
        PlayerData playerData = this.getAPI().getPlayerData(player.getUniqueId());
        String nickname = target == null ? getAllArgs(0, args) : getAllArgs(1, args);

        if (target != null && !sender.hasPermission("rosechat.nickname.others")) {
            this.getAPI().getLocaleManager().sendMessage(sender, "no-permission");
            return;
        }

        if (nickname.equalsIgnoreCase("off")) {
            playerData.setNickname(null);
            player.setDisplayName(null);
            if (target == null) {
                this.getAPI().getLocaleManager().sendMessage(sender, "command-nickname-success", StringPlaceholders.single("name", player.getName()));
            } else {
                this.getAPI().getLocaleManager().sendMessage(player, "command-nickname-success", StringPlaceholders.single("name", player.getName()));
                this.getAPI().getLocaleManager().sendMessage(sender, "command-nickname-other",
                        StringPlaceholders.builder("name", player.getName()).addPlaceholder("player", player.getName()).build());
            }
            return;
        }

        if (this.isNicknameAllowed(player, nickname)) {
            RoseSender roseSender = new RoseSender(player);
            MessageWrapper message = new MessageWrapper(roseSender, MessageLocation.GROUP, null, nickname).validate().filterLanguage().filterURLs();;
            if (!message.canBeSent()) {
                if (message.getFilterType() != null) message.getFilterType().sendWarning(roseSender);
                return;
            }

            BaseComponent[] components = message.parse(null, roseSender);
            String formattedNickname = TextComponent.toLegacyText(components);
            playerData.setNickname(nickname);
            player.setDisplayName(formattedNickname);
            playerData.save();

            if (target == null) {
                this.getAPI().getLocaleManager().sendMessage(sender, "command-nickname-success", StringPlaceholders.single("name", formattedNickname));
            } else {
                this.getAPI().getLocaleManager().sendMessage(player, "command-nickname-success", StringPlaceholders.single("name", formattedNickname));
                this.getAPI().getLocaleManager().sendMessage(sender, "command-nickname-other",
                        StringPlaceholders.builder("name", formattedNickname).addPlaceholder("player", player.getName()).build());
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            tab.add("<nickname>");
            if (sender.hasPermission("rosechat.nickname.others")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (sender instanceof Player && player.getUniqueId().equals(((Player) sender).getUniqueId())) continue;
                    tab.add(player.getName());
                }
            }
        } else if (args.length == 2 && sender.hasPermission("rosechat.nickname.others")) {
            tab.add("<nickname>");
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
        String formattedNickname = ChatColor.stripColor(HexUtils.colorify(PlaceholderAPIHook.applyPlaceholders(player, nickname)));

        if (formattedNickname.length() < ConfigurationManager.Setting.MIN_NICKNAME_LENGTH.getInt()) {
            this.getAPI().getLocaleManager().sendMessage(player, "command-nickname-too-short");
            return false;
        }

        if (formattedNickname.length() > ConfigurationManager.Setting.MAX_NICKNAME_LENGTH.getInt()) {
            this.getAPI().getLocaleManager().sendMessage(player, "command-nickname-too-long");
            return false;
        }

        String colorified = HexUtils.colorify(nickname);
        if ((formattedNickname.contains(" ") && !ConfigurationManager.Setting.ALLOW_SPACES_IN_NICKNAMES.getBoolean())
                || (!StringUtils.isAlphanumericSpace(formattedNickname) && !ConfigurationManager.Setting.ALLOW_NONALPHANUMERIC_CHARACTERS_IN_NICKNAMES.getBoolean())
                || ChatColor.stripColor(colorified).isEmpty()) {
            this.getAPI().getLocaleManager().sendMessage(player, "command-nickname-not-allowed");
            return false;
        }

        return true;
    }
}
