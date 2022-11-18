package dev.rosewood.rosechat.command;

import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.command.api.AbstractCommand;
import dev.rosewood.rosechat.listener.PacketListener;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RoseSender;
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
            UUID uuid = UUID.fromString(args[0]);
            DeletableMessage message = null;

            List<DeletableMessage> toDelete = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerData playerData = this.getAPI().getPlayerData(player.getUniqueId());

                for (DeletableMessage deletableMessage : playerData.getMessageLog().getDeletableMessages()) {
                    if (!deletableMessage.getUUID().equals(uuid)) continue;

                    RoseSender roseSender = new RoseSender(player);

                    BaseComponent[] deletedMessage = this.parseDeletedMessagePlaceholder(roseSender, roseSender,
                            MessageUtils.getSenderViewerPlaceholders(roseSender, roseSender)
                            .addPlaceholder("id", deletableMessage.getUUID())
                            .addPlaceholder("type", deletableMessage.isClient() ? "client" : "server").build(), deletableMessage);

                    if (deletedMessage == null) {
                        toDelete.add(deletableMessage);
                    } else {
                        if (TextComponent.toPlainText(deletedMessage).isEmpty()) {
                            toDelete.add(deletableMessage);
                        } else {
                            String json = ComponentSerializer.toString(deletedMessage);

                            if (player.hasPermission("rosechat.deletemessages.client")) {
                                BaseComponent[] withDeleteButton = PacketListener.appendButton(roseSender, playerData, deletableMessage.getUUID().toString(), json);
                                if (withDeleteButton != null) {
                                    deletableMessage.setJson(ComponentSerializer.toString(withDeleteButton));
                                    deletableMessage.setClient(true);
                                } else {
                                    deletableMessage.setJson(json);
                                }
                            } else {
                                deletableMessage.setJson(json);
                            }
                        }
                    }

                    message = deletableMessage;
                }

                if (message == null || !message.getUUID().equals(uuid)) continue;
                for (DeletableMessage deletableMessage : toDelete) {
                    if (!deletableMessage.isClient() || (playerData.getUUID() == ((Player) sender).getUniqueId())) {
                        playerData.getMessageLog().getDeletableMessages().remove(deletableMessage);
                    }
                }

                for (int i = 0; i < 100; i++) player.sendMessage("\n");
                for (DeletableMessage deletableMessage : playerData.getMessageLog().getDeletableMessages())
                    player.spigot().sendMessage(ComponentSerializer.parse(deletableMessage.getJson()));
            }

            // Delete this message from Discord too, if enabled.
            if (!Setting.DELETE_DISCORD_MESSAGES.getBoolean()) return;
            if (message == null) return;
            if (this.getAPI().getDiscord() != null && message.getDiscordId() != null) {
                this.getAPI().getDiscord().deleteMessage(message.getDiscordId());
            }
        } catch (IllegalArgumentException ignored) {}
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
