package dev.rosewood.rosechat.floralapi.root;

/**
 * FloralAPI's Base API/
 */
public interface RootPlugin {

    /**
     * Gets the version of the Root API.
     * @return The version of the Root API.
     */
    default String getRootVersion() {
        return "1.2.1";
    }
}
