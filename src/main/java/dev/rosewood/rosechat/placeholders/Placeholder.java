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
        String toParse = this.condition;

        switch (this.conditionType) {
            case PLACEHOLDER:
                return (PlaceholderAPI.setPlaceholders(player, toParse).equalsIgnoreCase("yes") ? this.conditionTrue : this.conditionFalse);
            case OTHER_PLACEHOLDER:
                return (PlaceholderAPI.setPlaceholders(viewer, toParse).equalsIgnoreCase("yes") ? this.conditionTrue : this.conditionFalse);
            case PLAYER_PERMISSION:
                return (player.hasPermission(this.condition) ? this.conditionTrue : this.conditionFalse);
            case OTHER_PERMISSION:
                return (viewer.hasPermission(this.condition) ? this.conditionTrue : this.conditionFalse);
        }

        return null;
    }

    public ConditionType getConditionType() {
        return this.conditionType;
    }

    public String getCondition() {
        return this.condition;
    }

    public List<String> getConditionTrue() {
        return this.conditionTrue;
    }

    public List<String> getConditionFalse() {
        return this.conditionFalse;
    }
}
