package dev.rosewood.rosechat.chat.task;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.log.ConsoleMessageLog;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatLogTask extends BukkitRunnable {

    private final ConsoleMessageLog log;
    private final File file;

    public ChatLogTask(RoseChat plugin, ConsoleMessageLog log) throws IOException {
        this.log = log;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String date = sdf.format(new Date());
        Files.createDirectories(Path.of(plugin.getDataFolder() + "/log/"));
        this.file = Files.createFile(Path.of(plugin.getDataFolder() + "/log/" + date + ".log")).toFile();
        this.runTaskTimerAsynchronously(plugin, 0L, 30L * 20L);
    }

    @Override
    public void run() {
        try {
            FileWriter writer = new FileWriter(this.file, true);
            for (String s : this.log.getMessages())
                writer.write(s + "\n");
            writer.close();
            this.log.getMessages().clear();
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.getLogger().warning("An error occurred while writing the chat log.");
        }
    }

    // Run once to save anything left over.
    public void save() {
        this.run();
        this.cancel();
    }

}
