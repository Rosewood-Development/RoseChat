package dev.rosewood.rosechat.placeholders.condition;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.ClickEvent;
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
    protected final Map<String, List<String>> conditionValues;
    protected  final Map<String, ClickEvent.Action> clickActions;

    /**
     * Creates a new placeholder condition.
     * These are used to change format based on the given condition.
     * @param section The {@link ConfigurationSection} to read from.
     * @param condition The pre-parsed condition.
     */
    public PlaceholderCondition(ConfigurationSection section, String condition) {
        this.section = section;
        this.condition = condition;
        this.conditionValues = new HashMap<>();
        this.clickActions = new HashMap<>();
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

    /**
     * @param sender The {@link RosePlayer} who sent the message.
     * @param viewer The {@link RosePlayer} who is viewing the message.
     * @param placeholders The {@link StringPlaceholders} to use.
     * @return The parsed condition as a {@link ClickEvent.Action}.
     */
    public ClickEvent.Action parseToAction(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        return null;
    }

    protected String parsePlaceholders(Player player, String placeholder, StringPlaceholders placeholders) {
        if (placeholder == null) return null;
        if (PlaceholderAPIHook.enabled()) return PlaceholderAPIHook.applyPlaceholders(player, placeholders.apply(placeholder));
        else return placeholders.apply(placeholder);
    }

    public PlaceholderCondition parseValues() {
        for (String sub : this.section.getKeys(false)) {
            if (sub.equals("condition")) continue;
            try {
                if (this.section.contains(sub + ".action")) this.clickActions.put(sub + ".action", ClickEvent.Action.valueOf(this.section.getString(sub + ".action")));
            } catch (IllegalArgumentException e) {
                LocaleManager localeManager = RoseChatAPI.getInstance().getLocaleManager();
                localeManager.sendCustomMessage(Bukkit.getConsoleSender(), localeManager.getLocaleMessage("prefix") +
                        "&eThe " + this.section.getString(sub + ".action") + " action is not a valid ClickEvent!");
            }

            if (this.section.contains(sub + ".value")) this.conditionValues.put(sub + ".value", Collections.singletonList(this.section.getString(sub + ".value")));
            this.conditionValues.put(sub, this.section.isList(sub) ? this.section.getStringList(sub) : Collections.singletonList(this.section.getString(sub)));
        }

        return this;
    }

    protected String combineConditionValues(String value) {
        List<String> result = this.conditionValues.get(this.clickActions.isEmpty() ? value : value + ".value");
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

    protected ClickEvent.Action getClickAction(String value) {
        return this.clickActions.get(value + ".action");
    }

    public String getCondition() {
        return this.condition;
    }

    public Map<String, List<String>> getConditionValues() {
        return this.conditionValues;
    }

    public Map<String, ClickEvent.Action> getClickActions() {
        return this.clickActions;
    }

}
