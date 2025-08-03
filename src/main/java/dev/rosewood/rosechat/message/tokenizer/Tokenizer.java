package dev.rosewood.rosechat.message.tokenizer;

import dev.rosewood.rosechat.message.PermissionArea;
import java.util.List;
import java.util.regex.Matcher;

public abstract class Tokenizer {

    private final String name;

    protected Tokenizer(String name) {
        this.name = name;
    }

    /**
     * Tokenizes the input and outputs a list of matches.
     *
     * @param params The {@link TokenizerParams} for this tokenization.
     * @return A List of {@link TokenizerResult} or null (or an empty list) if the input is invalid.
     */
    public abstract List<TokenizerResult> tokenize(TokenizerParams params);

    /**
     * @return The name of this tokenizer.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Checks if the sender of a message has the specified permission.
     * @param params The {@link TokenizerParams} to get information from, such as the sender and message location.
     * @param permission The permission to check, should not contain the location information. For example, 'rosechat.emojis'.
     * @return True if the sender has the permission
     */
    public boolean hasTokenPermission(TokenizerParams params, String permission) {
        // If the message doesn't exist, sent from the console, or has a location of 'NONE', then the sender should have permission.
        if (params == null || params.getSender() == null
                || params.getLocation() == PermissionArea.NONE || (params.getSender().isConsole())
                || !params.containsPlayerInput())
            return true;

        // Gets the full permission, e.g. rosechat.emoji.channel.global
        String fullPermission = permission + "." + params.getLocationPermission();

        return params.getSender().getIgnoredPermissions().contains(fullPermission.replace("rosechat.", ""))
                || params.getSender().getIgnoredPermissions().contains("*")
                || checkAndLogPermission(params, fullPermission);
    }

    /**
     * Checks if the sender of a message has the specified permission.
     * Checks against the first permission, for example, 'rosechat.emojis', and extended permissions such as 'rosechat.emoji.smile'.
     * @param params The {@link TokenizerParams} to get information from, such as the sender and message location.
     * @param permission The permission to check, should not contain the location information. For example, 'rosechat.emojis'
     * @param extendedPermission The extended permission, should not contain the location information. For example, 'rosechat.emoji.smile'.
     * @return True if the sender has permission.
     */
    public boolean hasExtendedTokenPermission(TokenizerParams params, String permission, String extendedPermission) {
        // If the message doesn't exist, sent from the console, or has a location of 'NONE', then the sender should have permission.
        if (params == null || params.getSender() == null
                || params.getLocation() == PermissionArea.NONE || (params.getSender().isConsole())
                || !params.containsPlayerInput())
            return true;

        // The sender will not have an extended permission if they do not have the base permission.
        if (!this.hasTokenPermission(params, permission))
            return false;

        return params.getSender().getIgnoredPermissions().contains(extendedPermission.replace("rosechat.", ""))
                || params.getSender().getIgnoredPermissions().contains("*")
                || checkAndLogPermission(params, extendedPermission);
    }

    private boolean checkAndLogPermission(TokenizerParams params, String permission) {
        boolean hasPermission = params.getSender().hasPermission(permission);
        if (!hasPermission)
            params.getOutputs().getMissingPermissions().add(permission);

        return hasPermission;
    }

    public String getCaptureGroup(Matcher matcher, String group) {
        try {
            return matcher.group(group);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return null;
        }
    }

    public Tokenizers.TokenizerBundle asBundle() {
        return new Tokenizers.TokenizerBundle(this.name, this);
    }

}
