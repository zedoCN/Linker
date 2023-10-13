package top.zedo.net;

import com.alibaba.fastjson2.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import top.zedo.LinkerLogger;
import top.zedo.data.LinkerCommand;
import top.zedo.data.LinkerEvent;
import top.zedo.data.LinkerGroup;
import top.zedo.data.LinkerUser;
import top.zedo.net.packet.BasePacket;
import top.zedo.net.packet.ChannelPacket;
import top.zedo.net.packet.JsonPacket;
import top.zedo.net.proxy.ProxyNetwork;

import java.net.InetSocketAddress;
import java.util.UUID;

public class LinkerClient {
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap clientBootstrap = new Bootstrap();
    InetSocketAddress LinkerServerAddress;
    Channel linkerServerChannel;
    public LinkerUser linkerUser;
    public LinkerGroup linkerGroup;
    LinkerClientEvent linkerClientEvent;
    public ProxyNetwork proxyNetwork = new ProxyNetwork(this);


    public LinkerClient(LinkerClientEvent linkerClientEvent) {
        this.linkerClientEvent = linkerClientEvent;
        clientBootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_RCVBUF, 512 * 1024)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(100 * 1024 * 1024, 0, 4, 0, 4));
                        ch.pipeline().addLast(new ClientHandler());
                    }
                });

    }

    public void setLinkerServerAddress(InetSocketAddress inetSocketAddress) {
        this.LinkerServerAddress = inetSocketAddress;
    }

    /**
     * 连接到Linker服务器
     */
    public boolean connect() {
        linkerServerChannel = clientBootstrap.connect(LinkerServerAddress).awaitUninterruptibly().channel();
        return linkerServerChannel.isActive();
    }

    /**
     * 修改昵称
     *
     * @param name 新昵称
     */
    public void changeName(String name) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        sendPacket(JsonPacket.buildCommandPacket(jsonObject, LinkerCommand.CHANGE_NAME));
    }

    /**
     * 创建组
     *
     * @param group 组名
     */
    public void createGroup(String group) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", group);
        sendPacket(JsonPacket.buildCommandPacket(jsonObject, LinkerCommand.CREATE_GROUP));
    }

    /**
     * 加入组
     *
     * @param uuid 组uuid
     */
    public void joinGroup(String uuid) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", uuid);
        sendPacket(JsonPacket.buildCommandPacket(jsonObject, LinkerCommand.JOIN_GROUP));
    }

    /**
     * 发送群消息
     *
     * @param message 消息
     */
    public void sendGroupMessage(String message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", message);
        sendPacket(JsonPacket.buildCommandPacket(jsonObject, LinkerCommand.SEND_GROUP_MESSAGE));
    }


    /**
     * 获得群列表
     */
    public void getGroups() {
        sendPacket(JsonPacket.buildCommandPacket(null, LinkerCommand.GET_GROUPS));
    }

    /**
     * 离开组
     */
    public void leaveGroup() {
        sendPacket(JsonPacket.buildCommandPacket(null, LinkerCommand.LEAVE_GROUP));
    }

    /**
     * 发送通道包
     *
     * @param packet 包
     */
    public void sendChannelPacket(ChannelPacket packet) {
        sendPacket(packet);
    }

    /**
     * 发送通道关闭事件
     *
     * @param channelUUID 通道uuid
     */
    public void sendChannelClose(UUID channelUUID, UUID to) {
        JSONObject json = new JSONObject();
        json.put("uuid", channelUUID.toString());
        json.put("to", to.toString());
        sendPacket(JsonPacket.buildEventPacket(json, LinkerEvent.CHANNEL_CLOSE));
    }

    /**
     * 向服务器发送数据包
     *
     * @param packet 数据包
     */
    protected void sendPacket(BasePacket packet) {
        if (linkerServerChannel != null) {
            if (linkerServerChannel.isActive()) {
                ByteBuf nettyByteBuf = Unpooled.wrappedBuffer(packet.buildPack());
                linkerServerChannel.writeAndFlush(nettyByteBuf);
            }
        }

    }

    /**
     * 处理事件
     *
     * @param event 事件类型
     * @param value 事件附加信息
     */
    private void handleEvent(LinkerEvent event, JSONObject value) {
        switch (event) {
            case USER_GET_START -> {
                linkerUser = LinkerUser.build(value);
            }
            case USER_LOGIN -> {
                LinkerLogger.info("连接成功");
                linkerUser = LinkerUser.build(value);
            }
            case CHANNEL_CLOSE -> {
                proxyNetwork.closeChannel(UUID.fromString(value.getString("uuid")));
                LinkerLogger.info("事件: 断开连接" + value);
            }
            case GROUP_UPDATE -> {
                linkerGroup = LinkerGroup.build(value);
            }
        }
        linkerClientEvent.handleEvent(linkerUser, event, value);
    }


    /**
     * 处理命令
     *
     * @param command 命令类型
     * @param value   命令附加信息
     */
    private void handleCommand(LinkerCommand command, JSONObject value) {
        switch (command) {

            case PING -> {
                value.put("acceptTime", System.currentTimeMillis());
                sendPacket(JsonPacket.buildCommandPacket(value, command));
            }

        }
    }

    private class ClientHandler extends ChannelHandlerAdapter {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            LinkerLogger.warning("连接异常" + cause.getMessage());
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            LinkerLogger.info("连接" + ctx.channel());

            /*JSONObject jsonObject = new JSONObject();

            sendPacket(JsonPacket.buildCommandPacket(jsonObject, LinkerCommand.LOGIN));*/
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            linkerClientEvent.handleEvent(linkerUser, LinkerEvent.USER_LEAVE, null);
            LinkerLogger.info("连接断开" + ctx.channel());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            BasePacket packet = BasePacket.resolved(((ByteBuf) msg).nioBuffer());
            ((ByteBuf) msg).release();//释放

            if (packet instanceof ChannelPacket channelPacket) {
                proxyNetwork.handleChannelPacket(channelPacket);
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
                        handleEvent(event, data.getJSONObject("value"));
                    }
                    case "command" -> {
                        LinkerCommand command;
                        try {
                            command = LinkerCommand.valueOf(data.getString("name"));
                        } catch (IllegalArgumentException e) {
                            LinkerLogger.warning("未知命令: " + e);
                            break;
                        }
                        handleCommand(command, data.getJSONObject("value"));
                    }
                    default -> {
                        LinkerLogger.warning("未知类型: " + data.getString("type"));
                    }
                }

            }
            //((ByteBuf) msg).release();//释放
        }
    }

    public interface LinkerClientEvent {
        void handleEvent(LinkerUser linkerUser, LinkerEvent event, JSONObject object);
    }
}
