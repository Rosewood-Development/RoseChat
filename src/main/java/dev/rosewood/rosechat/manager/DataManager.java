package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.MuteTask;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.message.DeletableMessage;
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

    private final ChannelManager channelManager;
    private final Map<UUID, PlayerData> playerData;
    private final Map<UUID, MuteTask> muteTasks;
    private final Map<String, List<String>> bungeePlayers;
    private final List<ChatChannel> mutedChannels;

    public DataManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.playerData = new HashMap<>();
        this.channelManager = rosePlugin.getManager(ChannelManager.class);
        this.muteTasks = new HashMap<>();
        this.bungeePlayers = new HashMap<>();
        this.mutedChannels = new ArrayList<>();
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
                PlayerData playerData;

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
                        boolean hasEmojis = result.getBoolean("has_emojis");
                        String currentChannel = result.getString("current_channel");
                        String color = result.getString("chat_color");
                        long muteTime = result.getLong("mute_time");
                        String nickname = result.getString("nickname");
                        ChatChannel channel = this.channelManager.getChannel(currentChannel);

                        playerData = new PlayerData(uuid);
                        playerData.setMessageSpy(messageSpy);
                        playerData.setChannelSpy(channelSpy);
                        playerData.setGroupSpy(groupSpy);
                        playerData.setCanBeMessaged(canBeMessaged);
                        playerData.setTagSounds(hasTagSounds);
                        playerData.setMessageSounds(hasMessageSounds);
                        playerData.setEmojis(hasEmojis);
                        playerData.setCurrentChannel(channel);
                        playerData.setColor(color);
                        playerData.setMuteTime(muteTime);
                        playerData.setNickname(nickname);
                        this.playerData.put(uuid, playerData);
                        if (muteTime > 0) this.muteTasks.put(uuid, new MuteTask(playerData));
                        callback.accept(playerData);
                    } else {
                        playerData = new PlayerData(uuid);
                        this.playerData.put(uuid, playerData);
                        callback.accept(playerData);
                    }
                }

                String ignoreQuery = "SELECT * FROM " + this.getTablePrefix() + "player_data_ignore WHERE ignoring_uuid = ?";
                try (PreparedStatement statement = connection.prepareStatement(ignoreQuery)) {
                    statement.setString(1, uuid.toString());
                    ResultSet result = statement.executeQuery();

                    if (result.next()) {
                        UUID ignored = UUID.fromString(result.getString("ignored_uuid"));
                        playerData.ignore(ignored);
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
                    statement.setString(1, playerData.getUUID().toString());
                    ResultSet result = statement.executeQuery();
                    create = !result.next();
                }

                if (create) {
                    String insertQuery = "INSERT INTO " + this.getTablePrefix() + "player_data (uuid, has_message_spy, has_channel_spy, has_group_spy, " +
                            "can_be_messaged, has_tag_sounds, has_message_sounds, has_emojis, current_channel, chat_color, mute_time, nickname) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                        statement.setString(1, playerData.getUUID().toString());
                        statement.setBoolean(2, playerData.hasMessageSpy());
                        statement.setBoolean(3, playerData.hasChannelSpy());
                        statement.setBoolean(4, playerData.hasGroupSpy());
                        statement.setBoolean(5, playerData.canBeMessaged());
                        statement.setBoolean(6, playerData.hasTagSounds());
                        statement.setBoolean(7, playerData.hasMessageSounds());
                        statement.setBoolean(8, playerData.hasEmojis());
                        statement.setString(9, playerData.getCurrentChannel().getId());
                        statement.setString(10, playerData.getColor());
                        statement.setLong(11, playerData.getMuteTime());
                        statement.setString(12, playerData.getNickname());
                        statement.executeUpdate();
                    }
                } else {
                    String updateQuery = "UPDATE " + this.getTablePrefix() + "player_data SET has_message_spy = ?, has_channel_spy = ?, has_group_spy = ?, " +
                            "can_be_messaged = ?, has_tag_sounds = ?, has_message_sounds = ?, has_emojis = ?, current_channel = ?, chat_color = ?, mute_time = ?, nickname = ? " +
                            "WHERE uuid = ?";
                    try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
                        statement.setBoolean(1, playerData.hasMessageSpy());
                        statement.setBoolean(2, playerData.hasChannelSpy());
                        statement.setBoolean(3, playerData.hasGroupSpy());
                        statement.setBoolean(4, playerData.canBeMessaged());
                        statement.setBoolean(5, playerData.hasTagSounds());
                        statement.setBoolean(6, playerData.hasMessageSounds());
                        statement.setBoolean(7, playerData.hasEmojis());
                        statement.setString(8, playerData.getCurrentChannel().getId());
                        statement.setString(9, playerData.getColor());
                        statement.setLong(10, playerData.getMuteTime());
                        statement.setString(11, playerData.getNickname());
                        statement.setString(12, playerData.getUUID().toString());
                        statement.executeUpdate();
                    }
                }
            });
        });
    }

    public void addIgnore(UUID ignoring, UUID ignored) {
        this.async(() -> {
            this.getDatabaseConnector().connect(connection -> {
                String insertQuery = "INSERT INTO " + this.getTablePrefix() + "player_data_ignore (ignoring_uuid, ignored_uuid) " +
                        "VALUES(?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                    statement.setString(1, ignoring.toString());
                    statement.setString(2, ignored.toString());
                    statement.executeUpdate();
                }
            });
        });
    }

    public void removeIgnore(UUID ignoring, UUID ignored) {
        this.async(() -> {
            this.getDatabaseConnector().connect(connection -> {
                String deleteQuery = "DELETE FROM " + this.getTablePrefix() + "player_data_ignore WHERE ignoring_uuid = ? AND ignored_uuid = ?";
                try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                    statement.setString(1, ignoring.toString());
                    statement.setString(2, ignored.toString());
                    statement.executeUpdate();
                }
            });
        });
    }

    public void getMutedChannels(Consumer<List<ChatChannel>> callback) {
        if (!this.mutedChannels.isEmpty()) {
            callback.accept(this.mutedChannels);
            return;
        }

        this.async(() -> {
            this.databaseConnector.connect(connection -> {
                List<ChatChannel> mutedChannels = new ArrayList<>();
                String dataQuery = "SELECT * FROM " + this.getTablePrefix() + "muted_channels";
                try (PreparedStatement statement = connection.prepareStatement(dataQuery)) {
                    ResultSet result = statement.executeQuery();

                    while (result.next()) {
                        ChatChannel channel = this.channelManager.getChannel(result.getString("id"));
                        channel.setMuted(true);
                        mutedChannels.add(channel);
                    }

                    callback.accept(mutedChannels);
                }
            });
        });
    }

    public void addMutedChannel(ChatChannel channel) {
        this.mutedChannels.add(channel);
        this.async(() -> {
            this.getDatabaseConnector().connect(connection -> {
                String insertQuery = "INSERT INTO " + this.getTablePrefix() + "muted_channels (id) " +
                        "VALUES(?)";
                try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                    statement.setString(1, channel.getId());
                    statement.executeUpdate();
                }
            });
        });
    }

    public void removeMutedChannel(ChatChannel channel) {
        this.mutedChannels.remove(channel);
        this.async(() -> {
            this.getDatabaseConnector().connect(connection -> {
                String deleteQuery = "DELETE FROM " + this.getTablePrefix() + "muted_channels WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                    statement.setString(1, channel.getId());
                    statement.executeUpdate();
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
            if (data.hasMessageSpy()) spies.add(data.getUUID());
        }

        return spies;
    }

    public List<UUID> getChannelSpies() {
        List<UUID> spies = new ArrayList<>();
        for (PlayerData data : this.getPlayerData().values()) {
            if (data.hasChannelSpy()) spies.add(data.getUUID());
        }

        return spies;
    }

    public List<UUID> getGroupSpies() {
        List<UUID> spies = new ArrayList<>();
        for (PlayerData data : this.getPlayerData().values()) {
            if (data.hasGroupSpy()) spies.add(data.getUUID());
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
