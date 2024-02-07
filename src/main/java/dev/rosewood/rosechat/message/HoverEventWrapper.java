package dev.rosewood.rosechat.message;

import dev.rosewood.rosegarden.utils.NMSUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class HoverEventWrapper {

    public static HoverEvent of(HoverEvent.Action action, BaseComponent[] content) {
        if (NMSUtil.getVersionNumber() == 20) {
            return new HoverEvent(action, new Text(content));
        } else {
            return new HoverEvent(action, content);
        }
    }
}
