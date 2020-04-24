package dev.rosewood.rosechat.groups;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class Group {

    private List<UUID> players;
    private UUID owner;
    private String id;
    private String name;
    private boolean isMuted;
    private List<Mail> mail;

    public Group(UUID owner, String name) {
        this(owner, name, name.toLowerCase().replace(" ", "_"));
    }

    public Group(UUID owner, String name, String id) {
        this.id = id;
        this.owner = owner;
        this.name = name;
    }

    public void createMail(Mail mail) {
        this.mail.add(mail);
    }

    public void addPlayer(Player player) {
        addPlayer(player.getUniqueId());
    }

    public void addPlayer(UUID uuid) {
        players.add(uuid);
    }

    public void removePlayer(Player player) {
        removePlayer(player.getUniqueId());
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

    public void setPlayers(List<UUID> players) {
        this.players = players;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public void setMail(List<Mail> mail) {
        this.mail = mail;
    }

    public List<Mail> getMail() {
        return mail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
