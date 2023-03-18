package dev.rosewood.rosechat.placeholders;

import dev.rosewood.rosechat.hook.channel.condition.ChannelCondition;
import dev.rosewood.rosechat.hook.channel.condition.CompoundChannelCondition;
import dev.rosewood.rosechat.hook.channel.condition.MultipleChannelCondition;
import dev.rosewood.rosechat.placeholders.condition.BooleanPlaceholderCondition;
import dev.rosewood.rosechat.placeholders.condition.CompoundPlaceholderCondition;
import dev.rosewood.rosechat.placeholders.condition.NullPlaceholderCondition;
import dev.rosewood.rosechat.placeholders.condition.NumberPlaceholderCondition;
import dev.rosewood.rosechat.placeholders.condition.PlaceholderCondition;
import dev.rosewood.rosechat.placeholders.condition.StringPlaceholderCondition;
import org.bukkit.configuration.ConfigurationSection;

public class ConditionManager {

    public static PlaceholderCondition getCondition(ConfigurationSection parentSection, String condition) {
        if (condition == null || condition.trim().isEmpty())
            return new NullPlaceholderCondition(parentSection, condition);

        if (condition.contains(","))
            return new StringPlaceholderCondition(parentSection, condition);

        if (condition.contains("||"))
            return new CompoundPlaceholderCondition(parentSection, condition);

        if (parentSection.contains("value"))
            return new NumberPlaceholderCondition(parentSection, condition);

        if (parentSection.contains("true") || parentSection.contains("false"))
            return new BooleanPlaceholderCondition(parentSection, condition);

        return new StringPlaceholderCondition(parentSection, condition);
    }

    public static ChannelCondition getChannelCondition(ConfigurationSection parentSection, String configNode) {
        if (!parentSection.contains(configNode))
            return null;

        if (parentSection.isList(configNode))
            return new MultipleChannelCondition(parentSection, parentSection.getStringList(configNode));

        String condition = parentSection.getString(configNode);
        if (condition == null || condition.trim().isEmpty())
            return null;

        if (condition.contains("||"))
            return new CompoundChannelCondition(parentSection, condition);

        return new ChannelCondition(parentSection, condition);
    }

}
