package dev.rosewood.rosechat.placeholders;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import java.util.List;

public class Placeholder {

    private ConditionType conditionType;
    private String condition;
    private List<String> conditionTrue;
    private List<String> conditionFalse;

    public enum ConditionType {
        PLACEHOLDER,
        OTHER_PLACEHOLDER,
        PLAYER_PERMISSION,
        OTHER_PERMISSION
    }

    public List<String> getConditionResult(Player player, Player viewer) {
        String toParse = condition;

        switch (conditionType) {
            case PLACEHOLDER:
                return (PlaceholderAPI.setPlaceholders(player, toParse).equalsIgnoreCase("yes") ? conditionTrue : conditionFalse);
            case OTHER_PLACEHOLDER:
                return (PlaceholderAPI.setPlaceholders(viewer, toParse).equalsIgnoreCase("yes") ? conditionTrue : conditionFalse);
            case PLAYER_PERMISSION:
                return (player.hasPermission(condition) ? conditionTrue : conditionFalse);
            case OTHER_PERMISSION:
                return (viewer.hasPermission(condition) ? conditionTrue : conditionFalse);
        }

        return null;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public String getCondition() {
        return condition;
    }

    public List<String> getConditionTrue() {
        return conditionTrue;
    }

    public List<String> getConditionFalse() {
        return conditionFalse;
    }
}
