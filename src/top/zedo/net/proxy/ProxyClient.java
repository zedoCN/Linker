package top.zedo.net.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import top.zedo.LinkerLogger;
import top.zedo.data.LinkerUser;
import top.zedo.net.LinkerClient;
import top.zedo.net.packet.ChannelPacket;

import java.util.HashMap;
import java.util.UUID;

public class ProxyClient {
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap clientBootstrap = new Bootstrap();
    LinkerClient linkerClient;
    ProxyNetwork proxyNetwork;
    HashMap<UUID,UUID> channelMap=new HashMap<>();//通道uuid 目标用户uuid

    public ProxyClient(LinkerClient linkerClient, ProxyNetwork proxyNetwork) {
        this.linkerClient = linkerClient;
        this.proxyNetwork = proxyNetwork;
        LinkerLogger.info("代理客户 初始化");

        clientBootstrap.group(group)
                .channel(NioSocketChannel.class)
                //长连接
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //添加客户端通道的处理器
                        ch.pipeline().addLast(new ProxyClientHandler());
                    }
                });
    }

    public Channel connect(UUID from, int port, UUID uuid) {
        LinkerLogger.info("代理客户 连接" + uuid);
        ChannelFuture channelFuture = clientBootstrap.connect("", port);
        channelFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                channelMap.put(uuid,from);
                future.channel().attr(ProxyNetwork.CHANNEL_UUID_KEY).set(uuid);
                proxyNetwork.channelMap.put(uuid, future.channel());
                LinkerLogger.info("代理客户连接成功");
            } else {
                Throwable cause = future.cause(); // 获取引发失败的异常
                LinkerLogger.warning("代理客户连接失败" + cause);
                if (cause != null) {
                    cause.printStackTrace(); // 打印异常堆栈
                }
            }
        });
        channelFuture.awaitUninterruptibly();
        return channelFuture.channel();
    }

    public class ProxyClientHandler extends ChannelHandlerAdapter {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            LinkerLogger.warning("代理网络连接异常" + cause.getMessage());
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            UUID uuid = ctx.attr(ProxyNetwork.CHANNEL_UUID_KEY).get();//通道uuid
            UUID to = channelMap.get(uuid);//来源用户uuid
            LinkerLogger.info("代理客户连接上了 来自" + to + " 通道UUID" + uuid);

            LinkerLogger.info("连接" + ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            LinkerLogger.info("连接断开" + ctx.channel());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            UUID uuid = ctx.attr(ProxyNetwork.CHANNEL_UUID_KEY).get();//通道uuid
            UUID to = channelMap.get(uuid);//来源用户uuid
            //LinkerLogger.info("代理客户收到数据 来自" + to + " 通道UUID" + uuid);

            ChannelPacket channelPacket = new ChannelPacket(linkerClient.linkerUser.getUUID(), to, uuid, ((ByteBuf) msg).nioBuffer());
            linkerClient.sendChannelPacket(channelPacket);
            ((ByteBuf) msg).release();//释放
            //LinkerLogger.info("代理客户转发" + channelPacket);


        }

    }


}
