package dev.rosewood.rosechat.hook.channel.simpleclans;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import java.util.List;

public class SimpleClansChannelProvider extends ChannelProvider {

    @Override
    public List<Class<? extends Channel>> getChannels() {
        return null;
    }

    @Override
    public Class<? extends Channel> getChannelGenerator() {
        return SimpleClansChannel.class;
    }

    @Override
    public CommentedConfigurationSection getConfigurationSection() {
        return RoseChat.getInstance().getManager(ChannelManager.class).getChannelsConfig();
    }

    @Override
    public String getSupportedPlugin() {
        return "SimpleClans";
    }

}
