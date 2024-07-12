package dev.rosewood.rosechat.hook.channel.rosechat;

import dev.rosewood.rosechat.api.RoseChatAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface Spyable {

    /**
     * Retrieves a list of players who should receive spy messages.
     * @param condition A {@link Predicate<Player>} to test against, to see if the player should receive a spy message.
     * @return A {@link List<Player>} of players who should receive a spy message.
     */
    default List<Player> getSpies(Predicate<Player> condition) {
        List<UUID> channelSpies = RoseChatAPI.getInstance().getPlayerDataManager().getChannelSpies();
        List<Player> spies = new ArrayList<>();

        for (UUID uuid : channelSpies) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null)
                continue;

            if (condition == null || condition.test(player))
                spies.add(player);
        }

        return spies;
    }

}
