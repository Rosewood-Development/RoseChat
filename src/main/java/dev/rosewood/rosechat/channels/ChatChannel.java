package dev.rosewood.rosechat.channels;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ChatChannel {

    private List<Player> players;
    private ChannelType channelType;

    public ChatChannel(ChannelType type) {
        this.channelType = type;
        this.players = new ArrayList<>();
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public void sendMessage(Player player, String message) {
        for (Player member : players) player.sendMessage(message);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public ChannelType getChannelType() {
        return channelType;
    }
}
