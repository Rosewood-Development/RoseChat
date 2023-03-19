package dev.rosewood.rosechat.hook.channel.simpleclans;

import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class SimpleClansChannel extends RoseChatChannel {

    private SimpleClansChannelType channelType;

    public SimpleClansChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);

        if (config.contains("channel-type")) this.channelType = SimpleClansChannelType.valueOf(config.getString("channel-type").toUpperCase());
        if (!config.contains("visible-anywhere")) this.visibleAnywhere = true;
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        List<Player> recipients = new ArrayList<>();

        if (!sender.isPlayer()) return recipients;
        Clan clan = SimpleClans.getInstance().getClanManager().getClanByPlayerUniqueId(sender.getUUID());
        if (clan == null) return recipients;

        if (this.channelType == SimpleClansChannelType.CLAN) {
            for (ClanPlayer clanPlayer : clan.getMembers()) {
                if (clanPlayer == null) continue;
                Player player = clanPlayer.toPlayer();
                if (player != null && this.getReceiveCondition(sender, player)) recipients.add(player);
            }
        } else {
            for (ClanPlayer clanPlayer : clan.getAllAllyMembers()) {
                if (clanPlayer == null) continue;
                Player player = clanPlayer.toPlayer();
                if (player != null && this.getReceiveCondition(sender, player)) recipients.add(player);
            }
        }

        return recipients;
    }

    @Override
    public boolean canJoinByCommand(Player player) {
        Clan clan = SimpleClans.getInstance().getClanManager().getClanByPlayerUniqueId(player.getUniqueId());
        if (clan == null) return false;

        return super.canJoinByCommand(player);
    }

    @Override
    public StringPlaceholders.Builder getInfoPlaceholders(RosePlayer sender, String trueValue, String falseValue, String nullValue) {
        return super.getInfoPlaceholders(sender, trueValue, falseValue, nullValue)
                .addPlaceholder("type", this.channelType.toString().toLowerCase());
    }

    public SimpleClansChannelType getChannelType() {
        return this.channelType;
    }

    public enum SimpleClansChannelType {

        CLAN,
        ALLY

    }

}
