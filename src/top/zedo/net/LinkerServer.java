package top.zedo.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.AttributeKey;
import top.zedo.LinkerLogger;
import top.zedo.net.data.BasePacket;
import top.zedo.net.data.ChannelPacket;
import top.zedo.net.data.JsonPacket;
import top.zedo.user.LinkerGroup;
import top.zedo.user.LinkerUser;

import java.util.*;

public class LinkerServer {
    HashMap<UUID, LinkerUser> userMap = new HashMap<>();//用户表
    HashMap<UUID, LinkerGroup> groupMap = new HashMap<>();//组表
    HashMap<UUID, Channel> channelMap = new HashMap<>();//连接表

    // 定义的键
    AttributeKey<Integer> USER_KEY = AttributeKey.valueOf("linkerUser");

    int serverPort;

    public LinkerServer() {

    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void start() {
        LinkerLogger.info("Linker服务器初始化");
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 创建一个单线程的 bossGroup
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 使用默认的 workerGroup

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(131128, 0, 4, 0, 4))
                                    .addLast(new ServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture cf = serverBootstrap.bind(serverPort).sync();
            LinkerLogger.info("Linker服务器启动成功");
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            LinkerLogger.info("Linker服务器已关闭");
        }
    }

    private void sendPacket(Channel channel, BasePacket packet) {
        ByteBuf nettyByteBuf = Unpooled.wrappedBuffer(packet.buildPack());
        channel.writeAndFlush(nettyByteBuf);
    }

    private static class ServerHandler extends ChannelHandlerAdapter {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            LinkerLogger.warning("连接异常" + cause.getMessage());
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            LinkerLogger.info("连接" + ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            LinkerLogger.info("连接断开" + ctx.channel());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf data = (ByteBuf) msg;
            LinkerLogger.info("数据"+data);
            BasePacket packet = BasePacket.resolved(data.nioBuffer());
            if (packet instanceof ChannelPacket channelPacket) {

            } else if (packet instanceof JsonPacket jsonPacket) {
                LinkerLogger.info("接受json包" + jsonPacket);

            }
        }
    }
}
