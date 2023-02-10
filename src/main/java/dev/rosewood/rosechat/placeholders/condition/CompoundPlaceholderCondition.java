package dev.rosewood.rosechat.placeholders.condition;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.configuration.ConfigurationSection;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder condition that contains multiple conditions.
 */
public class CompoundPlaceholderCondition extends PlaceholderCondition {

    private final List<BooleanPlaceholderCondition> conditions;

    public CompoundPlaceholderCondition(ConfigurationSection section, String condition) {
        super(section, condition);
        this.conditions = new ArrayList<>();
    }

    @Override
    public String parse(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        boolean result = false;
        for (BooleanPlaceholderCondition condition : this.conditions)
            result = result || condition.parseToBoolean(sender, viewer, placeholders);

        return String.valueOf(result);
    }

    @Override
    public String parseToString(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        String parsed = this.parse(sender, viewer, placeholders);
        String result = this.combineConditionValues(parsed);
        return this.combineConditionValues(String.valueOf(result));
    }

    public boolean parseToBoolean(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        boolean result = false;
        for (BooleanPlaceholderCondition condition : this.conditions)
            result = result || condition.parseToBoolean(sender, viewer, placeholders);

        return result;
    }

    @Override
    public ClickEvent.Action parseToAction(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        String parsed = this.parse(sender, viewer, placeholders);
        return this.getClickAction(parsed);
    }

    @Override
    public PlaceholderCondition parseValues() {
        for (String condition : this.condition.split("\\|\\|")) {
            BooleanPlaceholderCondition booleanPlaceholderCondition = new BooleanPlaceholderCondition(this.section, condition);
            booleanPlaceholderCondition.parseValues();
            this.conditions.add(booleanPlaceholderCondition);
        }

        return super.parseValues();
    }

}
