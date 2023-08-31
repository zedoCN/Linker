package top.zedo.net;

import com.alibaba.fastjson2.JSONArray;
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
            ScheduledExecutorService trafficCalculatorExecutor = Executors.newSingleThreadScheduledExecutor();
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

        for (LinkerGroup group : groupMap.values()) {
            sendPacket(group, JsonPacket.buildEventPacket(group.toJSON(), LinkerEvent.GROUP_UPDATE));
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
        if (group != null) {
            for (LinkerUser user : group.members.values()) {
                sendPacket(user, packet);
            }
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
        switch (event) {
            case CHANNEL_CLOSE -> {
                if (user.group != null) {
                    UUID to = UUID.fromString(value.getString("to"));
                    sendPacket(userMap.get(to), JsonPacket.buildEventPacket(value, LinkerEvent.CHANNEL_CLOSE));
                }
            }
        }
    }

    /**
     * 处理命令
     *
     * @param user    命令来源
     * @param command 命令类型
     * @param value   命令附加信息
     */
    private void handleCommand(LinkerUser user, LinkerCommand command, JSONObject value) {
        JSONObject commandResult = new JSONObject();
        commandResult.put("command", command.name());
        switch (command) {
            case PING -> {
                user.delay = (int) (System.currentTimeMillis() - value.getLongValue("sendTime"));
                commandResult.put("success", true);
            }
            case CHANGE_NAME -> {
                user.name = value.getString("name");
                commandResult.put("success", true);
            }
            case CREATE_GROUP -> {
                leaveGroup(user);
                LinkerGroup group = new LinkerGroup(value.getString("name"), user);
                group.setUUID(UUID.randomUUID());
                groupMap.put(group.getUUID(), group);//创建组
                commandResult.put("success", true);
            }
            case GET_GROUPS -> {
                JSONObject jsonObject = new JSONObject();
                JSONArray groups = new JSONArray();
                jsonObject.put("groups", groups);
                for (LinkerGroup group : groupMap.values()) {
                    groups.add(group.toJSON());
                }
                sendPacket(user, JsonPacket.buildEventPacket(jsonObject, LinkerEvent.USER_GET_GROUPS));
                commandResult.put("success", true);
            }
            case SEND_GROUP_MESSAGE -> {
                sendPacket(user.group, JsonPacket.buildEventPacket(value, LinkerEvent.GROUP_GET_MESSAGES));
                commandResult.put("success", true);
            }
            case JOIN_GROUP -> {
                leaveGroup(user);
                UUID groupUUID = null;
                try {
                    groupUUID = UUID.fromString(value.getString("uuid"));
                } catch (IllegalArgumentException e) {
                    commandResult.put("success", false);
                    commandResult.put("message", "无效组UUID。");
                }

                if (groupUUID != null) {
                    LinkerGroup group = groupMap.get(groupUUID);
                    if (group != null) {
                        group.members.put(user.getUUID(), user);
                        user.group = group;
                        commandResult.put("success", true);
                    } else {
                        commandResult.put("success", false);
                        commandResult.put("message", "没有这个组。");
                    }
                }

            }
            case LEAVE_GROUP -> {
                leaveGroup(user);
                commandResult.put("success", true);
            }
        }
        sendPacket(user, JsonPacket.buildEventPacket(commandResult, LinkerEvent.COMMAND_SUCCESS));
    }

    /**
     * 处理通道包
     *
     * @param user   包来源
     * @param packet 包
     */
    private void handleChannelPacket(LinkerUser user, ChannelPacket packet) {
        if (user.group != null) {//必须在组里
            LinkerUser to = userMap.get(packet.toUser);
            if (to == null) {//检查目标用户是否存在
                LinkerLogger.warning("未知目标用户" + packet);
                return;
            }
            LinkerLogger.info("来源:" + user + " 转发数据包" + packet);
            sendPacket(to, packet);//转发数据包
        }
    }

    /**
     * 离开组
     *
     * @param user 用户
     */
    private void leaveGroup(LinkerUser user) {
        if (user.group != null) {//如果有组
            if (user.isHost()) {//用户是主机
                sendPacket(user.group, JsonPacket.buildEventPacket(null, LinkerEvent.HOST_DISSOLVE_GROUP));
                groupMap.remove(user.group.getUUID());
            } else {
                sendPacket(user.group, JsonPacket.buildEventPacket(user.toJSON(), LinkerEvent.USER_LEAVE_GROUP));
                user.group.members.remove(user.getUUID());
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
            linkerUser.setUUID(UUID.randomUUID());
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
                leaveGroup(linkerUser);
                userMap.remove(linkerUser.getUUID());//移除用户
                userChannelMap.remove(linkerUser.getUUID());
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
                    handleChannelPacket(linkerUser, channelPacket);
                } else if (packet instanceof JsonPacket jsonPacket) {
                    JSONObject data = jsonPacket.getJsonObject();
                    switch (data.getString("type")) {
                        case "event" -> {
                            LinkerEvent event;
                            try {
                                event = LinkerEvent.valueOf(data.getString("name"));
                            } catch (IllegalArgumentException e) {
                                LinkerLogger.warning("未知事件: " + e);
                                break;
                            }
                            handleEvent(linkerUser, event, data.getJSONObject("value"));
                        }
                        case "command" -> {
                            LinkerCommand command;
                            try {
                                command = LinkerCommand.valueOf(data.getString("name"));
                            } catch (IllegalArgumentException e) {
                                LinkerLogger.warning("未知命令: " + e);
                                break;
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
            ((ByteBuf) msg).release();//释放
        }
    }
}
