package dev.rosewood.rosechat.hook.channel.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorldGuardChannel extends RoseChatChannel {

    private final WorldGuardPlatform worldGuard;
    private final RegionContainer regionContainer;
    private List<String> whitelist;
    private List<String> blacklist;
    private boolean useMembers;

    public WorldGuardChannel(ChannelProvider provider) {
        super(provider);
        this.worldGuard = WorldGuard.getInstance().getPlatform();
        this.regionContainer = this.worldGuard.getRegionContainer();
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);
        this.whitelist = config.contains("whitelist") ? config.getStringList("whitelist") : new ArrayList<>();
        this.blacklist = config.contains("blacklist") ? config.getStringList("blacklist") : new ArrayList<>();
        this.useMembers = config.getBoolean("use-members") && config.getBoolean("use-members");
    }

    @Override
    public void send(RosePlayer sender, String message) {
        for (UUID uuid : this.getMembers(sender)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            player.sendMessage("[Region] " + message);
        }
    }

    // TODO: Sudo player needs a world
    @Override
    public List<UUID> getMembers(RosePlayer sender) {
        List<UUID> members = new ArrayList<>();
        World world = sender.asPlayer().getWorld();

        // There can only be a whitelist OR a blacklist.
        // The whitelist contains regions to send the message to, all other regions will not receive the message.
        // The blacklist contains regions to NOT send the message to, all other regions will receive the message.
        // There's no point in looping through the blacklist if there is already a whitelist.
        boolean isBlacklist = this.whitelist.isEmpty();
        List<ProtectedRegion> regions = new ArrayList<>();
        for (String regionName : (isBlacklist ? this.blacklist : this.whitelist)) {
            RegionManager regionManager = this.regionContainer.get(BukkitAdapter.adapt(world));

            if (regionManager == null) return new ArrayList<>();
            ProtectedRegion region = regionManager.getRegion(regionName.toLowerCase());
            if (region == null) continue;
            else regions.add(region);
        }

        // Use the region members if the setting is enabled.
        if (this.useMembers) {
            for (ProtectedRegion region : regions) {
                members.addAll(region.getMembers().getUniqueIds());
            }

            return members;
        }

        // Loop through the players in the world, if their position is in the region - send!
        for (Player player : world.getPlayers()) {
            Location location = player.getLocation();

            for (ProtectedRegion region : regions) {
                if (isBlacklist) {
                    if (region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) continue;
                    members.add(player.getUniqueId());
                } else {
                    if (!region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) continue;
                    members.add(player.getUniqueId());
                }
            }
        }

        return members;
    }

    public List<String> getWhitelist() {
        return this.whitelist;
    }

    public List<String> getBlacklist() {
        return this.blacklist;
    }

    public boolean useMembers() {
        return this.useMembers;
    }

}
