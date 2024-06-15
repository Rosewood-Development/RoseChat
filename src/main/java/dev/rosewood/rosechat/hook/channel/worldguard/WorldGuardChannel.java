package dev.rosewood.rosechat.hook.channel.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class WorldGuardChannel extends RoseChatChannel {

    private final RegionContainer regionContainer;
    private List<String> whitelist;
    private List<String> blacklist;
    private boolean useMembers;

    public WorldGuardChannel(ChannelProvider provider) {
        super(provider);

        WorldGuardPlatform worldGuard = WorldGuard.getInstance().getPlatform();
        this.regionContainer = worldGuard.getRegionContainer();
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);

        this.whitelist = config.contains("whitelist") ? config.getStringList("whitelist") : new ArrayList<>();
        this.blacklist = config.contains("blacklist") ? config.getStringList("blacklist") : new ArrayList<>();
        this.useMembers = config.getBoolean("use-members") && config.getBoolean("use-members");
        if (!config.contains("visible-anywhere"))
            this.visibleAnywhere = true;
    }

    public boolean onEnterArea(RosePlayer player) {
        if (!this.getJoinCondition(player) || !this.autoJoin)
            return false;

        Location location = player.asPlayer().getLocation();
        if (location.getWorld() == null)
            return false;

        RegionManager regionManager = this.regionContainer.get(BukkitAdapter.adapt(location.getWorld()));
        if (regionManager == null)
            return false;

        ApplicableRegionSet regions = regionManager.getApplicableRegions(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        for (ProtectedRegion region : regions.getRegions()) {
            if (this.whitelist.contains(region.getId())) {
                return !this.useMembers || region.getMembers().contains(player.getUUID());
            }
        }

        return false;
    }

    @Override
    public boolean onLogin(RosePlayer player) {
        return this.onEnterArea(player);
    }

    @Override
    public boolean onWorldJoin(RosePlayer player, World from, World to) {
        return this.onEnterArea(player);
    }

    @Override
    public boolean onWorldLeave(RosePlayer player, World from, World to) {
        // Always leave the channel if auto-join is enabled.
        return this.autoJoin;
    }

    public Set<ProtectedRegion> getPlayerRegion(Player player) {
        RegionManager regionManager = this.regionContainer.get(BukkitAdapter.adapt(player.getWorld()));
        if (regionManager == null)
            return null;

        return regionManager.getApplicableRegions(BlockVector3.at(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ())).getRegions();
    }

    public boolean isInWhitelistedRegion(RosePlayer player) {
        Set<ProtectedRegion> regionSet = this.getPlayerRegion(player.asPlayer());
        if (regionSet == null)
            return false;

        for (ProtectedRegion region : regionSet) {
            if (this.whitelist.contains(region.getId()))
                return true;
        }

        return false;
    }

    public boolean isInBlacklistedRegion(RosePlayer player) {
        Set<ProtectedRegion> regionSet = this.getPlayerRegion(player.asPlayer());
        if (regionSet == null)
            return false;

        for (ProtectedRegion region : regionSet) {
            if (this.blacklist.contains(region.getId()))
                return true;
        }

        return false;
    }

    @Override
    public List<Player> getSpies(Predicate<Player> condition) {
        return super.getSpies(condition.or(player -> ((!this.whitelist.isEmpty() && !isInWhitelistedRegion(new RosePlayer(player)))
                || (this.whitelist.isEmpty() && isInBlacklistedRegion(new RosePlayer(player))))));
    }

    public List<Player> getRecipients(RosePlayer sender, List<Player> globalRecipients) {
        List<Player> recipients = new ArrayList<>();

        if (this.useMembers && sender.isPlayer()) {
            Player player = sender.asPlayer();

            for (String regionStr : this.whitelist) {
                RegionManager manager = this.regionContainer.get(BukkitAdapter.adapt(player.getWorld()));
                if (manager == null)
                    return recipients;

                ProtectedRegion region = manager.getRegion(regionStr);
                if (region == null)
                    return recipients;

                for (UUID memberUUID : region.getMembers().getUniqueIds()) {
                    Player member = Bukkit.getPlayer(memberUUID);
                    if (member != null && this.getReceiveCondition(sender, new RosePlayer(member)))
                        recipients.add(member);
                }
            }

            return recipients;
        }

        if (!this.whitelist.isEmpty()) {
            // If using a whitelist, send to all players in the region.
            for (Player player : globalRecipients) {
                RosePlayer rosePlayer = new RosePlayer(player);
                if (this.getReceiveCondition(sender, rosePlayer) && this.isInWhitelistedRegion(rosePlayer)) {
                    recipients.add(player);
                }
            }

        } else {
            // If using a blacklist, send to everyone EXCEPT players in the region.
            for (Player player : globalRecipients) {
                RosePlayer rosePlayer = new RosePlayer(player);
                if (this.getReceiveCondition(sender, rosePlayer) && !this.isInBlacklistedRegion(rosePlayer)) {
                    recipients.add(player);
                }
            }
        }

        return recipients;
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        return this.getRecipients(sender, new ArrayList<>(Bukkit.getOnlinePlayers()));
    }

    @Override
    public List<Player> getMemberRecipients(RosePlayer sender, World world) {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : this.members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null)
                continue;

            players.add(player);
        }

        return this.getRecipients(sender, players);
    }

    @Override
    public int getMemberCount() {
        int count = this.whitelist.isEmpty() ? Bukkit.getOnlinePlayers().size() : 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            RosePlayer rosePlayer = new RosePlayer(player);

            if (!this.whitelist.isEmpty()) {
                if (this.getReceiveCondition(rosePlayer, rosePlayer) && this.isInWhitelistedRegion(rosePlayer))
                    count++;
            } else {
                if (this.getReceiveCondition(rosePlayer, rosePlayer) && this.isInBlacklistedRegion(rosePlayer))
                    count--;
            }
        }

        return count;
    }

    @Override
    public boolean canJoinByCommand(RosePlayer player) {
        return player.hasPermission("rosechat.channelbypass") ||
                (player.hasPermission("rosechat.channel." + this.getId()) && this.joinable && this.getJoinCondition(player) && this.isInWhitelistedRegion(player));
    }

    @Override
    public StringPlaceholders.Builder getInfoPlaceholders() {
        LocaleManager localeManager = RoseChatAPI.getInstance().getLocaleManager();
        String trueValue = localeManager.getLocaleMessage("command-chat-info-true");
        String falseValue = localeManager.getLocaleMessage("command-chat-info-false");

        return super.getInfoPlaceholders()
                .add("regions", this.whitelist.isEmpty() ? this.blacklist.toString() : this.whitelist.toString())
                .add("use-members", this.useMembers ? trueValue : falseValue);
    }

    public List<String> getWhitelist() {
        return this.whitelist;
    }

    public List<String> getBlacklist() {
        return this.blacklist;
    }

}
