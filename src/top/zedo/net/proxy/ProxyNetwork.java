package top.zedo.net.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import top.zedo.LinkerLogger;
import top.zedo.net.LinkerClient;
import top.zedo.net.packet.ChannelPacket;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 代理网络
 */
public class ProxyNetwork {
    boolean proxy = false;//是否开始代理
    boolean isHost = false;//是否是主机

    protected static AttributeKey<UUID> CHANNEL_UUID_KEY = AttributeKey.valueOf("uuid");
    private final ScheduledExecutorService flushScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;
    ProxyServer proxyServer;
    ProxyClient proxyClient;
    public HashMap<UUID, Channel> channelMap = new HashMap<>();//连接表
    HashMap<UUID, ByteBuf> bufferMap = new HashMap<>();//缓冲区表
    private final Lock bufferLock = new ReentrantLock();

    int port;
    LinkerClient linkerClient;

    public ProxyNetwork(LinkerClient linkerClient) {
        this.linkerClient = linkerClient;
        proxyServer = new ProxyServer(linkerClient, this);
        proxyClient = new ProxyClient(linkerClient, this);
        setFlushDelay(20);
    }

    public void setFlushDelay(long flushDelay) {
        if (scheduledFuture != null)
            scheduledFuture.cancel(false);
        scheduledFuture = flushScheduler.scheduleAtFixedRate(this::flushBuffer, 0, flushDelay, TimeUnit.MILLISECONDS);
    }

    private void flushBuffer() {
        bufferLock.lock();
        try {
            for (UUID uuid : channelMap.keySet()) {
                ByteBuf buffer = bufferMap.get(uuid);
                if (buffer == null)
                    continue;
                if (buffer.readableBytes() < 0)
                    continue;
                UUID to;
                if (isHost)
                    to = proxyClient.channelMap.get(uuid);
                else
                    to = linkerClient.linkerGroup.host.getUUID();
                linkerClient.sendChannelPacket(uuid, to, buffer);

                /*for (int i = 0; i < buffer.numComponents(); i++) {
                    buffer.component(i).release();
                }*/
                buffer.clear();
                //buffer.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bufferLock.unlock();
        }
    }

    protected void sendProxyData(UUID uuid, ByteBuf byteBuf) {
        bufferLock.lock();
        ByteBuf buffer = bufferMap.get(uuid);
        if (buffer == null) {
            buffer = Unpooled.compositeBuffer();
            bufferMap.put(uuid, buffer);
        }
        buffer.writeBytes(byteBuf.nioBuffer());
        bufferLock.unlock();
        if (buffer.readableBytes() > 1024 * 1024 * 10)
            flushBuffer();
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

    public boolean isOpen() {
        return proxy;
    }

    /**
     * 关闭代理
     */
    public void close() {
        System.out.println("关闭代理");
        try {
            if (isHost) {//主机模式 关闭代理客户
                for (Channel channel : proxyClient.proxyNetwork.channelMap.values()) {
                    channel.close();
                }
            } else {//用户模式 关闭代理服务器
                proxyServer.close();
            }
        } catch (Exception e) {
        }
        proxy = false;
    }


    /**
     * 处理通道包
     *
     * @param channelPacket 包
     */
    public void handleChannelPacket(ChannelPacket channelPacket) {
        //System.out.println("收到数据包 包数量: " + channelPacket);


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

        ByteBuf byteBuf = Unpooled.wrappedBuffer(channelPacket.sourceData);
        channel.write(byteBuf);
        channel.flush();
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
