package dev.rosewood.rosechat.groups;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.floralapi.root.storage.YMLFile;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GroupManager {

    private YMLFile data;
    private Map<String, Group> groups;

    public GroupManager() {
        data = RoseChat.getInstance().getDataFile();
        groups = new HashMap<>();
        load();
    }

    public void load() {
        for (String id : data.getSection("groups").getKeys(false)) {
            String path = "groups." + id;
            UUID owner = UUID.fromString(data.getString(path + ".owner"));
            String name = data.getString(path + ".name");

            List<UUID> players = new ArrayList<>();
            for (String uuidStr : data.getStringList(path + ".players")) players.add(UUID.fromString(uuidStr));

            Group group = new Group(owner, name, id);

            List<Mail> mailList = new ArrayList<>();
            for (String mailID : data.getSection(path + ".mail").getKeys(false)) {
                String text = ChatColor.translateAlternateColorCodes('&',
                        data.getString(path + ".mail." + mailID + ".text"));
                UUID sender = UUID.fromString(data.getString(path + ".mail." + mailID + ".sender"));

                List<UUID> unread = new ArrayList<>();
                for (String uuidStr : data.getStringList(path + ".mail." + mailID + ".unread"))
                    unread.add(UUID.fromString(uuidStr));

                Mail mail = new Mail(group, sender, text);
                mail.setUnreadPlayers(unread);
            }

            group.setPlayers(players);
            group.setMail(mailList);
        }
    }

    public void save() {
        if (groups.isEmpty()) return;

        for (String id : groups.keySet()) {
            Group group = groups.get(id);
            String path = "groups." + group.getId();
            List<String> players = new ArrayList<>();

            for (UUID uuid : group.getPlayers()) players.add(uuid.toString());

            data.set(path + ".owner", group.getOwner().toString());
            data.set(path + ".name", group.getName().replace(ChatColor.COLOR_CHAR, '&'));
            data.set(path + ".players", players);

            int index = 1;
            for (Mail mail : group.getMail()) {
                List<String> unread = new ArrayList<>();
                for (UUID uuid : mail.getUnreadPlayers()) unread.add(uuid.toString());

                data.set(path + ".mail." + index + ".text", mail.getMessage().replace(ChatColor.COLOR_CHAR, '&'));
                data.set(path + ".mail." + index + ".sender", mail.getSender().toString());
                data.set(path + ".mail." + index + ".unread", unread);

                index++;
            }
        }

        data.save();
    }

    public Group getGroup(String id) {
        return groups.get(id);
    }

    public Map<String, Group> getGroups() {
        return groups;
    }
}
