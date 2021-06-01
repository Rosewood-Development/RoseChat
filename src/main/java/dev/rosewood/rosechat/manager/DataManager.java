package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.AbstractDataManager;
import org.bukkit.Bukkit;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class DataManager extends AbstractDataManager {

    private ChannelManager channelManager;
    private Map<UUID, PlayerData> playerData;

    public DataManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.playerData = new HashMap<>();
        this.channelManager = rosePlugin.getManager(ChannelManager.class);
    }

    public PlayerData getPlayerData(UUID uuid) {
        PlayerData data = this.playerData.get(uuid);
        if (data == null)
            throw new IllegalStateException("PlayerData for [" + uuid + "] not yet loaded.");
        return data;
    }

    public void unloadPlayerData(UUID uuid) {
        this.playerData.remove(uuid);
    }

    public void getPlayerData(UUID uuid, Consumer<PlayerData> callback) {
        if (this.playerData.containsKey(uuid)) {
            callback.accept(this.playerData.get(uuid));
            return;
        }

        this.async(() -> {
            this.databaseConnector.connect(connection -> {
                String dataQuery = "SELECT * FROM " + this.getTablePrefix() + "player_data WHERE uuid = ?";
                try (PreparedStatement statement = connection.prepareStatement(dataQuery)) {
                    statement.setString(1, uuid.toString());
                    ResultSet result = statement.executeQuery();

                    if (result.next()) {
                        boolean socialSpy = result.getBoolean("social_spy");
                        boolean canBeMessaged = result.getBoolean("can_be_messaged");
                        boolean hasTagSounds = result.getBoolean("has_tag_sounds");
                        boolean hasMessageSounds = result.getBoolean("has_message_sounds");
                        String currentChannel = result.getString("current_channel");
                        String color = result.getString("chat_color");
                        ChatChannel channel = this.channelManager.getChannel(currentChannel);

                        PlayerData playerData = new PlayerData(uuid);
                        playerData.setSocialSpy(socialSpy);
                        playerData.setCanBeMessaged(canBeMessaged);
                        playerData.setTagSounds(hasTagSounds);
                        playerData.setMessageSounds(hasMessageSounds);
                        playerData.setCurrentChannel(channel);
                        playerData.setColor(color);
                        channel.add(uuid);
                        this.playerData.put(uuid, playerData);
                        callback.accept(playerData);
                    } else {
                        PlayerData playerData = new PlayerData(uuid);
                        this.playerData.put(uuid, playerData);
                        callback.accept(playerData);
                    }
                }
            });
        });
    }

    public void updatePlayerData(PlayerData playerData) {
        this.async(() -> {
            this.databaseConnector.connect(connection -> {
                boolean create;

                String checkQuery = "SELECT 1 FROM " + this.getTablePrefix() + "player_data WHERE uuid = ?";
                try (PreparedStatement statement = connection.prepareStatement(checkQuery)) {
                    statement.setString(1, playerData.getUuid().toString());
                    ResultSet result = statement.executeQuery();
                    create = !result.next();
                }

                if (create) {
                    String insertQuery = "INSERT INTO " + this.getTablePrefix() + "player_data (uuid, social_spy, " +
                            "can_be_messaged, has_tag_sounds, has_message_sounds, current_channel, chat_color) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                        statement.setString(1, playerData.getUuid().toString());
                        statement.setBoolean(2, playerData.hasSocialSpy());
                        statement.setBoolean(3, playerData.canBeMessaged());
                        statement.setBoolean(4, playerData.hasTagSounds());
                        statement.setBoolean(5, playerData.hasMessageSounds());
                        statement.setString(6, playerData.getCurrentChannel().getId());
                        statement.setString(7, playerData.getColor());
                        statement.executeUpdate();
                    }
                } else {
                    String updateQuery = "UPDATE " + this.getTablePrefix() + "player_data SET " +
                            "social_spy = ?, can_be_messaged = ?, has_tag_sounds = ?, has_message_sounds = ?, current_channel = ?, chat_color = ? " +
                            "WHERE uuid = ?";
                    try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
                        statement.setBoolean(1, playerData.hasSocialSpy());
                        statement.setBoolean(2, playerData.canBeMessaged());
                        statement.setBoolean(3, playerData.hasTagSounds());
                        statement.setBoolean(4, playerData.hasMessageSounds());
                        statement.setString(5, playerData.getCurrentChannel().getId());
                        statement.setString(6, playerData.getColor());
                        statement.setString(7, playerData.getUuid().toString());
                        statement.executeUpdate();
                    }
                }
            });
        });
    }

    private void async(Runnable asyncCallback) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, asyncCallback);
    }

    private void sync(Runnable syncCallback) {
        Bukkit.getScheduler().runTask(this.rosePlugin, syncCallback);
    }

    public Map<UUID, PlayerData> getPlayerData() {
        return this.playerData;
    }

    public List<UUID> getSocialSpies() {
        List<UUID> spies = new ArrayList<>();
        for (PlayerData data : getPlayerData().values()) {
            if (data.hasSocialSpy()) spies.add(data.getUuid());
        }

        return spies;
    }
}
