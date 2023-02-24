package dev.rosewood.rosechat.message.tokenizer;

import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.message.RosePlayer;

public interface Tokenizer<T extends Token> {

    T tokenize(RoseMessage roseMessage, RosePlayer viewer, String input, boolean ignorePermissions);

    // TODO: Temporary return true, move to another class later
    /**
     * Checks if the sender of the given {@link RoseMessage} has the specified permission.
     * @param wrapper The {@link RoseMessage} to get the sender from.
     * @param ignorePermissions Whether permissions should be ignored.
     * @param permission The permission to check.
     * @return True if the sender has permission.
     */
    default boolean hasPermission(RoseMessage wrapper, boolean ignorePermissions, String permission) {
       /* if (ignorePermissions) return true;
        if (wrapper == null || wrapper.getSender() == null || wrapper.getLocation() == MessageLocation.NONE) return true;

        String groupPermission = wrapper.getGroup() == null ? "" : wrapper.getGroup().getLocationPermission();

        return wrapper.getSender().hasPermission(permission + "." + wrapper.getLocation().toString().toLowerCase() + "." + groupPermission)
                || wrapper.getSender().getIgnoredPermissions().contains(groupPermission.replace("rosechat.", ""))
                || wrapper.getSender().getIgnoredPermissions().contains("*");*/
        return true;
    }

    /**
     * Checks if the sender of the given {@link RoseMessage} has the specified permission.
     * Checks against the first permission, for example "rosechat.emojis", and an extended permission, for example "rosechat.emojis.smile".
     * @param wrapper The {@link RoseMessage} to get the sender from.
     * @param ignorePermissions Whether permissions should be ignored.
     * @param permission The permission to check.
     * @param extendedPermission The extended permission.
     * @return True if the sender has permission.
     */
    default boolean hasExtendedPermission(RoseMessage wrapper, boolean ignorePermissions, String permission, String extendedPermission) {
       /* if (ignorePermissions) return true;
        if (wrapper == null || wrapper.getSender() == null || wrapper.getLocation() == MessageLocation.NONE) return true;

        boolean hasParentPermission = hasPermission(wrapper, ignorePermissions, permission);
        boolean hasPermission = wrapper.getSender().hasPermission(extendedPermission) ||
                wrapper.getSender().getIgnoredPermissions().contains(extendedPermission.replace("rosechat.", ""))
                || wrapper.getSender().getIgnoredPermissions().contains("*");


        return hasParentPermission && hasPermission;*/
        return true;
    }

}
