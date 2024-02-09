package dev.rosewood.rosechat.hook.channel.condition;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.placeholder.condition.PlaceholderCondition;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.configuration.ConfigurationSection;
import java.util.ArrayList;
import java.util.List;

public class CompoundChannelCondition extends ChannelCondition {

    private final List<ChannelCondition> conditions;

    public CompoundChannelCondition(ConfigurationSection section, String condition) {
        super(section, condition);
        this.conditions = new ArrayList<>();
    }

    @Override
    public boolean parseToBoolean(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        boolean result = false;
        for (ChannelCondition condition : this.conditions)
            result = result || condition.parseToBoolean(sender, viewer, placeholders);

        return result;
    }

    @Override
    public PlaceholderCondition parseValues() {
        for (String condition : this.condition.split("\\|\\|")) {
            ChannelCondition channelCondition = new ChannelCondition(this.section, condition);
            channelCondition.parseValues();
            this.conditions.add(channelCondition);
        }

        return this;
    }

}
