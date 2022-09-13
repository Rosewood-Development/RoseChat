package dev.rosewood.rosechat.placeholders.condition;

import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.configuration.ConfigurationSection;

public class NullPlaceholderCondition extends PlaceholderCondition {

    public NullPlaceholderCondition(ConfigurationSection section, String condition) {
        super(section, condition);
    }

    @Override
    public String parse(RoseSender sender, RoseSender viewer, StringPlaceholders placeholders) {
        return this.combineConditionValues("default");
    }

    @Override
    public String parseToString(RoseSender sender, RoseSender viewer, StringPlaceholders placeholders) {
        return this.parse(sender, viewer, placeholders);
    }

    @Override
    public ClickEvent.Action parseToAction(RoseSender sender, RoseSender viewer, StringPlaceholders placeholders) {
        return this.getClickAction("default");
    }
}
