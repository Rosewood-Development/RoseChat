package dev.rosewood.rosechat.command.argument;

import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosegarden.command.framework.ArgumentHandler;
import java.util.UUID;

public class RoseChatArgumentHandlers {

    public static final ArgumentHandler<String> OFFLINE_PLAYER = new OfflinePlayerArgumentHandler(false);
    public static final ArgumentHandler<RosePlayer> ROSE_PLAYER = new RosePlayerArgumentHandler(false);
    public static final ArgumentHandler<Channel> CHANNEL = new ChannelArgumentHandler(true);
    public static final ArgumentHandler<String> CHAT_COLOR = new ChatColorArgumentHandler(PermissionArea.CHATCOLOR);
    public static final ArgumentHandler<UUID> UUID = new UUIDArgumentHandler();
    public static final ArgumentHandler<Integer> MUTE_DURATION = new MuteDurationArgumentHandler();
    public static final ArgumentHandler<GroupChannel> GROUP_CHAT = new GroupArgumentHandler(true);
    public static final ArgumentHandler<GroupChannel> MEMBER_GROUP = new MemberGroupArgumentHandler();
    public static final ArgumentHandler<RosePlayer> GROUP_MEMBER = new GroupMemberArgumentHandler();
    public static final ArgumentHandler<String> OFFLINE_GROUP = new OfflineGroupArgumentHandler();
    public static final ArgumentHandler<GroupChannel> GROUP_INVITE = new GroupInviteArgumentHandler();
    public static final ArgumentHandler<String> NICKNAME = new NicknameArgumentHandler();

}
