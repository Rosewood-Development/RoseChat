package dev.rosewood.rosechat.managers;

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

    private Map<UUID, PlayerData> playerData;

    public DataManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.playerData = new HashMap<>();
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerData.get(uuid);
    }

    public void getPlayerData(UUID uuid, Consumer<PlayerData> callback) {
        if (playerData.containsKey(uuid)) {
            callback.accept(playerData.get(uuid));
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

                        PlayerData playerData = new PlayerData(uuid);
                        playerData.setSocialSpy(socialSpy);
                        playerData.setCanBeMessaged(canBeMessaged);
                        playerData.setTagSounds(hasTagSounds);
                        playerData.setMessageSounds(hasMessageSounds);
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
                String updateQuery = "UPDATE " + this.getTablePrefix() + "player_data SET " +
                        "social_spy = ?, can_be_messaged = ?, has_tag_sounds = ?, has_message_sounds = ? " +
                        "WHERE uuid = ?";
                try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
                    statement.setBoolean(1, playerData.hasSocialSpy());
                    statement.setBoolean(2, playerData.canBeMessaged());
                    statement.setBoolean(3, playerData.hasTagSounds());
                    statement.setBoolean(4, playerData.hasMessageSounds());
                    statement.setString(5, playerData.getUuid().toString());
                    statement.addBatch();
                    statement.executeUpdate();
                }
            });
        });
    }

    public void removePlayerData(UUID uuid) {
        this.async(() -> {

        });
    }

    private void async(Runnable asyncCallback) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, asyncCallback);
    }

    private void sync(Runnable syncCallback) {
        Bukkit.getScheduler().runTask(this.rosePlugin, syncCallback);
    }

    public Map<UUID, PlayerData> getPlayerData() {
        return playerData;
    }

    public List<UUID> getSocialSpies() {
        List<UUID> spies = new ArrayList<>();
        for (PlayerData data : getPlayerData().values()) {
            if (data.hasSocialSpy()) spies.add(data.getUuid());
        }

        return spies;
    }
}
