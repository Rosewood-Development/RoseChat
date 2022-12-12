package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.listener.PacketListener;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosechat.message.PrivateMessageInfo;
import dev.rosewood.rosechat.placeholders.RoseChatPlaceholder;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeleteMessageCommand extends AbstractCommand {

    public DeleteMessageCommand() {
        super(true, "delmsg");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            this.getAPI().getLocaleManager().sendMessage(sender, "invalid-arguments", StringPlaceholders.single("syntax", getSyntax()));
            return;
        }

        try {
            Player player = (Player) sender;
            UUID uuid = UUID.fromString(args[0]);
            List<DeletableMessage> messages = new ArrayList<>();

            PlayerData senderData = this.getAPI().getPlayerData(player.getUniqueId());
            for (DeletableMessage deletableMessage : senderData.getMessageLog().getDeletableMessages()) {
                if (!deletableMessage.getUUID().equals(uuid)) continue;
                messages.add(deletableMessage);
            }

            for (DeletableMessage message : messages) {
                // Handle private messages differently.
                if (message.getPrivateMessageInfo() != null) {
                    PrivateMessageInfo info = message.getPrivateMessageInfo();
                    if (info.getSender() == info.getReceiver()) {
                        this.deleteMessageForPlayer(player, message);
                    }

                    else if (info.getSender().getUUID().equals(player.getUniqueId())) {
                        this.deleteMessageForPlayer(player, message);
                        if (info.getReceiver().isPlayer()) {
                            Player receiver = info.getReceiver().asPlayer();
                            for (DeletableMessage deletableMessage : this.getAPI().getPlayerData(receiver.getUniqueId()).getMessageLog().getDeletableMessages()) {
                                if (deletableMessage.getUUID().equals(uuid)) this.deleteMessageForPlayer(receiver, deletableMessage);
                            }
                        }
                    }

                    else if (info.getReceiver().getUUID().equals(player.getUniqueId())) {
                        if (info.getReceiver().isPlayer()) {
                            Player receiver = info.getReceiver().asPlayer();
                            for (DeletableMessage deletableMessage : this.getAPI().getPlayerData(receiver.getUniqueId()).getMessageLog().getDeletableMessages()) {
                                if (deletableMessage.getUUID().equals(uuid)) this.deleteMessageForPlayer(receiver, deletableMessage);
                            }
                        }
                    }

                    else if (info.getSpies().contains(player.getUniqueId())) {
                        if (info.getSender().isPlayer()) {
                            Player pmSender = info.getSender().asPlayer();
                            for (DeletableMessage deletableMessage : this.getAPI().getPlayerData(pmSender.getUniqueId()).getMessageLog().getDeletableMessages()) {
                                if (deletableMessage.getUUID().equals(uuid)) this.deleteMessageForPlayer(pmSender, deletableMessage);
                            }
                        }
                        if (info.getReceiver().isPlayer()) {
                            Player receiver = info.getReceiver().asPlayer();
                            for (DeletableMessage deletableMessage : this.getAPI().getPlayerData(receiver.getUniqueId()).getMessageLog().getDeletableMessages()) {
                                if (deletableMessage.getUUID().equals(uuid)) this.deleteMessageForPlayer(receiver, deletableMessage);
                            }
                        }
                        this.deleteMessageForPlayer(player, message);
                    }

                    return;
                }

                if (message.isClient()) {
                    this.deleteMessageForPlayer(player, message);
                } else {
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        for (DeletableMessage deletableMessage : this.getAPI().getPlayerData(onlinePlayer.getUniqueId()).getMessageLog().getDeletableMessages()) {
                            if (deletableMessage.getUUID().equals(uuid)) this.deleteMessageForPlayer(onlinePlayer, deletableMessage);
                        }
                    }
                }
            }
        } catch (IllegalArgumentException ignored) {}
    }

    private void deleteMessageForPlayer(Player player, DeletableMessage message) {
        // Get the deleted message placeholder.
        RoseSender roseSender = new RoseSender(player);
        BaseComponent[] deletedMessage = this.parseDeletedMessagePlaceholder(roseSender, roseSender,
                MessageUtils.getSenderViewerPlaceholders(roseSender, roseSender)
                        .addPlaceholder("id", message.getUUID())
                        .addPlaceholder("type", message.isClient() ? "client" : "server").build(), message);

        // Append the delete button to the 'Deleted Message' message.
        boolean updated = false;
        if (deletedMessage != null && !TextComponent.toPlainText(deletedMessage).isEmpty()) {
            String json = ComponentSerializer.toString(deletedMessage);
            if (player.hasPermission("rosechat.deletemessages.client")) {
                BaseComponent[] withDeleteButton = PacketListener.appendButton(roseSender, roseSender.getPlayerData(), message.getUUID().toString(), json);
                if (withDeleteButton != null) {
                    message.setJson(ComponentSerializer.toString(withDeleteButton));
                } else {
                    // If the delete button doesn't exist, use the 'Deleted Message' message.
                    message.setJson(json);
                }
            } else {
                // If the player doesn't have permission, use the 'Deleted Message' message.
                message.setJson(json);
            }

            updated = true;
        }

        // Remove the original message.
        if (!updated) roseSender.getPlayerData().getMessageLog().getDeletableMessages().remove(message);
        // Send blank lines to remove the old messages.
        for (int i = 0; i < 100; i++) player.sendMessage("\n");
        // Resend the messages!
        for (DeletableMessage deletableMessage : roseSender.getPlayerData().getMessageLog().getDeletableMessages())
            player.spigot().sendMessage(ComponentSerializer.parse(deletableMessage.getJson()));

        // Delete this message from Discord too.
        if (message.isClient()) return;

        if (updated) message.setClient(true);
        if (!Setting.DELETE_DISCORD_MESSAGES.getBoolean()) return;
        if (this.getAPI().getDiscord() != null && message.getDiscordId() != null)
            this.getAPI().getDiscord().deleteMessage(message.getDiscordId());
    }

    private BaseComponent[] parseDeletedMessagePlaceholder(RoseSender sender, RoseSender viewer, StringPlaceholders placeholders, DeletableMessage deletableMessage) {
        String placeholderId = Setting.DELETED_MESSAGE_FORMAT.getString();
        RoseChatPlaceholder placeholder = this.getAPI().getPlaceholderManager().getPlaceholder(placeholderId.substring(1, placeholderId.length() - 1));
        if (placeholder == null) return null;

        BaseComponent[] components;
        HoverEvent hoverEvent = null;
        ClickEvent clickEvent = null;

        if (placeholder.getText() == null) return null;
        String text = placeholder.getText().parseToString(sender, viewer, placeholders);
        if (text == null) return null;
        components = this.getAPI().parse(sender, viewer, text, placeholders);

        if (placeholder.getHover() != null) {
            String hover = placeholder.getHover().parseToString(sender, viewer, placeholders);
            if (hover != null) {
                if (hover.equalsIgnoreCase("%original%")) {
                    hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentSerializer.parse(deletableMessage.getJson()));
                } else {
                    hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, this.getAPI().parse(sender, viewer, hover, placeholders));
                }
            }
        }

        if (placeholder.getClick() != null) {
            String click = placeholder.getClick().parseToString(sender, viewer, placeholders);
            ClickEvent.Action action = placeholder.getClick().parseToAction(sender, viewer, placeholders);
            if (click != null && action != null) {
                clickEvent = new ClickEvent(action, TextComponent.toPlainText(this.getAPI().parse(sender, viewer, click, placeholders)));
            }
        }

        for (BaseComponent component : components) {
            component.setHoverEvent(hoverEvent);
            component.setClickEvent(clickEvent);
        }

        return components;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getSyntax() {
        return null;
    }

}
