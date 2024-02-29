package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.event.player.PlayerNicknameEvent;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.parser.RoseChatParser;
import dev.rosewood.rosechat.message.tokenizer.MessageOutputs;
import dev.rosewood.rosechat.message.tokenizer.placeholder.RoseChatPlaceholderTokenizer;
import dev.rosewood.rosechat.message.tokenizer.shader.ShaderTokenizer;
import dev.rosewood.rosechat.message.wrapper.MessageRules;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
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
import java.util.regex.Matcher;

public class NicknameCommand extends AbstractCommand {

    private final RoseChat plugin;

    public NicknameCommand(RoseChat plugin) {
        super(false, "nickname", "nick");
        this.plugin = plugin;
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

            nickname = null;
            PlayerNicknameEvent playerNicknameEvent = new PlayerNicknameEvent(player, nickname);
            Bukkit.getPluginManager().callEvent(playerNicknameEvent);
            if (playerNicknameEvent.isCancelled()) return;

            nickname = playerNicknameEvent.getNickname();

            PlayerData data = this.getAPI().getPlayerData(player.getUniqueId());
            data.setNickname(null);
            player.setDisplayName(null);
            data.save();

            if (this.plugin.getNicknameProvider() != null) {
                this.plugin.getNicknameProvider().setNickname(player, null);
            }

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

        PlayerNicknameEvent playerNicknameEvent = new PlayerNicknameEvent(target, nickname);
        Bukkit.getPluginManager().callEvent(playerNicknameEvent);
        if (playerNicknameEvent.isCancelled()) return;

        nickname = playerNicknameEvent.getNickname();

        // Ignore shader colours in the nickname.
        if (nickname.contains("#")) {
            Matcher matcher = MessageUtils.HEX_REGEX.matcher(nickname);
            if (matcher.find()) {
                String match = nickname.substring(matcher.start(), matcher.end());
                if (Setting.CORE_SHADER_COLORS.getStringList().contains(match)) {
                    String freeHex = ShaderTokenizer.findFreeHex(match.substring(1));
                    nickname = nickname.replace(match, "#" + freeHex);
                }
            }
        }

        RosePlayer roseSender = new RosePlayer(sender);
        RosePlayer roseTarget = new RosePlayer(target);

        RoseMessage message = RoseMessage.forLocation(roseSender, MessageLocation.NICKNAME);
        message.setPlayerInput(nickname);

        MessageRules rules = new MessageRules().applyAllFilters().ignoreMessageLogging();
        MessageRules.RuleOutputs outputs = rules.apply(message, nickname);

        // Block the message if it breaks the rules.
        if (outputs.isBlocked()) {
            if (outputs.getWarning() != null)
                outputs.getWarning().send(roseSender);
            return;
        }

        RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
            if (roseSender.isConsole() || this.isNicknameAllowed(roseSender, roseTarget, message)) {
                MessageTokenizerResults<BaseComponent[]> components = message.parse(roseTarget, outputs.getFilteredMessage());

                PlayerData data = this.getAPI().getPlayerData(roseTarget.getUUID());

                roseTarget.asPlayer().setDisplayName(TextComponent.toLegacyText(components.content()));
                data.setNickname(outputs.getFilteredMessage());
                data.save();

                if (this.plugin.getNicknameProvider() != null) {
                    Player player = roseTarget.asPlayer();
                    this.plugin.getNicknameProvider().setNickname(player, player.getDisplayName());
                }

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
                tab.add("<player>");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (MessageUtils.isPlayerVanished(player))
                        continue;

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
        String nickname = message.getPlayerInput();
        String strippedNickname = ChatColor.stripColor(HexUtils.colorify(PlaceholderAPIHook.applyPlaceholders(target.asPlayer(), nickname)));

        if (strippedNickname.length() < Math.max(1, Setting.MINIMUM_NICKNAME_LENGTH.getInt())) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-too-short");
            return false;
        }

        if (strippedNickname.length() > Setting.MAXIMUM_NICKNAME_LENGTH.getInt()) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-too-long");
            return false;
        }

        if (!Setting.ALLOW_SPACES_IN_NICKNAMES.getBoolean() && strippedNickname.contains(" ")) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
            return false;
        }
        
        if (!Setting.ALLOW_NONALPHANUMERIC_CHARACTERS.getBoolean() && !MessageUtils.isAlphanumericSpace(strippedNickname)) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "command-nickname-not-allowed");
            return false;
        }

        // Parse the nickname, make sure the player isn't missing any permissions
        MessageTokenizerResults<BaseComponent[]> results = new RoseChatParser().parse(message, target, RoseChatPlaceholderTokenizer.MESSAGE_PLACEHOLDER);
        MessageOutputs outputs = results.outputs();
        if (!outputs.getMissingPermissions().isEmpty()) {
            this.getAPI().getLocaleManager().sendComponentMessage(sender, "no-permission");
            return false;
        }

        return true;
    }

}
