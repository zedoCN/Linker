package old.mode.proxy;

import io.netty.channel.Channel;

import java.nio.ByteBuffer;

public interface ProxyInterface {

    public void getData(Channel channel, ByteBuffer buffer);

    public void disconnect(Channel channel);

    public void connect(Channel channel);
}
