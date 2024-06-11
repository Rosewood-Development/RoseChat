package dev.rosewood.rosechat.hook.channel.condition;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.placeholder.condition.PlaceholderCondition;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.configuration.ConfigurationSection;
import java.util.ArrayList;
import java.util.List;

public class MultipleChannelCondition extends ChannelCondition {

    private final List<String> conditionStrs;
    private final List<ChannelCondition> conditions;

    public MultipleChannelCondition(ConfigurationSection section, List<String> conditions) {
        super(section, null);

        this.conditionStrs = conditions;
        this.conditions = new ArrayList<>();
    }

    @Override
    public boolean parseToBoolean(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        boolean result = false;
        boolean isFirst = true;
        for (ChannelCondition condition : this.conditions) {
            if (isFirst) {
                result = condition.parseToBoolean(sender, viewer, placeholders);
                isFirst = false;
            }
            else result = result && condition.parseToBoolean(sender, viewer, placeholders);
        }

        return result;
    }

    @Override
    public PlaceholderCondition parseValues() {
        for (String condition : this.conditionStrs) {
            ChannelCondition channelCondition = condition.contains("||") ?
                    new CompoundChannelCondition(this.section, condition) : new ChannelCondition(this.section, condition);
            channelCondition.parseValues();
            this.conditions.add(channelCondition);
        }

        return this;
    }

}
