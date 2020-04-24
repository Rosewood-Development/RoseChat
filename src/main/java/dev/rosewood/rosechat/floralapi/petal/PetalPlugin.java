package dev.rosewood.rosechat.floralapi.petal;

/**
 * FloralAPI's User Interface API.
 */
public interface PetalPlugin {

    /**
     * Gets the version of the Petal API.
     * @return The version of the Petal API.
     */
    default String getPetalVersion() {
        return "1.1.3";
    }
}
