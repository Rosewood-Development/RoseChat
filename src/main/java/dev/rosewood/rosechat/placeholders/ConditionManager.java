package dev.rosewood.rosechat.placeholders;

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

}
