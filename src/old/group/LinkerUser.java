package old.group;

import old.data.BasePack;
import io.netty.channel.Channel;

import java.util.UUID;

public class LinkerUser {
    public String name;
    public Channel channel;//连接
    public LinkerGroup group;//所在组
    public long timestamp = System.currentTimeMillis();//创建时间
    public boolean enable = false;//是启用状态

    public UUID uuid = UUID.randomUUID();


    public LinkerUser(Channel channel) {
        this.channel = channel;
    }

    public void sendPack(BasePack pack) {
        //System.out.println("   Linker服务器 发送包:" + pack);
        channel.writeAndFlush(pack.buildData(channel.alloc()));
    }

    @Override
    public String toString() {
        return "LinkerUser{" +
                "name='" + name + '\'' +
                ", old.group=" + (group != null ? group.name : "") +
                ", timestamp=" + timestamp +
                ", enable=" + enable +
                '}';
    }
}
