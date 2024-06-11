package dev.rosewood.rosechat.command.argument;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.command.framework.Argument;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.InputIterator;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class RosePlayerArgumentHandler extends ArgumentHandler<RosePlayer> {

    protected final boolean withBungeePlayers;

    public RosePlayerArgumentHandler(boolean withBungeePlayers) {
        super(RosePlayer.class);
        this.withBungeePlayers = withBungeePlayers;
    }

    @Override
    public RosePlayer handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();

        if (input.equalsIgnoreCase("console") && Bukkit.getPlayer("console") == null)
            return new RosePlayer(Bukkit.getConsoleSender());

        if (this.withBungeePlayers && RoseChatAPI.getInstance().isBungee()) {
            if (!RoseChatAPI.getInstance().getBungeeManager().getAllPlayers().contains(input))
                throw new HandledArgumentException("argument-handler-player", StringPlaceholders.of("input", input));
        }

        Player player = MessageUtils.getPlayerExact(input);
        if (player == null)
            throw new HandledArgumentException("argument-handler-player", StringPlaceholders.of("input", input));

        return new RosePlayer(player);
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
