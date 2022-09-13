package dev.rosewood.rosechat.placeholders.condition;

import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.List;

public class NumberPlaceholderCondition extends PlaceholderCondition {

    public NumberPlaceholderCondition(ConfigurationSection section, String condition) {
        super(section, condition);
    }

    @Override
    public String parse(RoseSender sender, RoseSender viewer, StringPlaceholders placeholders) {
        Player player = this.condition.startsWith("%other_") ? viewer.asPlayer() : sender.asPlayer();
        String condition = this.condition.replace("other_", "");
        String parsed = this.parsePlaceholders(player, condition, placeholders);

        List<String> valueList = this.conditionValues.get("value");
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
    public String parseToString(RoseSender sender, RoseSender viewer, StringPlaceholders placeholders) {
        String parsed = this.parse(sender, viewer, placeholders);
        String result = this.combineConditionValues(parsed);
        return result == null || result.isEmpty() ? this.combineConditionValues("default") : result;
    }

    @Override
    public ClickEvent.Action parseToAction(RoseSender sender, RoseSender viewer, StringPlaceholders placeholders) {
        String parsed = this.parse(sender, viewer, placeholders);
        return this.getClickAction(parsed);
    }

}
