package dev.rosewood.rosechat.placeholders.condition;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceholderCondition {

    protected final ConfigurationSection section;
    protected final String condition;
    protected final Map<String, List<String>> values;
    protected ClickEvent.Action clickAction;
    protected HoverEvent.Action hoverAction;

    /**
     * Creates a new placeholder condition.
     * These are used to change format based on the given condition.
     * @param section The {@link ConfigurationSection} to read from.
     * @param condition The pre-parsed condition.
     */
    public PlaceholderCondition(ConfigurationSection section, String condition) {
        this.section = section;
        this.condition = condition;
        this.values = new HashMap<>();
    }

    /**
     * Parses the given condition into a string, using the given sender, viewer, and placeholders.
     * @param sender The {@link RosePlayer} who sent the message.
     * @param viewer The {@link RosePlayer} who is viewing the message.
     * @param placeholders The {@link StringPlaceholders} to use.
     * @return The message after parsing has occurred.
     */
    protected String parse(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        return null;
    }

    /**
     * @param sender The {@link RosePlayer} who sent the message.
     * @param viewer The {@link RosePlayer} who is viewing the message.
     * @param placeholders The {@link StringPlaceholders} to use.
     * @return The parsed condition as a string.
     */
    public String parseToString(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        return null;
    }

    /**
     * @param sender The {@link RosePlayer} who sent the message.
     * @param viewer The {@link RosePlayer} who is viewing the message.
     * @param placeholders The {@link StringPlaceholders} to use.
     * @return The parsed condition as a boolean.
     */
    public boolean parseToBoolean(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        return false;
    }

    protected String parsePlaceholders(Player player, Player player2, String placeholder, StringPlaceholders placeholders) {
        if (placeholder == null) return null;
        if (PlaceholderAPIHook.enabled()) {
            placeholder = placeholders.apply(placeholder);
            placeholder = PlaceholderAPIHook.applyRelationalPlaceholders(player, player2, placeholder);
            return PlaceholderAPIHook.applyPlaceholders(player, placeholder);
        }
        else return placeholders.apply(placeholder);
    }

    public PlaceholderCondition parseValues() {
        for (String valueId : this.section.getKeys(false)) {
            if (valueId.equalsIgnoreCase("condition")) continue;

            try {
                if (valueId.equalsIgnoreCase("action")) {
                    if (this.section.getName().equalsIgnoreCase("hover")) {
                        this.hoverAction = HoverEvent.Action.valueOf(this.section.getString(valueId));
                    } else if (this.section.getName().equalsIgnoreCase("click")) {
                        this.clickAction = ClickEvent.Action.valueOf(this.section.getString(valueId));
                    }

                    continue;
                }
            } catch (IllegalArgumentException e) {
                LocaleManager localeManager = RoseChatAPI.getInstance().getLocaleManager();
                localeManager.sendCustomMessage(Bukkit.getConsoleSender(), localeManager.getLocaleMessage("prefix") +
                        "&eThe " + this.section.getString(valueId) + " action is not a valid Click or Hover Event!");
            }

            if (valueId.equalsIgnoreCase("hover") && this.hoverAction == null)
                this.hoverAction = HoverEvent.Action.SHOW_TEXT;

            if (valueId.equalsIgnoreCase("click") && this.clickAction == null)
                this.clickAction = ClickEvent.Action.SUGGEST_COMMAND;

            List<String> value = this.section.isList(valueId) ?
                    this.section.getStringList(valueId) : Collections.singletonList(this.section.getString(valueId));
            this.values.put(valueId, value);
        }

        return this;
    }

    protected String combineConditionValues(String value) {
        List<String> result = this.values.get(value);
        if (result == null || result.isEmpty()) return "";

        StringBuilder resultBuilder = new StringBuilder();
        int index = 0;
        for (String s : result) {
            if (index != 0) resultBuilder.append("\n");
            resultBuilder.append(s);
            index++;
        }

        return resultBuilder.toString();
    }

    public HoverEvent.Action getHoverAction() {
        return this.hoverAction;
    }

    public ClickEvent.Action getClickAction() {
        return this.clickAction;
    }

    public String getCondition() {
        return this.condition;
    }

    public Map<String, List<String>> getValues() {
        return this.values;
    }

}
