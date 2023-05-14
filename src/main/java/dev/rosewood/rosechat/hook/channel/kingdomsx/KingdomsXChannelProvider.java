package dev.rosewood.rosechat.hook.channel.kingdomsx;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import java.util.List;

public class KingdomsXChannelProvider extends ChannelProvider {

    @Override
    public List<Class<? extends Channel>> getChannels() {
        return null;
    }

    @Override
    public Class<? extends Channel> getChannelGenerator() {
        return KingdomsXChannel.class;
    }

    @Override
    public CommentedConfigurationSection getConfigurationSection() {
        return RoseChat.getInstance().getManager(ChannelManager.class).getChannelsConfig();
    }

    @Override
    public String getSupportedPlugin() {
        return "KingdomsX";
    }

}
