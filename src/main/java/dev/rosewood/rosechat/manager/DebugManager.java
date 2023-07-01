package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.LinkedList;

public class DebugManager extends Manager {

    private boolean enabled;
    private boolean writeToFile;
    private boolean timerEnabled;
    private boolean doOnce;
    private final LinkedList<String> messages;

    public DebugManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.messages = new LinkedList<>();
    }

    public void addMessage(Supplier<String> message) {
        if (!this.enabled || !this.writeToFile)
            return;

        String prefix = "[" + System.currentTimeMillis() + "]";
        this.messages.add(prefix + " " + message.get() + "\n");
    }

    @Override
    public void reload() {

    }

    @Override
    public void disable() {

    }

    public void save() {
        if (!this.enabled || !this.writeToFile)
            return;

        Bukkit.getScheduler().runTaskAsynchronously(RoseChat.getInstance(), () -> {
            long time = System.currentTimeMillis();
            File debugFolder = new File(this.rosePlugin.getDataFolder(), "debug");
            debugFolder.mkdirs();
            File debugFile = new File(this.rosePlugin.getDataFolder() + "/debug", "debug-" + time + ".log");

            try {
                if (!debugFile.createNewFile()) return;

                Writer writer = Files.newBufferedWriter(Paths.get(debugFile.getAbsolutePath()));
                for (String message : this.messages) {
                    if (message != null) writer.write(message);
                }

                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.messages.clear();
        });
    }

    public LinkedList<String> getMessages() {
        return this.messages;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setWriteToFile(boolean writeToFile) {
        this.writeToFile = writeToFile;
    }

    public void setTimerEnabled(boolean timerEnabled) {
        this.timerEnabled = timerEnabled;
    }

    public boolean isTimerEnabled() {
        return this.timerEnabled;
    }

    public void setDoOnce(boolean doOnce) {
        this.doOnce = doOnce;
    }

    public boolean doOnce() {
        return this.doOnce;
    }

}
