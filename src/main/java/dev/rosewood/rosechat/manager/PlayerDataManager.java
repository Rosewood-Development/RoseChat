package dev.rosewood.rosechat.manager;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;

public class PlayerDataManager extends Manager {

    private final DataManager dataManager;
    private final Map<UUID, PlayerData> playerData;
    private final Multimap<String, String> bungeePlayers;
    private final List<Channel> mutedChannels;

    public PlayerDataManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.playerData = new HashMap<>();
        this.dataManager = rosePlugin.getManager(DataManager.class);
        this.bungeePlayers = ArrayListMultimap.create();
        this.mutedChannels = new ArrayList<>();

        // Need to make sure this always gets loaded before the PlayerDataManager
        rosePlugin.getManager(ChannelManager.class);
    }

    @Override
    public void reload() {
        Bukkit.getOnlinePlayers().forEach(player -> this.getPlayerData(player.getUniqueId(), data -> { }));
        this.getMutedChannels((channels) -> {});
    }

    @Override
    public void disable() {
        this.playerData.clear();
        this.mutedChannels.clear();
    }

    public PlayerData getPlayerData(UUID uuid) {
        return this.playerData.get(uuid);
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

    /**
     * Gets the PlayerData synchronously. May cause a database query on the main thread.
     *
     * @param uuid The UUID of the player to load.
     * @return The PlayerData for the player.
     */
    public PlayerData getPlayerDataSynchronous(UUID uuid) {
        if (this.playerData.containsKey(uuid))
            return this.playerData.get(uuid);

        PlayerData playerData = this.dataManager.getPlayerData(uuid);
        this.playerData.put(uuid, playerData);
        return playerData;
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

   public void getMutedChannels(Consumer<List<Channel>> callback) {
        if (!this.mutedChannels.isEmpty()) {
            callback.accept(this.mutedChannels);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> {
            List<Channel> mutedChannels = this.dataManager.getMutedChannels();
            this.mutedChannels.addAll(mutedChannels);
            callback.accept(mutedChannels);
        });
    }

    public void addMutedChannel(Channel channel) {
        this.mutedChannels.add(channel);
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> this.dataManager.addMutedChannel(channel));
    }

    public void removeMutedChannel(Channel channel) {
        this.mutedChannels.remove(channel);
        Bukkit.getScheduler().runTaskAsynchronously(this.rosePlugin, () -> this.dataManager.removeMutedChannel(channel));
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

    public Multimap<String, String> getBungeePlayers() {
        return this.bungeePlayers;
    }

    public Collection<String> getPlayersOnServer(String server) {
        return this.bungeePlayers.get(server);
    }

}
