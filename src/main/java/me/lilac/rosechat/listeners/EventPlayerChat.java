package me.lilac.rosechat.listeners;

import me.lilac.rosechat.Rosechat;
import me.lilac.rosechat.chat.ChatFilter;
import me.lilac.rosechat.placeholder.FormatType;
import me.lilac.rosechat.placeholder.PlaceholderMessage;
import me.lilac.rosechat.storage.Messages;
import me.lilac.rosechat.storage.PlayerData;
import me.lilac.rosechat.utils.Methods;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;

public class EventPlayerChat implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        event.setCancelled(true);
        Player player = event.getPlayer();

        if (PlayerData.getMutedPlayers().contains(player.getUniqueId())) {
            player.sendMessage(Methods.format(Messages.getMuted()));
            return;
        }

        String message = new ChatFilter(player, event.getMessage()).getFilteredMessage();
        if (message == null) return;

        if (!Rosechat.getInstance().getChatManager().getMessages().containsKey(player.getUniqueId()))
            Rosechat.getInstance().getChatManager().getMessages().put(player.getUniqueId(), new ArrayList<>());
        Rosechat.getInstance().getChatManager().getMessages().get(player.getUniqueId()).add(event.getMessage());

        if (PlayerData.getPlayersUsingStaffchat().contains(player.getUniqueId())) {

            TextComponent finalMessage = new PlaceholderMessage(player, message, FormatType.STAFF_CHAT).getMessage();
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.hasPermission("rosechat.staffchat")) return;
                online.spigot().sendMessage(finalMessage);
            }
            Bukkit.getConsoleSender().sendMessage(finalMessage.toLegacyText());
            return;
        }

        TextComponent finalMessage = new PlaceholderMessage(player, message, FormatType.CHAT).getMessage();
        Bukkit.spigot().broadcast(finalMessage);
        Bukkit.getConsoleSender().sendMessage(finalMessage.toLegacyText());
    }
}
