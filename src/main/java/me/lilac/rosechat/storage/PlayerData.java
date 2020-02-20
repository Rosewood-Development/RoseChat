package me.lilac.rosechat.storage;

import me.lilac.rosechat.Rosechat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerData {

    private static List<UUID> playersUsingStaffchat = new ArrayList<>();
    private static List<UUID> playersWithoutSounds = new ArrayList<>();
    private static List<UUID> playersUsingSocialSpy = new ArrayList<>();
    private static List<UUID> playersWithoutMessages = new ArrayList<>();
    private static List<UUID> mutedPlayers = new ArrayList<>();

    public static List<UUID> getPlayersUsingStaffchat() {
        return playersUsingStaffchat;
    }

    public static List<UUID> getPlayersWithoutSounds() {
        return playersWithoutSounds;
    }

    public static List<UUID> getPlayersUsingSocialSpy() {
        return playersUsingSocialSpy;
    }

    public static List<UUID> getPlayersWithoutMessages() {
        return playersWithoutMessages;
    }

    public static List<UUID> getMutedPlayers() {
        return mutedPlayers;
    }

    public static void save() {
        Rosechat.getInstance().getData().save();
    }
}
