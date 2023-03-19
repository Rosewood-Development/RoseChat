package dev.rosewood.rosechat.hook.channel.kingdomsx;

import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannel;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.group.model.relationships.KingdomRelation;
import org.kingdoms.constants.player.KingdomPlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KingdomsXChannel extends RoseChatChannel {

    private KingdomsChannelType channelType;

    public KingdomsXChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);

        if (config.contains("channel-type")) this.channelType = KingdomsChannelType.valueOf(config.getString("channel-type").toUpperCase());
        if (!config.contains("visible-anywhere")) this.visibleAnywhere = true;
    }

    public List<Player> getRecipientsByRelation(RosePlayer sender, Kingdom kingdom, KingdomRelation relation) {
        List<Player> recipients = new ArrayList<>();

        for (Kingdom k : kingdom.getKingdomsWithRelation(relation)) {
            for (UUID uuid : k.getMembers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && this.getReceiveCondition(sender, player)) recipients.add(player);
            }
        }

        return recipients;
    }

    @Override
    public List<Player> getVisibleAnywhereRecipients(RosePlayer sender, World world) {
        List<Player> recipients = new ArrayList<>();

        if (!sender.isPlayer()) return recipients;
        Kingdom kingdom = KingdomPlayer.getKingdomPlayer(sender.getUUID()).getKingdom();
        if (kingdom == null) return recipients;

        switch (this.channelType) {
            case KINGDOM: {
                for (UUID uuid : kingdom.getMembers()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && this.getReceiveCondition(sender, player)) recipients.add(player);
                }

                return recipients;
            }

            case NATION: {
                if (kingdom.getNation() == null) return recipients;
                for (Kingdom nation : kingdom.getNation().getKingdoms()) {
                    for (UUID uuid : nation.getMembers()) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && this.getReceiveCondition(sender, player)) recipients.add(player);
                    }
                }

                return recipients;
            }

            case ALLY: {
                recipients.addAll(this.getRecipientsByRelation(sender, kingdom, KingdomRelation.ALLY));
                return recipients;
            }

            case TRUCE: {
                recipients.addAll(this.getRecipientsByRelation(sender, kingdom, KingdomRelation.TRUCE));
                return recipients;
            }

            case ENEMY: {
                recipients.addAll(this.getRecipientsByRelation(sender, kingdom, KingdomRelation.ENEMY));
                return recipients;
            }

        }

        return recipients;
    }

    @Override
    public boolean canJoinByCommand(Player player) {
        Kingdom kingdom = KingdomPlayer.getKingdomPlayer(player.getUniqueId()).getKingdom();
        if (kingdom == null) return false;

        return super.canJoinByCommand(player);
    }

    @Override
    public StringPlaceholders.Builder getInfoPlaceholders(RosePlayer sender, String trueValue, String falseValue, String nullValue) {
        return super.getInfoPlaceholders(sender, trueValue, falseValue, nullValue)
                .addPlaceholder("type", this.channelType.toString().toLowerCase());
    }

    public enum KingdomsChannelType {

        NATION,
        KINGDOM,
        ALLY,
        TRUCE,
        ENEMY

    }

}
