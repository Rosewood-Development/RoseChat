package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.MuteTask;
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
    private Map<UUID, MuteTask> muteTasks;
    private Map<String, List<String>> bungeePlayers;

    public DataManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.playerData = new HashMap<>();
        this.channelManager = rosePlugin.getManager(ChannelManager.class);
        this.muteTasks = new HashMap<>();
        this.bungeePlayers = new HashMap<>();
    }

    public PlayerData getPlayerData(UUID uuid) {
        return this.playerData.get(uuid);
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
                        boolean messageSpy = result.getBoolean("has_message_spy");
                        boolean channelSpy = result.getBoolean("has_channel_spy");
                        boolean groupSpy = result.getBoolean("has_group_spy");
                        boolean canBeMessaged = result.getBoolean("can_be_messaged");
                        boolean hasTagSounds = result.getBoolean("has_tag_sounds");
                        boolean hasMessageSounds = result.getBoolean("has_message_sounds");
                        String currentChannel = result.getString("current_channel");
                        String color = result.getString("chat_color");
                        long muteTime = result.getLong("mute_time");
                        ChatChannel channel = this.channelManager.getChannel(currentChannel);

                        PlayerData playerData = new PlayerData(uuid);
                        playerData.setMessageSpy(messageSpy);
                        playerData.setChannelSpy(channelSpy);
                        playerData.setGroupSpy(groupSpy);
                        playerData.setCanBeMessaged(canBeMessaged);
                        playerData.setTagSounds(hasTagSounds);
                        playerData.setMessageSounds(hasMessageSounds);
                        playerData.setCurrentChannel(channel);
                        playerData.setColor(color);
                        playerData.setMuteTime(muteTime);
                        channel.add(uuid);
                        this.playerData.put(uuid, playerData);
                        if (muteTime > 0) this.muteTasks.put(uuid, new MuteTask(playerData));
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
                    String insertQuery = "INSERT INTO " + this.getTablePrefix() + "player_data (uuid, has_message_spy, has_channel_spy, has_group_spy, " +
                            "can_be_messaged, has_tag_sounds, has_message_sounds, current_channel, chat_color, mute_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                        statement.setString(1, playerData.getUuid().toString());
                        statement.setBoolean(2, playerData.hasMessageSpy());
                        statement.setBoolean(3, playerData.hasChannelSpy());
                        statement.setBoolean(4, playerData.hasGroupSpy());
                        statement.setBoolean(5, playerData.canBeMessaged());
                        statement.setBoolean(6, playerData.hasTagSounds());
                        statement.setBoolean(7, playerData.hasMessageSounds());
                        statement.setString(8, playerData.getCurrentChannel().getId());
                        statement.setString(9, playerData.getColor());
                        statement.setLong(10, playerData.getMuteTime());
                        statement.executeUpdate();
                    }
                } else {
                    String updateQuery = "UPDATE " + this.getTablePrefix() + "player_data SET has_message_spy = ?, has_channel_spy = ?, has_group_spy = ?, " +
                            "can_be_messaged = ?, has_tag_sounds = ?, has_message_sounds = ?, current_channel = ?, chat_color = ?, mute_time = ? " +
                            "WHERE uuid = ?";
                    try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
                        statement.setBoolean(1, playerData.hasMessageSpy());
                        statement.setBoolean(2, playerData.hasChannelSpy());
                        statement.setBoolean(3, playerData.hasGroupSpy());
                        statement.setBoolean(4, playerData.canBeMessaged());
                        statement.setBoolean(5, playerData.hasTagSounds());
                        statement.setBoolean(6, playerData.hasMessageSounds());
                        statement.setString(7, playerData.getCurrentChannel().getId());
                        statement.setString(8, playerData.getColor());
                        statement.setLong(9, playerData.getMuteTime());
                        statement.setString(10, playerData.getUuid().toString());
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

    public List<UUID> getMessageSpies() {
        List<UUID> spies = new ArrayList<>();
        for (PlayerData data : this.getPlayerData().values()) {
            if (data.hasMessageSpy()) spies.add(data.getUuid());
        }

        return spies;
    }

    public List<UUID> getChannelSpies() {
        List<UUID> spies = new ArrayList<>();
        for (PlayerData data : this.getPlayerData().values()) {
            if (data.hasChannelSpy()) spies.add(data.getUuid());
        }

        return spies;
    }

    public List<UUID> getGroupSpies() {
        List<UUID> spies = new ArrayList<>();
        for (PlayerData data : this.getPlayerData().values()) {
            if (data.hasGroupSpy()) spies.add(data.getUuid());
        }

        return spies;
    }

    public Map<UUID, MuteTask> getMuteTasks() {
        return this.muteTasks;
    }

    public Map<String, List<String>> getBungeePlayers() {
        return this.bungeePlayers;
    }

    public List<String> getPlayersOnServer(String server) {
        return this.bungeePlayers.get(server);
    }
}
