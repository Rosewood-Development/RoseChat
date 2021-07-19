package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageWrapper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface GroupReceiver {

    /**
     * Sends the message wrapper to the entire group.
     * @param messageWrapper The message wrapper to send.
     */
    void send(MessageWrapper messageWrapper);

    /**
     * Sends a json string to the group.
     * @param json The json string to send.
     */
    void sendJson(String json);

    /**
     * @return The members.
     */
    List<UUID> getMembers();

    default void sendToPlayer(MessageWrapper message, Player receiver) {
        PlayerData receiverData = RoseChatAPI.getInstance().getPlayerData(receiver.getUniqueId());
        if (message.getSender().isPlayer() && receiverData.getIgnoringPlayers().contains(message.getSender().getUUID())) return;
        BaseComponent[] messageComponents;
        if (receiver.hasPermission("rosechat.deletemessages.others")) {
            messageComponents = message.withDelete();
        } else if (receiver.hasPermission("rosechat.deletemessages.self")) {
            if (message.getSender().isPlayer() && message.getSender().getUUID().equals(receiver.getUniqueId())) {
                messageComponents = message.withDeleteSelf();
            } else {
                messageComponents = message.getComponents();
            }
        } else {
            messageComponents = message.getComponents();
        }

        receiver.spigot().sendMessage(messageComponents);
        receiverData.getMessageLog().addDeletableMessage(new DeletableMessage(message.getUUID(), ComponentSerializer.toString(messageComponents), false));
    }
}
