package dev.rosewood.rosechat.command.command;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.command.RoseChatCommand;
import dev.rosewood.rosechat.command.argument.RoseChatArgumentHandlers;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.manager.DataManager;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.parser.RoseChatParser;
import dev.rosewood.rosechat.message.tokenizer.MessageOutputs;
import dev.rosewood.rosechat.message.tokenizer.placeholder.RoseChatPlaceholderTokenizer;
import dev.rosewood.rosechat.message.wrapper.MessageRules;
import dev.rosewood.rosechat.message.wrapper.MessageRules.RuleOutputs;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentCondition;
import dev.rosewood.rosegarden.command.framework.ArgumentsDefinition;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.CommandInfo;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NicknameCommand extends RoseChatCommand {

    public NicknameCommand(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("nickname")
                .aliases("nick")
                .descriptionKey("command-nickname-description")
                .permission("rosechat.nickname")
                .arguments(ArgumentsDefinition.builder()
                        .optional("player", RoseChatArgumentHandlers.ROSE_PLAYER,
                                ArgumentCondition.hasPermission("rosechat.nickname.others"))
                        .required("nickname", RoseChatArgumentHandlers.NICKNAME)
                        .build())
                .build();
    }

    @Override
    protected boolean hasPriority() {
        return true;
    }

    @RoseExecutable
    public void execute(CommandContext context, String nickname) {
        RosePlayer player = new RosePlayer(context.getSender());
        if (player.isConsole()) {
            player.sendLocaleMessage("only-player");
            return;
        }

        if (nickname.equalsIgnoreCase("off") || nickname.equalsIgnoreCase("remove")) {
            this.removeNickname(player, player);
            return;
        }

        String strippedNickname = MessageUtils.stripShaderColors(nickname);

        RoseMessage nicknameMessage = RoseMessage.forLocation(player, PermissionArea.NICKNAME);
        nicknameMessage.setPlayerInput(strippedNickname);

        MessageRules rules = new MessageRules().applyAllFilters().ignoreMessageLogging();
        RuleOutputs outputs = rules.apply(nicknameMessage, nickname);

        if (outputs.isBlocked()) {
            if (outputs.getWarning() != null)
                outputs.getWarning().send(player);

            if (Settings.SEND_BLOCKED_MESSAGES_TO_STAFF.get()) {
                for (Player staffPlayer : Bukkit.getOnlinePlayers()) {
                    if (staffPlayer.hasPermission("rosechat.seeblocked")) {
                        RosePlayer rosePlayer = new RosePlayer(staffPlayer);
                        rosePlayer.sendLocaleMessage("blocked-message",
                                StringPlaceholders.of("player", nicknameMessage.getSender().getName(),
                                        "message", nickname));
                    }
                }
            }

            return;
        }

        if (!this.isNicknameAllowed(player, player, nicknameMessage))
            return;

        this.setNickname(player, player, strippedNickname);
    }

    @RoseExecutable
    public void execute(CommandContext context, RosePlayer target, String nickname) {
        RosePlayer player = new RosePlayer(context.getSender());

        if (nickname.equalsIgnoreCase("off") || nickname.equalsIgnoreCase("remove")) {
            this.removeNickname(player, target);
            return;
        }

        String strippedNickname = MessageUtils.stripShaderColors(nickname);
        this.setNickname(player, target, strippedNickname);
    }

    private void removeNickname(RosePlayer player, RosePlayer target) {
        boolean success = target.removeNickname();
        if (!success)
            return;

        if (player.isConsole() || !player.getUUID().equals(target.getUUID()))
            player.sendLocaleMessage("command-nickname-player",
                    StringPlaceholders.of(
                            "player", target.getRealName(),
                            "name", target.getName()
                    ));
        target.sendLocaleMessage("command-nickname-success",
                StringPlaceholders.of("name", target.getName()));
    }

    private void setNickname(RosePlayer player, RosePlayer target, String nickname) {
        // Parse the nickname to find what it would be like as a display name.
        RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
            if (!Settings.ALLOW_DUPLICATE_NAMES.get()) {
                RoseMessage message = RoseMessage.forLocation(player, PermissionArea.NICKNAME);
                MessageTokenizerResults<BaseComponent[]> components = message.parse(target, nickname);

                String displayName = TextComponent.toLegacyText(components.content());
                if (RoseChat.getInstance().getManager(DataManager.class).containsNickname(target.getUUID(), ChatColor.stripColor(HexUtils.colorify(displayName).toLowerCase()))) {
                    player.sendLocaleMessage("command-nickname-taken");
                    return;
                }
            }

            Bukkit.getScheduler().runTask(RoseChat.getInstance(), () -> {
                boolean success = target.setNickname(nickname);
                if (!success)
                    return;

                if (player.isConsole() || !player.getUUID().equals(target.getUUID()))
                    player.sendLocaleMessage("command-nickname-player",
                            StringPlaceholders.of(
                                    "player", target.getRealName(),
                                    "name", target.getName()
                            ));
                target.sendLocaleMessage("command-nickname-success",
                        StringPlaceholders.of("name", target.getName()));
            });
        });
    }

    private boolean isNicknameAllowed(RosePlayer player, RosePlayer target, RoseMessage message) {
        String nickname = message.getPlayerInput();
        String strippedNickname = ChatColor.stripColor(HexUtils.colorify(nickname));

        if (strippedNickname.length() < Math.max(1, Settings.MINIMUM_NICKNAME_LENGTH.get())) {
            player.sendLocaleMessage("command-nickname-too-short");
            return false;
        }

        if (strippedNickname.length() > Settings.MAXIMUM_NICKNAME_LENGTH.get()) {
            player.sendLocaleMessage("command-nickname-too-long");
            return false;
        }

        if (!Settings.ALLOW_SPACES_IN_NICKNAMES.get() && strippedNickname.contains(" ")) {
            player.sendLocaleMessage("command-nickname-not-allowed");
            return false;
        }

        if (!Settings.ALLOW_NONALPHANUMERIC_CHARACTERS.get() && !MessageUtils.isAlphanumericSpace(strippedNickname)) {
            player.sendLocaleMessage("command-nickname-not-allowed");
            return false;
        }

        // Parse the nickname to make sure the player isn't missing any permissions.
        MessageTokenizerResults<BaseComponent[]> results = new RoseChatParser().parse(message, target, RoseChatPlaceholderTokenizer.MESSAGE_PLACEHOLDER);
        MessageOutputs outputs = results.outputs();
        if (!outputs.getMissingPermissions().isEmpty()) {
            player.sendLocaleMessage("no-permission");
            return false;
        }

        return true;
    }

}
