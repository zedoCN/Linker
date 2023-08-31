package top.zedo.net.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import top.zedo.LinkerLogger;
import top.zedo.net.LinkerClient;
import top.zedo.net.packet.ChannelPacket;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;

public class ProxyServer {
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    LinkerClient linkerClient;
    ProxyNetwork proxyNetwork;


    public ProxyServer(LinkerClient linkerClient, ProxyNetwork proxyNetwork) {
        this.linkerClient = linkerClient;
        this.proxyNetwork = proxyNetwork;
    }

    /**
     * 启动代理服务器
     */
    public void start(int port) {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        LinkerLogger.info("代理服务器初始化");
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        //添加客户端通道的处理器
                        ch.pipeline().addLast(new ProxyServerHandler());
                    }
                });
        ChannelFuture channelFuture = serverBootstrap.bind(port);
        channelFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                LinkerLogger.info("代理服务器启动成功");
            } else {
                Throwable cause = future.cause(); // 获取引发失败的异常
                LinkerLogger.warning("代理服务器启动失败" + cause);
                if (cause != null) {
                    cause.printStackTrace(); // 打印异常堆栈
                }
            }
        });
    }

    /**
     * 关闭代理服务器
     */
    public void close() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        LinkerLogger.info("代理服务器关闭");
    }

    public class ProxyServerHandler extends ChannelHandlerAdapter {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            LinkerLogger.warning("代理网络连接异常" + cause.getMessage());
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            UUID uuid = UUID.randomUUID();
            ctx.attr(ProxyNetwork.CHANNEL_UUID_KEY).set(uuid);
            proxyNetwork.channelMap.put(uuid, ctx.channel());

            /*ChannelPacket channelPacket = new ChannelPacket(linkerClient.linkerUser.getUUID()
                    , linkerClient.linkerGroup.host.getUUID()
                    , uuid
                    , ByteBuffer.allocateDirect(0));
            linkerClient.sendChannelPacket(channelPacket);*/

            LinkerLogger.info("连接" + ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            UUID uuid = ctx.attr(ProxyNetwork.CHANNEL_UUID_KEY).get();
            LinkerLogger.info("连接断开" + ctx.channel());
            proxyNetwork.channelMap.remove(uuid);
            linkerClient.sendChannelClose(uuid, linkerClient.linkerGroup.host.getUUID());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            UUID uuid = ctx.attr(ProxyNetwork.CHANNEL_UUID_KEY).get();//通道uuid
            ChannelPacket channelPacket = new ChannelPacket(linkerClient.linkerUser.getUUID()
                    , linkerClient.linkerGroup.host.getUUID()
                    , uuid
                    , ((ByteBuf) msg).nioBuffer());
            linkerClient.sendChannelPacket(channelPacket);
            ((ByteBuf) msg).release();//释放
            //LinkerLogger.info("代理服务器转发" + channelPacket);
        }

    }
}
