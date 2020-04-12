package dev.rosewood.rosechat.floralapi.root.utils;

import dev.rosewood.rosechat.floralapi.root.storage.YMLFile;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Basic functions within a utility class.
 */
public class Utils {

    /**
     * Serializes a location to a String.
     * @param location The location to serialize
     * @param direction Whether or not to use direction.
     * @return The serailized String.
     */
    public static String serializeLocation(Location location, boolean direction) {
        String loc = location.getWorld().getName() + ", " + location.getBlockX() + ", "
                + location.getBlockY() + ", " + location.getBlockZ();
        if (direction) loc += ", " + location.getYaw() + ", " + location.getPitch();
        return loc;
    }

    /**
     * Saves a serialized a location to a YMLFile.
     * @param file The file to save to.
     * @param path The path to save to.
     * @param location The location to save.
     * @param direction Whether or not to use direction.
     */
    public static void serializeLocation(YMLFile file, String path, Location location, boolean direction) {
        file.set(path + ".world", location.getWorld().getName());
        file.set(path + ".x", location.getBlockX());
        file.set(path + ".y", location.getBlockY());
        file.set(path + ".z", location.getBlockZ());

        if (direction) {
            file.set(path + ".yaw", location.getYaw());
            file.set(path + ".pitch", location.getPitch());
        }
    }

    /**
     * Deserializes a String to a Location.
     * @param string The String to deserialize.
     * @param direction Whether or not to use direction.
     * @return The deserialized Location.
     */
    public static Location deserializeLocation(String string, boolean direction) {
        String[] locationSplit = string.split(", ");
        String world = locationSplit[0];
        int x = Integer.parseInt(locationSplit[1]);
        int y = Integer.parseInt(locationSplit[2]);
        int z = Integer.parseInt(locationSplit[3]);

        if (direction) {
            float yaw = Float.parseFloat(locationSplit[4]);
            float pitch = Float.parseFloat(locationSplit[5]);
            return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
        }

        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    /**
     * Loads and deserializes a Location from a YMLFile.
     * @param file The file to load from.
     * @param path The path to load from.
     * @param direction Whether or not to use direction.
     * @return The loaded Location.
     */
    public static Location deserializeLocation(YMLFile file, String path, boolean direction) {
        String world = file.getString(path + ".world");
        int x = file.getInt(path + ".x");
        int y = file.getInt(path + ".y");
        int z = file.getInt(path + ".z");

        if (direction) {
            float yaw = file.getFloat(path + ".yaw");
            float pitch = file.getFloat(path + ".pitch");
            return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
        }

        return new Location(Bukkit.getWorld(world), x, y, z);
    }


}
