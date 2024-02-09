package dev.rosewood.rosechat.placeholder.condition;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.configuration.ConfigurationSection;

/**
 * A placeholder condition for when no conditions are specified.
 */
public class NullPlaceholderCondition extends PlaceholderCondition {

    public NullPlaceholderCondition(ConfigurationSection section, String condition) {
        super(section, condition);
    }

    @Override
    public String parse(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        return this.values.get("default").get(0);
    }

    @Override
    public String parseToString(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        return this.parse(sender, viewer, placeholders);
    }

}
