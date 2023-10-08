package top.zedo.net.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import top.zedo.LinkerLogger;
import top.zedo.net.LinkerClient;
import top.zedo.net.packet.ChannelPacket;

import java.util.HashMap;
import java.util.UUID;

/**
 * 代理网络
 */
public class ProxyNetwork {
    boolean proxy = false;//是否开始代理
    boolean isHost = false;//是否是主机

    protected static AttributeKey<UUID> CHANNEL_UUID_KEY = AttributeKey.valueOf("uuid");

    ProxyServer proxyServer;
    ProxyClient proxyClient;
    HashMap<UUID, Channel> channelMap = new HashMap<>();//连接表
    int port;
    LinkerClient linkerClient;

    public ProxyNetwork(LinkerClient linkerClient) {
        this.linkerClient = linkerClient;
        proxyServer = new ProxyServer(linkerClient, this);
        proxyClient = new ProxyClient(linkerClient, this);
    }

    public void setIp(String ip) {
        proxyClient.setIp(ip);
    }

    /**
     * 设置代理模式
     *
     * @param isHost 是否是主机模式
     */
    public void setMode(boolean isHost) {
        this.isHost = isHost;
        LinkerLogger.info("设置代理模式为" + (isHost ? "主机模式" : "用户模式") + " 启动" + (isHost ? "代理客户" : "代理服务器"));
    }

    /**
     * 开始代理
     *
     * @param port 端口
     */
    public void start(int port) {
        if (!proxy) {
            this.port = port;
            if (isHost) {//主机模式 启动代理客户 (模拟客户)

            } else {//用户模式 启动代理服务器 (模拟服务器)
                proxyServer.start(port);
            }
            proxy = true;
        }
    }

    /**
     * 关闭代理
     */
    public void close() {
        if (proxy) {
            proxy = false;
            if (isHost) {//主机模式 关闭代理客户

            } else {//用户模式 关闭代理服务器
                proxyServer.close();
            }
        }
    }

    /**
     * 处理通道包
     *
     * @param channelPacket 包
     */
    public void handleChannelPacket(ChannelPacket channelPacket) {
        Channel channel = channelMap.get(channelPacket.uuid);
        if (isHost) {//是主机模式则创建 代理客户连接
            if (channel == null) {
                channel = proxyClient.connect(channelPacket.fromUser, port, channelPacket.uuid);
            }
        }
        if (channel == null) {
            LinkerLogger.warning("丢失连接" + channelPacket);
            linkerClient.sendChannelClose(channelPacket.uuid, channelPacket.fromUser);
            return;
        }
        //System.out.println("收到代理数据包:" + channelPacket);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(channelPacket.sourceData);
        channel.writeAndFlush(byteBuf);
    }

    public void closeChannel(UUID channelUUID) {
        Channel channel = channelMap.get(channelUUID);
        LinkerLogger.info("远程请求 断开连接" + channel);
        if (channel != null) {
            channel.close();
            channelMap.remove(channelUUID);
        }
    }
}
