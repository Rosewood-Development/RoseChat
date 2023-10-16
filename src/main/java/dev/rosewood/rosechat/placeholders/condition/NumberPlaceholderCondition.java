package dev.rosewood.rosechat.placeholders.condition;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.List;

/**
 * A placeholder condition for number values.
 * Allows checking if a value is more, less, or equal to a given number.
 */
public class NumberPlaceholderCondition extends PlaceholderCondition {

    public NumberPlaceholderCondition(ConfigurationSection section, String condition) {
        super(section, condition);
    }

    @Override
    public String parse(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        Player player = this.condition.startsWith("%other_") ? viewer.asPlayer() : sender.asPlayer();
        String condition = this.condition.replace("other_", "");
        String parsed = this.parsePlaceholders(player, viewer == null ? null : viewer.asPlayer(), condition, placeholders);

        List<String> valueList = this.values.get("value");
        if (valueList == null || valueList.isEmpty()) return "";

        String resultId = "default";

        try {
            double result = Double.parseDouble(parsed);
            double value = Double.parseDouble(valueList.get(0));
            if (result > value) resultId = "more";
            if (result == value) resultId = "equal";
            if (result < value) resultId = "less";
        } catch (NumberFormatException ignored) {

        }

        return resultId;
    }

    @Override
    public String parseToString(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        String parsed = this.parse(sender, viewer, placeholders);
        String result = this.combineConditionValues(parsed);
        return result == null || result.isEmpty() ? this.combineConditionValues("default") : result;
    }

}
