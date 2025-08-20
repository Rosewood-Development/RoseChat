package dev.rosewood.rosechat.chat.filter;

import dev.rosewood.rosechat.message.RosePlayer;
import java.util.ArrayList;
import java.util.List;

public record Filter(String id,
                     List<String> matches,
                     String prefix, String suffix,
                     List<String> inlineMatches, String inlinePrefix, String inlineSuffix,
                     String stop,
                     int sensitivity,
                     boolean useRegex,
                     boolean isEmoji,
                     boolean block,
                     String message,
                     String sound,
                     boolean canToggle,
                     boolean colorRetention,
                     boolean tagPlayers,
                     boolean matchLength,
                     boolean notifyStaff,
                     boolean addToSuggestions,
                     boolean escapable,
                     String bypassPermission,
                     String usePermission,
                     String hover,
                     String font,
                     String replacement,
                     String discordOutput,
                     List<String> serverCommands,
                     List<String> playerCommands) {

    public boolean hasPermission(RosePlayer rosePlayer) {
        if (this.bypassPermission == null && this.usePermission == null)
            return true;

        if (this.bypassPermission != null && !rosePlayer.hasPermission(this.bypassPermission))
            return true;

        return this.usePermission != null && rosePlayer.hasPermission(this.usePermission);
    }

    public Filter cloneAsTag() {
        String prefix = this.matches.getFirst();
        String suffix = prefix.charAt(0)  + "/" + prefix.substring(1);

        return new Filter(this.id + "-tag",
                new ArrayList<>(),
                prefix, suffix,
                this.inlineMatches,
                this.inlinePrefix, this.inlineSuffix,
                this.stop,
                this.sensitivity,
                this.useRegex, this.isEmoji,
                this.block,
                this.message, this.sound,
                this.canToggle, this.colorRetention,
                this.tagPlayers, this.matchLength,
                this.notifyStaff, this.addToSuggestions,
                this.escapable,
                this.bypassPermission, this.usePermission,
                this.hover, this.font,
                this.replacement + "%group_1%", this.discordOutput,
                this.serverCommands, this.playerCommands);
    }

}
