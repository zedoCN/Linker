package group;

import io.netty.channel.Channel;

import java.util.UUID;

public class ProxyChannel {
    public UUID uuid;
    public LinkerUser from;
    public Channel channel;

    @Override
    public String toString() {
        return "ProxyChannel{" +
                "uuid=" + uuid +
                ", from=" + from +
                ", channel=" + channel +
                '}';
    }

    public ProxyChannel(UUID uuid, LinkerUser from) {
        this.uuid = uuid;
        this.from = from;
    }

    public ProxyChannel(UUID uuid, Channel channel) {
        this.uuid = uuid;
        this.channel = channel;
    }
}
