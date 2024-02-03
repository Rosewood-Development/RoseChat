package dev.rosewood.rosechat.hook.nickname;

import org.bukkit.entity.Player;

public interface NicknameProvider {

    /**
     * Updates the nickname of a player.
     */
    void setNickname(Player player, String nickname);

}
