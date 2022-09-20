package dev.rosewood.rosechat.placeholders.condition;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.message.RoseSender;
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

    public PlaceholderCondition(ConfigurationSection section, String condition) {
        this.section = section;
        this.condition = condition;
        this.conditionValues = new HashMap<>();
        this.clickActions = new HashMap<>();
    }

    protected String parse(RoseSender sender, RoseSender viewer, StringPlaceholders placeholders) {
        return null;
    }

    public String parseToString(RoseSender sender, RoseSender viewer, StringPlaceholders placeholders) {
        return null;
    }

    public boolean parseToBoolean(RoseSender sender, RoseSender viewer, StringPlaceholders placeholders) {
        return false;
    }

    public ClickEvent.Action parseToAction(RoseSender sender, RoseSender viewer, StringPlaceholders placeholders) {
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
