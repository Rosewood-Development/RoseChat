package dev.rosewood.rosechat.placeholder.condition;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class StringPlaceholderCondition extends PlaceholderCondition {

    public StringPlaceholderCondition(ConfigurationSection section, String condition) {
        super(section, condition);
    }

    @Override
    protected String parse(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        StringBuilder resultBuilder = new StringBuilder();
        for (String placeholder : this.condition.split(",")) {
            String parsed;

            if (placeholder.contains("||")) {
                parsed = String.valueOf(new CompoundPlaceholderCondition(this.section, placeholder).parseValues()
                        .parseToBoolean(sender, viewer, placeholders));
            } else {
                Player player = this.condition.startsWith("%other_") ? viewer.asPlayer() : sender.asPlayer();
                String condition = placeholder.replace("other_", "");
                parsed = this.parsePlaceholders(player, viewer == null ? null : viewer.asPlayer(), condition, placeholders);

                // Convert PAPI 'yes' and 'no' to 'true' and 'false'
                parsed = parsed.equalsIgnoreCase("yes") ? "true" : parsed;
                parsed = parsed.equalsIgnoreCase("no") ? "false" : parsed;
            }

            if (resultBuilder.length() != 0)
                resultBuilder.append(",");

            boolean hasCondition = false;
            for (String conditionValue : this.values.keySet()) {
                hasCondition = conditionValue.toLowerCase()
                        .contains(ChatColor.stripColor(resultBuilder.toString().toLowerCase() + parsed.toLowerCase()));
                if (hasCondition)
                    break;
            }

            resultBuilder.append(hasCondition ? ChatColor.stripColor(parsed.toLowerCase()) : "default");
        }

        return resultBuilder.toString();
    }

}
