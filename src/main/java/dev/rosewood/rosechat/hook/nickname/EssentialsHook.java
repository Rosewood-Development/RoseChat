package dev.rosewood.rosechat.hook.nickname;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EssentialsHook implements NicknameProvider {

    private final Essentials essentials;

    public EssentialsHook() {
        this.essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
    }

    @Override
    public void setNickname(Player player, String nickname) {
        this.essentials.getUser(player).setNickname(nickname);
    }

}
