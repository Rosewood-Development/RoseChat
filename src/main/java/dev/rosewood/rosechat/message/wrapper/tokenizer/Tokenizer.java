package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;

public interface Tokenizer<T extends Token> {

    T tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions);

    /**
     * Checks if the sender of the given {@link MessageWrapper} has the specified permission.
     * @param wrapper The {@link MessageWrapper} to get the sender from.
     * @param ignorePermissions Whether permissions should be ignored.
     * @param permission The permission to check.
     * @return True if the sender has permission.
     */
    default boolean hasPermission(MessageWrapper wrapper, boolean ignorePermissions, String permission) {
        if (ignorePermissions) return true;
        if (wrapper == null || wrapper.getSender() == null || wrapper.getLocation() == MessageLocation.NONE) return true;

        String groupPermission = wrapper.getGroup() == null ? "" : wrapper.getGroup().getLocationPermission();

        return wrapper.getSender().hasPermission(permission + "." + wrapper.getLocation().toString().toLowerCase() + "." + groupPermission)
                || wrapper.getSender().getIgnoredPermissions().contains(groupPermission.replace("rosechat.", ""))
                || wrapper.getSender().getIgnoredPermissions().contains("*");
    }

    /**
     * Checks if the sender of the given {@link MessageWrapper} has the specified permission.
     * Checks against the first permission, for example "rosechat.emojis", and an extended permission, for example "rosechat.emojis.smile".
     * @param wrapper The {@link MessageWrapper} to get the sender from.
     * @param ignorePermissions Whether permissions should be ignored.
     * @param permission The permission to check.
     * @param extendedPermission The extended permission.
     * @return True if the sender has permission.
     */
    default boolean hasExtendedPermission(MessageWrapper wrapper, boolean ignorePermissions, String permission, String extendedPermission) {
        if (ignorePermissions) return true;
        if (wrapper == null || wrapper.getSender() == null || wrapper.getLocation() == MessageLocation.NONE) return true;

        boolean hasParentPermission = hasPermission(wrapper, ignorePermissions, permission);
        boolean hasPermission = wrapper.getSender().hasPermission(extendedPermission) ||
                wrapper.getSender().getIgnoredPermissions().contains(extendedPermission.replace("rosechat.", ""))
                || wrapper.getSender().getIgnoredPermissions().contains("*");


        return hasParentPermission && hasPermission;
    }

}
