package dev.rosewood.rosechat.placeholders;

import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class ConditionalPlaceholder {

    private String left, right;
    private Operator operator;
    private ConditionType conditionType;
    private double numberValue;
    private String condition;
    private Map<String, List<String>> conditionValues;
    private Map<String, ClickEvent.Action> clickActions;

    public ConditionalPlaceholder() {
        conditionValues = new HashMap<>();
        clickActions = new HashMap<>();
    }

    public enum ConditionType {
        BOOLEAN,
        NUMBER,
        STRING,
        NONE
    }

    public String parse(RoseSender sender, RoseSender viewer, StringPlaceholders placeholders) {
        Player leftPlayer = this.left == null ? sender.asPlayer() : this.left.startsWith("%other_") ? (viewer.isPlayer() ? viewer.asPlayer() : null) : (sender.isPlayer() ? sender.asPlayer() : null);
        Player rightPlayer = this.right == null ? null : this.right.startsWith("%other_") ? (viewer.isPlayer() ? viewer.asPlayer() : null) : (sender.isPlayer() ? sender.asPlayer() : null);
        Player conditionPlayer = this.condition == null ? null : this.condition.startsWith("%other_") ? (viewer.isPlayer() ? viewer.asPlayer() : null) : (sender.isPlayer() ? sender.asPlayer() : null);
        String leftPlaceholder = this.left == null ? null : this.left.replace("other_", "");
        String rightPlaceholder = this.right == null ? null : this.right.replace("other_", "");
        String conditionPlaceholder = this.condition == null ? null : this.condition.replace("other_", "");

        leftPlaceholder = leftPlaceholder == null ? null : this.parsePlaceholders(leftPlayer, leftPlaceholder, placeholders);
        rightPlaceholder = rightPlaceholder == null ? null : this.parsePlaceholders(rightPlayer, rightPlaceholder, placeholders);
        conditionPlaceholder = conditionPlaceholder == null ? null : this.parsePlaceholders(conditionPlayer, conditionPlaceholder, placeholders);

        String resultId = null;
        switch (this.conditionType) {
            case BOOLEAN:
                if (this.operator != null) {
                    resultId = String.valueOf(this.operator.evaluate(leftPlaceholder, rightPlaceholder));
                } else {
                    resultId = String.valueOf(conditionPlaceholder.equalsIgnoreCase("yes") || conditionPlaceholder.equalsIgnoreCase("true"));
                }
                break;
            case NUMBER:
                try {
                    double num = Double.parseDouble(conditionPlaceholder);
                    if (num > this.numberValue) resultId = "more";
                    if (num == this.numberValue) resultId = "equal";
                    if (num < this.numberValue) resultId = "less";
                } catch (NumberFormatException e) {
                    resultId = "default";
                }
                break;
            case NONE:
                resultId = "default";
                break;
            default:
                resultId = conditionPlaceholder;
                break;
        }

        List<String> resultList = this.conditionValues.containsKey(resultId) ? this.conditionValues.get(resultId) : this.conditionValues.get("default");
        if (resultList == null) {
            return "";
        }

        StringBuilder resultBuilder = new StringBuilder();
        int index = 0;
        for (String s : resultList) {
            if (index != 0) resultBuilder.append("\n");
            resultBuilder.append(s);
            index++;
        }

        return resultBuilder.toString();
    }

    public ClickEvent.Action parseToAction(RoseSender sender, RoseSender viewer, StringPlaceholders placeholders) {
        Player leftPlayer = this.left == null ? null : this.left.startsWith("%other_") ? (viewer.isPlayer() ? viewer.asPlayer() : null) : (sender.isPlayer() ? sender.asPlayer() : null);
        Player rightPlayer = this.right == null ? null : this.right.startsWith("%other_") ? (viewer.isPlayer() ? viewer.asPlayer() : null) : (sender.isPlayer() ? sender.asPlayer() : null);
        Player conditionPlayer = this.condition == null ? null : this.condition.startsWith("%other_") ? (viewer.isPlayer() ? viewer.asPlayer() : null) : (sender.isPlayer() ? sender.asPlayer() : null);
        String leftPlaceholder = this.left == null ? null : this.left.replace("other_", "");
        String rightPlaceholder = this.right == null ? null : this.right.replace("other_", "");
        String conditionPlaceholder = this.condition == null ? null : this.condition.replace("other_", "");

        leftPlaceholder = leftPlaceholder == null ? null : this.parsePlaceholders(leftPlayer, leftPlaceholder, placeholders);
        rightPlaceholder = rightPlaceholder == null ? null : this.parsePlaceholders(rightPlayer, rightPlaceholder, placeholders);
        conditionPlaceholder = conditionPlaceholder == null ? null : this.parsePlaceholders(conditionPlayer, conditionPlaceholder, placeholders);

        String resultId = null;
        switch (this.conditionType) {
            case BOOLEAN:
                if (this.operator != null) {
                    resultId = String.valueOf(this.operator.evaluate(leftPlaceholder, rightPlaceholder));
                } else {
                    resultId = String.valueOf(conditionPlaceholder.equalsIgnoreCase("yes") || conditionPlaceholder.equalsIgnoreCase("true"));
                }
                break;
            case NUMBER:
                try {
                    double num = Double.parseDouble(conditionPlaceholder);
                    if (num > this.numberValue) resultId = "more";
                    if (num == this.numberValue) resultId = "equal";
                    if (num < this.numberValue) resultId = "less";
                } catch (NumberFormatException e) {
                    resultId = "default";
                }
                break;
            case NONE:
                resultId = "default";
                break;
            default:
                resultId = conditionPlaceholder;
                break;
        }

        return this.clickActions.containsKey(resultId) ? this.clickActions.get(resultId) : this.clickActions.get("default");
    }

    private String parsePlaceholders(Player player, String placeholder, StringPlaceholders placeholders) {
        if (placeholder == null) return null;
        if (PlaceholderAPIHook.enabled()) return PlaceholderAPIHook.applyPlaceholders(player, placeholders.apply(placeholder));
        else return placeholders.apply(placeholder);
    }

    // Ty Nicole <3
    public void parseCondition(ConfigurationSection parentSection, String condition) {
        this.condition = condition;

        if (condition == null || condition.trim().isEmpty()) {
            this.conditionType = ConditionType.NONE;
            loadValues(parentSection);
            return;
        }

        char placeholderSymbol = '%';
        outer:
        for (Operator operator : Operator.values()) {
            String symbol = operator.getSymbol();
            boolean inPlaceholder = false;
            StringBuilder buffer = new StringBuilder();
            for (char c : condition.toCharArray()) {
                if (c == placeholderSymbol)
                    inPlaceholder = !inPlaceholder;

                buffer.append(c);
                if (!inPlaceholder && buffer.toString().endsWith(symbol)) {
                    this.left = buffer.substring(0, buffer.length() - symbol.length()).trim();
                    this.operator = operator;
                    this.right = condition.substring(this.left.length() + symbol.length()).trim();
                    break outer;
                }
            }
        }

        if (this.left != null && this.right != null && this.operator != null) this.conditionType = ConditionType.BOOLEAN;
        else {
            if (parentSection.contains("value")) {
                this.conditionType = ConditionType.NUMBER;
                this.numberValue = parentSection.getDouble("value");
            }

            if (parentSection.contains("true") || parentSection.contains("false")) this.conditionType = ConditionType.BOOLEAN;
            else this.conditionType = ConditionType.STRING;
        }

        loadValues(parentSection);
    }

    private void loadValues(ConfigurationSection parentSection) {
        for (String section : parentSection.getKeys(false)) {
            if (section.equalsIgnoreCase("condition") || section.equalsIgnoreCase("condition-type") || section.equalsIgnoreCase("value")) continue;
            if (parentSection.isConfigurationSection(section)) {
                if (parentSection.contains(section + ".action")) this.clickActions.put(section, ClickEvent.Action.valueOf(parentSection.getString(section + ".action")));
                if (parentSection.contains(section + ".value")) this.conditionValues.put(section, Collections.singletonList(parentSection.getString(section + ".value")));
            } else {
                this.conditionValues.put(section, parentSection.isList(section) ? parentSection.getStringList(section) : Collections.singletonList(parentSection.getString(section)));
            }
        }
    }

    private enum Operator {
        NOT_EQUALS("!=", (left, right) -> !left.equalsIgnoreCase(right)),
        LESS_THAN_OR_EQUALS("<=", (left, right) -> Double.parseDouble(left) <= Double.parseDouble(right)),
        GREATER_THAN_OR_EQUALS(">=", (left, right) -> Double.parseDouble(left) >= Double.parseDouble(right)),

        EQUALS("=", String::equalsIgnoreCase),
        LESS_THAN("<", (left, right) -> Double.parseDouble(left) < Double.parseDouble(right)),
        GREATER_THAN(">", (left, right) -> Double.parseDouble(left) > Double.parseDouble(right)),
        CONTAINS("^", (left, right) -> left.toLowerCase().contains(right.toLowerCase()));

        private final String symbol;
        private final BiFunction<String, String, Boolean> operation;

        Operator(String symbol, BiFunction<String, String, Boolean> operation) {
            this.symbol = symbol;
            this.operation = operation;
        }

        public String getSymbol() {
            return this.symbol;
        }

        public boolean evaluate(String left, String right) {
            try {
                return this.operation.apply(left, right);
            } catch (Exception e) {
                return false;
            }
        }
    }

    public double getNumberValue() {
        return this.numberValue;
    }

    public ConditionType getConditionType() {
        return this.conditionType;
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
