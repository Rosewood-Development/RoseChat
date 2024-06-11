package dev.rosewood.rosechat.hook.channel.condition;

import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.placeholder.ConditionManager;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public abstract class ConditionalChannel extends Channel {

    private ChannelCondition joinCondition;
    private ChannelCondition receiveCondition;

    public ConditionalChannel(ChannelProvider provider) {
        super(provider);
    }

    @Override
    public void onLoad(String id, ConfigurationSection config) {
        super.onLoad(id, config);

        if (config.contains("join-conditions")) {
            this.joinCondition = ConditionManager.getChannelCondition(config, "join-conditions");
            if (this.joinCondition != null)
                this.joinCondition.parseValues();
        }

        if (config.contains("receive-conditions")) {
            this.receiveCondition = ConditionManager.getChannelCondition(config, "receive-conditions");
            if (this.receiveCondition != null)
                this.receiveCondition.parseValues();
        }
    }

    public boolean getJoinCondition(RosePlayer player, StringPlaceholders placeholders) {
        return this.joinCondition == null || this.joinCondition.parseToBoolean(player, null, placeholders);
    }

    public boolean getJoinCondition(Player player, StringPlaceholders placeholders) {
        return this.getJoinCondition(new RosePlayer(player), placeholders);
    }

    public boolean getJoinCondition(Player player) {
        return this.getJoinCondition(player, StringPlaceholders.empty());
    }

    public boolean getJoinCondition(RosePlayer player) {
        return this.getJoinCondition(player, StringPlaceholders.empty());
    }

    public boolean getReceiveCondition(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        return this.receiveCondition == null || this.receiveCondition.parseToBoolean(sender, viewer, placeholders);
    }

    public boolean getReceiveCondition(Player sender, Player viewer, StringPlaceholders placeholders) {
        return this.getReceiveCondition(new RosePlayer(sender), new RosePlayer(viewer), placeholders);
    }

    public boolean getReceiveCondition(Player sender, Player viewer) {
        return this.getReceiveCondition(sender, viewer, StringPlaceholders.empty());
    }

    public boolean getReceiveCondition(RosePlayer sender, Player viewer) {
        return this.getReceiveCondition(sender, new RosePlayer(viewer));
    }

    public boolean getReceiveCondition(RosePlayer sender, RosePlayer viewer) {
        return this.getReceiveCondition(sender, viewer, StringPlaceholders.empty());
    }

}
