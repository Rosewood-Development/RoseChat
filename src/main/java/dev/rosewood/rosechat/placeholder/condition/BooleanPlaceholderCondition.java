package dev.rosewood.rosechat.placeholder.condition;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * A placeholder condition for boolean values.
 */
public class BooleanPlaceholderCondition extends PlaceholderCondition {

    private Operator operator;
    private String left;
    private String right;

    public BooleanPlaceholderCondition(ConfigurationSection section, String condition) {
        super(section, condition);
    }

    @Override
    public String parse(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        return String.valueOf(this.parseToBoolean(sender, viewer, placeholders));
    }

    @Override
    public boolean parseToBoolean(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        Player singlePlayer = this.condition.startsWith("%other_") ? viewer.asPlayer() : sender.asPlayer();
        String singleCondition = this.condition.replace("other_", "");
        String singleParsed = this.parsePlaceholders(singlePlayer, viewer == null ? null : viewer.asPlayer(), singleCondition, placeholders);
        Player leftPlayer = this.left == null ? null : this.left.startsWith("%other_") ? viewer.asPlayer() : sender.asPlayer();
        Player rightPlayer = this.right == null ? null : this.right.startsWith("%other_") ? viewer.asPlayer() : sender.asPlayer();
        String leftCondition = this.left == null ? null : this.left.replace("other_", "");
        String rightCondition = this.right == null ? null : this.right.replace("other_", "");
        String leftParsed = this.left == null ? null : this.parsePlaceholders(leftPlayer, viewer == null ? null : viewer.asPlayer(), leftCondition, placeholders);
        String rightParsed = this.right == null ? null : this.parsePlaceholders(rightPlayer, viewer == null ? null : viewer.asPlayer(), rightCondition, placeholders);

        boolean result;
        if (this.operator != null) {
            result = this.operator.evaluate(ChatColor.stripColor(leftParsed), ChatColor.stripColor(rightParsed));
        } else {
            result = singleParsed.equalsIgnoreCase("yes") || singleParsed.equals("true");
        }

        return result;
    }

    @Override
    public PlaceholderCondition parseValues() {
        char placeholderSymbol = '%';
        outer:
        for (Operator operator : Operator.values()) {
            String symbol = operator.getSymbol();
            boolean inPlaceholder = false;
            StringBuilder buffer = new StringBuilder();
            for (char c : this.condition.toCharArray()) {
                if (c == placeholderSymbol)
                    inPlaceholder = !inPlaceholder;

                buffer.append(c);
                if (!inPlaceholder && buffer.toString().endsWith(symbol)) {
                    this.left = buffer.substring(0, buffer.length() - symbol.length()).trim();
                    this.operator = operator;
                    this.right = this.condition.substring(this.left.length() + symbol.length()).trim();
                    break outer;
                }
            }
        }

        super.parseValues();
        return this;
    }

}
