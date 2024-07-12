package dev.rosewood.rosechat.chat.log;

import java.util.ArrayList;
import java.util.List;

public class ConsoleMessageLog {

    protected final List<String> messages;

    public ConsoleMessageLog() {
        this.messages = new ArrayList<>();
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    public String getLastMessage() {
        if (this.messages.isEmpty())
            return null;

        return this.messages.get(this.messages.size() - 1);
    }

    public void removeLastMessage() {
        if (this.messages.isEmpty())
            return;

        this.messages.remove(this.messages.size() - 1);
    }

    public List<String> getMessages() {
        return this.messages;
    }

    // TODO Save to file

}
