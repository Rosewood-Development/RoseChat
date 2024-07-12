package dev.rosewood.rosechat.chat.task;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.chat.channel.ChannelMessageOptions;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class SlowmodeTask extends BukkitRunnable {

    private final Channel channel;
    private final BukkitTask task;

    public SlowmodeTask(Channel channel, int time) {
        // Allow channels to have an infinite slow mode where messages can't be sent.
        // This allows messages to still be logged for use later.
        if (time > 9999) {
            this.channel = channel;
            this.task = null;
            return;
        }

        this.channel = channel;
        this.task = this.runTaskTimerAsynchronously(RoseChat.getInstance(), 0L, time * 20L);
    }

    @Override
    public void run() {
        ChannelMessageOptions options = this.channel.getMessageLog().getAndRemoveNextMessage();
        if (options == null)
            return;

        options = new ChannelMessageOptions.Builder()
                .sender(options.sender())
                .message(options.message())
                .format(options.format())
                .sendToDiscord(options.sendToDiscord())
                .discordId(options.discordId())
                .messageId(options.messageId())
                .isJson(options.isJson())
                .wrapper(options.wrapper())
                .bypassSlowmode(true)
                .build();
        this.channel.send(options);
    }

    public void stop() {
        if (this.task != null)
            this.task.cancel();
    }

}
