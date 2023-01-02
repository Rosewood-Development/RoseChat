package dev.rosewood.rosechat.message.wrapper.tokenizer;

import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;

public interface Tokenizer<T extends Token> {

    T tokenize(MessageWrapper messageWrapper, RoseSender viewer, String input, boolean ignorePermissions);

    default boolean hasPermission(MessageWrapper wrapper, boolean ignorePermissions, String permission) {
        String groupPermission = wrapper == null ? "" : (wrapper.getGroup() == null ? "" : "." + wrapper.getGroup().getLocationPermission());
        return ignorePermissions
                || wrapper == null
                || wrapper.getLocation() == MessageLocation.NONE
                || (wrapper.getSender().hasPermission(permission + "." + wrapper.getLocation().toString() + groupPermission))
                || (wrapper.getSender().getIgnoredPermissions().contains(groupPermission.replace("rosechat.", "")))
                || (wrapper.getSender().getIgnoredPermissions().contains("rosechat.*"));
    }

    default boolean hasExtendedPermission(MessageWrapper wrapper, boolean ignorePermissions, String permission, String extendedPermission) {
        return ignorePermissions
                || (hasPermission(wrapper, ignorePermissions, permission) && (wrapper != null && wrapper.getSender() != null &&
                ((wrapper.getSender().hasPermission(extendedPermission))) || (wrapper.getSender().getIgnoredPermissions().contains(extendedPermission.replace("rosechat.", "")) || (wrapper.getSender().getIgnoredPermissions().contains("rosechat.*")))));
    }

    default boolean hasBasicPermission(MessageWrapper wrapper, boolean ignorePermissions, String permission) {
        return ignorePermissions
                || wrapper == null
                || wrapper.getLocation() == MessageLocation.NONE
                || (wrapper.getSender().hasPermission(permission));
    }

}
