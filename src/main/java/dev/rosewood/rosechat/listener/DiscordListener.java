package dev.rosewood.rosechat.listener;

import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import org.bukkit.event.Listener;

public class DiscordListener implements Listener {

    @Subscribe
    public void onDiscordMessageProcess(DiscordGuildMessagePostProcessEvent event) {
    }
}
