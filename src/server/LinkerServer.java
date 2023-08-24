package server;

import data.*;
import group.LinkerGroup;
import group.LinkerUser;
import group.ProxyChannel;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.CharsetUtil;
import mode.LinkerClient;
import util.ByteBufferHexPrinter;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;

public class LinkerServer {
    HashMap<LinkerUser, LinkerGroup> groupUserMap = new HashMap<>();
    HashMap<UUID, LinkerGroup> groupUUIDMap = new HashMap<>();//群uuid

    HashMap<Channel, LinkerUser> userChannelMap = new HashMap<>();
    HashMap<UUID, LinkerUser> userUUIDMap = new HashMap<>();//用户uuid

    ByteBuffer packetsByteBuffer;//用于记录剩余的数据包数据


    public LinkerServer(int port) {
        System.out.println("   Linker服务器初始化");
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //添加客户端通道的处理器
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(40960, 0, 4, -4, 0));
                            ch.pipeline().addLast(new ServerHandler());
                        }
                    });
            serverBootstrap.bind(port).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    private class ServerHandler extends ChannelHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            //System.out.println("   Linker服务器 用户连接");
        }


        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //获取客户端发送过来的消息
            ByteBuffer byteBuffer;
            //获取客户端发送过来的消息
            if (msg instanceof ByteBuffer) {
                byteBuffer = (ByteBuffer) msg;
            } else {
                ByteBuf byteBuf = (ByteBuf) msg;
                byteBuffer = byteBuf.nioBuffer();
            }


            BasePack pack = BasePack.analyzePack(byteBuffer);

            //ByteBufferHexPrinter.printByteBufferAsHex(byteBuffer);
            //.out.println("   Linker服务器 收到数据" + ctx.channel() + " " + pack);
            if (pack == null) {
                System.out.println("未知数据包。");
                return;
            }


            LinkerUser user = userChannelMap.get(ctx.channel());
            if (pack instanceof InitPack initPack) {
                user.enable = true;
                user.name = initPack.getName();
                user.sendPack(new UserPack(user));
            } else if (pack instanceof EventPack eventPack) {
                switch (eventPack.getType()) {
                    case GROUP_CREAT -> {
                        createGroup(user, eventPack.getParameter());
                        user.sendPack(new GroupPack(user.group));
                    }
                    case GROUP_DISSOLVE -> {
                        if (user.equals(user.group.host)) {
                            dissolveGroup(user.group);
                        } else {
                            user.sendPack(new MessagePack("你不是主机啊！？"));
                        }
                    }
                    case GROUP_JOIN -> {
                        if (user.group != null) {
                            leaveGroup(user);
                        }
                        joinGroup(user, UUID.fromString(eventPack.getParameter()));
                    }
                    case GROUP_LEAVE -> {
                        if (user.group != null) {
                            leaveGroup(user);
                        }
                    }
                    case GET_GROUPS -> {
                        user.sendPack(new MessagePack(groupUUIDMap.toString()));
                    }
                    case CHANNEL_CONNECT -> {
                        if (user.equals(user.group.host)) {//消息来自主机
                            /*System.out.println("通道连接");
                            ProxyChannel proxyChannel = new ProxyChannel(UUID.fromString(eventPack.getParameter()), user);
                            user.group.proxyChannels.put(proxyChannel.uuid, proxyChannel);
                            System.out.println(user.group.proxyChannels);
                            user.group.host.sendPack(eventPack);*/
                        } else {//消息来自客户
                            System.out.println("通道连接");
                            ProxyChannel proxyChannel = new ProxyChannel(UUID.fromString(eventPack.getParameter()), user);
                            user.group.proxyChannels.put(proxyChannel.uuid, proxyChannel);
                            System.out.println(user.group.proxyChannels);
                            user.group.host.sendPack(eventPack);
                        }
                    }
                    case CHANNEL_DISCONNECT -> {
                        if (user.equals(user.group.host)) {//消息来自主机
                            System.out.println("   Linker服务器 通道断开" + eventPack.getParameter());
                            user.group.proxyChannels.remove(UUID.fromString(eventPack.getParameter()));
                            user.group.host.sendPack(eventPack);
                        } else {//消息来自客户
                            System.out.println("   Linker服务器 通道断开" + eventPack.getParameter());
                            ProxyChannel proxyChannel = user.group.proxyChannels.get(UUID.fromString(eventPack.getParameter()));
                            if (proxyChannel != null) {
                                user.group.proxyChannels.remove(proxyChannel.uuid);
                                proxyChannel.from.sendPack(eventPack);
                            }

                        }
                    }
                }
            } else if (pack instanceof DataPack dataPack) {
                if (user.equals(user.group.host)) {//来自主机的数据包 发送给对应的用户
                    System.out.println("   Linker服务器 转发数据包: 主机 -> 用户");
                    ProxyChannel proxyChannel = user.group.proxyChannels.get(dataPack.uuid);
                    proxyChannel.from.sendPack(dataPack);
                } else {//来自用户的数据包 发送给主机
                    System.out.println("   Linker服务器 转发数据包: 用户 -> 主机");
                    user.group.host.sendPack(dataPack);
                }
            }

            int remaining = byteBuffer.limit() - pack.size;
            if (0 < remaining) {//发现末尾有多余的包
                packetsByteBuffer = byteBuffer.slice(pack.size, remaining);
                channelRead(ctx, packetsByteBuffer);//尝试再次读取
            }

            if (msg instanceof ByteBuf byteBuf)
                byteBuf.release();
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            System.out.println("   Linker服务器 客户进入");
            LinkerUser user = new LinkerUser(ctx.channel());
            creatUser(user);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            System.out.println("   Linker服务器 客户退出");
            LinkerUser user = userChannelMap.get(ctx.channel());
            removeUser(user);
        }

        //处理异常, 一般是需要关闭通道
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }

    public void creatUser(LinkerUser user) {
        userUUIDMap.put(user.uuid, user);
        userChannelMap.put(user.channel, user);
        System.out.println("   Linker服务器 客户创建:" + user);
    }

    public void removeUser(LinkerUser user) {
        userUUIDMap.remove(user.uuid);
        userChannelMap.remove(user.channel);
        if (user.group != null) {//如果在一个组里
            leaveGroup(user);
        }
        System.out.println("   Linker服务器 客户离开:" + user);
    }

    public void createGroup(LinkerUser host, String name) {
        LinkerGroup group = new LinkerGroup(host, name);
        host.group = group;
        groupUserMap.put(host, group);
        groupUUIDMap.put(group.uuid, group);
        System.out.println("   Linker服务器 创建组:" + group);
    }

    public void dissolveGroup(LinkerGroup group) {
        for (LinkerUser user : group.users) {
            leaveGroup(user);//离开组
        }
        groupUserMap.remove(group.host);
        groupUUIDMap.remove(group.host.group.uuid);
        System.out.println("   Linker服务器 移除组:" + group);
    }

    public void joinGroup(LinkerUser user, UUID groupUUID) {
        user.group = groupUUIDMap.get(groupUUID);
        user.group.users.add(user);
        user.group.sendPack(new EventPack(EventType.GROUP_JOIN, user, ""));
        System.out.println("   Linker服务器 加入组:" + user);
    }

    public void leaveGroup(LinkerUser user) {
        if (user.equals(user.group.host)) {//如果是主机
            dissolveGroup(user.group);
        } else {
            user.group.sendPack(new EventPack(EventType.GROUP_LEAVE, user, ""));
            user.group.sendPack(new EventPack(EventType.GROUP_DISSOLVE, user, ""));
        }
        System.out.println("   Linker服务器 离开组:" + user);
    }
}
