package dev.rosewood.rosechat.placeholders.condition;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.ClickEvent;
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
        return this.combineConditionValues("default");
    }

    @Override
    public String parseToString(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        return this.parse(sender, viewer, placeholders);
    }

    @Override
    public ClickEvent.Action parseToAction(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        return this.getClickAction("default");
    }

}
