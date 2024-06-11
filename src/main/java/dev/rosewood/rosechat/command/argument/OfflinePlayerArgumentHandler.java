package dev.rosewood.rosechat.command.argument;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class OfflinePlayerArgumentHandler extends ArgumentHandler<String> {

    private final boolean withBungeePlayers;

    public OfflinePlayerArgumentHandler(boolean withBungeePlayers) {
        super(String.class);

        this.withBungeePlayers = withBungeePlayers;
    }

    @Override
    public String handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();

        if (input.trim().isEmpty()) {
            throw new ArgumentHandler.HandledArgumentException("argument-handler-string");
        } else {
            return input;
        }
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        List<String> suggestions = new ArrayList<>(Bukkit.getOnlinePlayers().stream()
                .filter(Predicate.not(MessageUtils::isPlayerVanished))
                .map(p -> ChatColor.stripColor(p.getDisplayName()))
                .toList());

        if (this.withBungeePlayers && RoseChatAPI.getInstance().isBungee()) {
            RoseChatAPI api = RoseChatAPI.getInstance();
            if (api.getBungeeManager().getBungeePlayers().containsKey("ALL")) {
                Collection<String> players = api.getBungeeManager().getBungeePlayers().get("ALL");
                for (String player : players) {
                    if (context.getSender().getName().equalsIgnoreCase(player))
                        continue;

                    suggestions.add(player);
                }
            }
        }

        return suggestions;
    }

}
