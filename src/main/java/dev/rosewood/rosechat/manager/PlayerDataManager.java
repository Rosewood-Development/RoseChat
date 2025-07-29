package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.bukkit.Bukkit;

public class PlayerDataManager extends Manager {

    private final Map<UUID, PlayerData> playerData;
    private DataManager dataManager;

    public PlayerDataManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        
        this.playerData = new ConcurrentHashMap<>();
    }

    @Override
    public void reload() {
        this.dataManager = this.rosePlugin.getManager(DataManager.class);

        // Delay to make sure channels are loaded first.
        Bukkit.getScheduler().runTaskLater(RoseChat.getInstance(), () -> {
            Bukkit.getOnlinePlayers().forEach(player -> this.getPlayerData(player.getUniqueId(), data -> {
                RosePlayer rosePlayer = new RosePlayer(player);

                // Put the player in the right channel when the plugin is reloaded.
                if (data.getCurrentChannel() != null) {
                    data.getCurrentChannel().onJoin(rosePlayer);
                } else {
                    Channel defaultChannel = RoseChatAPI.getInstance().getChannelManager().getDefaultChannel();
                    defaultChannel.onJoin(rosePlayer);
                    data.setCurrentChannel(defaultChannel);
                    data.save();
                }
            }));

            this.loadChannelSettings();
        }, 5L);
    }

    @Override
    public void disable() {
        this.playerData.clear();
    }

    /**
     * Gets the PlayerData synchronously. May cause a database query on the main thread.
     * Only use this if you expect the player data to already be loaded.
     *
     * @param uuid The UUID of the player to load.
     * @return The PlayerData for the player.
     */
    public PlayerData getPlayerData(UUID uuid) {
        return this.playerData.computeIfAbsent(uuid, this.dataManager::getPlayerData);
    }

    public void getPlayerData(UUID uuid, Consumer<PlayerData> callback) {
        if (this.playerData.containsKey(uuid)) {
            callback.accept(this.playerData.get(uuid));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            PlayerData playerData = this.dataManager.getPlayerData(uuid);
            this.playerData.put(uuid, playerData);
            callback.accept(playerData);
        });
    }

    public void updatePlayerData(PlayerData playerData) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            this.dataManager.updatePlayerData(playerData);
        });
    }

    public void unloadPlayerData(UUID uuid) {
        this.playerData.remove(uuid);
    }

    public void addIgnore(UUID ignoring, UUID ignored) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            this.dataManager.addIgnore(ignoring, ignored);
        });
    }

    public void removeIgnore(UUID ignoring, UUID ignored) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            this.dataManager.removeIgnore(ignoring, ignored);
        });
    }

    public void hideChannel(UUID uuid, String channel) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            this.dataManager.hideChannel(uuid, channel);
        });
    }

    public void showChannel(UUID uuid, String channel) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            this.dataManager.showChannel(uuid, channel);
        });
    }

    public void loadChannelSettings() {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, this.dataManager::loadChannelSettings);
    }

    public void saveChannelSettings(Channel channel) {
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> this.dataManager.saveChannelSettings(channel));
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

}
