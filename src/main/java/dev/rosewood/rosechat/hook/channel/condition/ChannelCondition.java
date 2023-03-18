package dev.rosewood.rosechat.hook.channel.condition;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.placeholders.condition.Operator;
import dev.rosewood.rosechat.placeholders.condition.PlaceholderCondition;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class ChannelCondition extends PlaceholderCondition {

    private Operator operator;
    private String left;
    private String right;

    public ChannelCondition(ConfigurationSection section, String condition) {
        super(section, condition);
    }

    @Override
    public boolean parseToBoolean(RosePlayer sender, RosePlayer viewer, StringPlaceholders placeholders) {
        Player singlePlayer = this.condition.startsWith("%receiver_") ? viewer.asPlayer() : sender.asPlayer();
        String singleCondition = this.condition.replace("receiver_", "").replace("sender_", "");
        String singleParsed = this.parsePlaceholders(singlePlayer, singleCondition, placeholders);
        Player leftPlayer = this.left == null ? null : (this.left.startsWith("%receiver_") ? viewer.asPlayer() : sender.asPlayer());
        Player rightPlayer = this.right == null ? null : (this.right.startsWith("%receiver_") ? viewer.asPlayer() : sender.asPlayer());
        String leftCondition = this.left == null ? null : this.left.replace("receiver_", "").replace("sender_", "");
        String rightCondition = this.right == null ? null : this.right.replace("receiver_", "").replace("sender_", "");
        String leftParsed = this.left == null ? null : this.parsePlaceholders(leftPlayer, leftCondition, placeholders);
        String rightParsed = this.right == null ? null : this.parsePlaceholders(rightPlayer, rightCondition, placeholders);

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

        return this;
    }

}
