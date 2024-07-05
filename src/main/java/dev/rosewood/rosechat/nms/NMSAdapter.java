package dev.rosewood.rosechat.nms;

import dev.rosewood.rosegarden.utils.NMSUtil;
import org.bukkit.Bukkit;

public class NMSAdapter {

    private static NMSHandler nmsHandler;
    private static boolean isPaper;

    static {
        try {
            int major = NMSUtil.getVersionNumber();
            int minor = NMSUtil.getMinorVersionNumber();

            String name = Bukkit.getServer().getClass().getPackage().getName();
            isPaper = !name.contains("R"); // Used for checking if we should use obfuscation on modern versions.

            if (major == 20 && minor >= 5)
                name = "v1_20_R4";
            else if (major >= 21)
                name = "v1_21_R1";
            else if (major == 16)
                name = "v1_16_R1";
            else
                name = "v1_20_R3"; // Versions 1.17 - 1.20.4 are the same.

            nmsHandler = (NMSHandler) Class.forName("dev.rosewood.rosechat.nms." + name).getConstructor().newInstance();
        } catch (Exception ignored) {}
    }

    public static boolean isValidVersion() {
        return nmsHandler != null;
    }

    public static boolean isPaper() {
        return isPaper;
    }

    public static NMSHandler getHandler() {
        return nmsHandler;
    }

}
