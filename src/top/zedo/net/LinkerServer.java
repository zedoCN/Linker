package top.zedo.net;

import com.alibaba.fastjson2.JSONObject;
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
import top.zedo.data.LinkerCommand;
import top.zedo.data.LinkerEvent;
import top.zedo.net.packet.BasePacket;
import top.zedo.net.packet.ChannelPacket;
import top.zedo.net.packet.JsonPacket;
import top.zedo.data.LinkerGroup;
import top.zedo.data.LinkerUser;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LinkerServer {
    HashMap<UUID, LinkerUser> userMap = new HashMap<>();//用户表
    HashMap<UUID, Channel> userChannelMap = new HashMap<>();//用户连接表

    HashMap<UUID, LinkerGroup> groupMap = new HashMap<>();//组表


    // 定义的键
    AttributeKey<LinkerUser> USER_KEY = AttributeKey.valueOf("linkerUser");

    private ScheduledExecutorService trafficCalculatorExecutor;
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

            // 启动每隔1秒执行的计时器
            trafficCalculatorExecutor = Executors.newSingleThreadScheduledExecutor();
            trafficCalculatorExecutor.scheduleAtFixedRate(this::calculateInfo, 1, 1, TimeUnit.SECONDS);

            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            LinkerLogger.info("Linker服务器已关闭");
        }
    }

    /**
     * 计算流量
     */
    private void calculateInfo() {
        for (LinkerUser user : userMap.values()) {
            // 计算用户的流量
            user.calculateTraffic();
            // 计算延迟
            JSONObject ping = new JSONObject();
            ping.put("sendTime", System.currentTimeMillis());
            sendPacket(user, JsonPacket.buildCommandPacket(ping, LinkerCommand.PING));

            //发送用户状态
            sendPacket(user, JsonPacket.buildEventPacket(user.toJSON(), LinkerEvent.USER_GET_START));
        }
    }


    /**
     * 通过用户发送数据包
     *
     * @param user   用户
     * @param packet 数据包
     */
    private void sendPacket(LinkerUser user, BasePacket packet) {
        ByteBuf nettyByteBuf = Unpooled.wrappedBuffer(packet.buildPack());
        user.totalDownBytes += nettyByteBuf.capacity();//计算流量
        userChannelMap.get(user.getUUID()).writeAndFlush(nettyByteBuf);
    }

    /**
     * 通过组发送数据包 (广播)
     *
     * @param group  组
     * @param packet 数据包
     */
    private void sendPacket(LinkerGroup group, BasePacket packet) {
        for (LinkerUser user : group.members.values()) {
            sendPacket(user, packet);
        }
    }

    /**
     * 处理事件
     *
     * @param user  事件来源
     * @param event 事件类型
     * @param value 事件附加信息
     */
    private void handleEvent(LinkerUser user, LinkerEvent event, JSONObject value) {

    }

    /**
     * 处理命令
     *
     * @param user    命令来源
     * @param command 命令类型
     * @param value   命令附加信息
     */
    private void handleCommand(LinkerUser user, LinkerCommand command, JSONObject value) {
        switch (command) {
            case PING -> {
                user.delay = (int) (System.currentTimeMillis() - value.getLongValue("sendTime"));
            }
            case CHANGE_NAME -> {
                user.name = value.getString("newName");
            }
        }
    }


    private class ServerHandler extends ChannelHandlerAdapter {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            LinkerLogger.warning("连接异常" + cause.getMessage());
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            LinkerUser linkerUser = new LinkerUser();//为连接创建用户
            linkerUser.loginTime = System.currentTimeMillis();
            linkerUser.ipAddress = ctx.channel().remoteAddress().toString();
            userMap.put(linkerUser.getUUID(), linkerUser);
            userChannelMap.put(linkerUser.getUUID(), ctx.channel());
            ctx.attr(USER_KEY).set(linkerUser);//为通道绑定用户
            sendPacket(linkerUser, JsonPacket.buildEventPacket(linkerUser.toJSON(), LinkerEvent.USER_LOGIN));
            LinkerLogger.info("连接" + ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            LinkerUser linkerUser = ctx.attr(USER_KEY).get();
            if (linkerUser != null) {
                if (linkerUser.group != null) {//用户在一个组里
                    if (linkerUser.group.host.equals(linkerUser)) {//用户是主机
                        sendPacket(linkerUser.group, JsonPacket.buildEventPacket(null, LinkerEvent.HOST_DISSOLVE_GROUP));
                    } else {
                        sendPacket(linkerUser.group, JsonPacket.buildEventPacket(null, LinkerEvent.USER_LEAVE_GROUP));
                    }
                }
                userMap.remove(linkerUser.getUUID());//移除用户
                userChannelMap.remove(linkerUser.getUUID());
                ctx.attr(USER_KEY).remove();
            } else {
                LinkerLogger.warning("连接未绑定用户: " + ctx.channel());
            }
            LinkerLogger.info("连接断开" + ctx.channel());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            LinkerUser linkerUser = ctx.attr(USER_KEY).get();
            if (linkerUser != null) {
                BasePacket packet = BasePacket.resolved(((ByteBuf) msg).nioBuffer());
                linkerUser.totalUpBytes += ((ByteBuf) msg).readableBytes();//计算流量
                if (packet instanceof ChannelPacket channelPacket) {

                } else if (packet instanceof JsonPacket jsonPacket) {
                    JSONObject data = jsonPacket.getJsonObject();
                    switch (data.getString("type")) {
                        case "event" -> {
                            LinkerEvent event;
                            try {
                                event = LinkerEvent.valueOf(data.getString("name"));
                            } catch (IllegalArgumentException e) {
                                LinkerLogger.warning("未知事件: " + e);
                                return;
                            }
                            handleEvent(linkerUser, event, data.getJSONObject("value"));
                        }
                        case "command" -> {
                            LinkerCommand command;
                            try {
                                command = LinkerCommand.valueOf(data.getString("name"));
                            } catch (IllegalArgumentException e) {
                                LinkerLogger.warning("未知命令: " + e);
                                return;
                            }
                            handleCommand(linkerUser, command, data.getJSONObject("value"));
                        }
                        default -> {
                            LinkerLogger.warning("未知类型: " + data.getString("type"));
                        }
                    }
                    //LinkerLogger.info("接受数据: " + jsonPacket);

                }
            } else {
                LinkerLogger.warning("连接未绑定用户: " + ctx.channel());
            }
        }
    }
}
