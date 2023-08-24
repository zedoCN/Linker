package mode;

import data.*;
import group.LinkerGroup;
import group.LinkerUser;
import group.ProxyChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import main.Main;
import mode.proxy.ProxyClient;
import mode.proxy.ProxyInterface;
import mode.proxy.ProxyServer;
import util.ByteBufferHexPrinter;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class LinkerClient {
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap clientBootstrap = new Bootstrap();
    public ChannelFuture channelFuture;
    Channel channel;
    ByteBufAllocator byteBufAllocator;

    public LinkerUser linkerUser;
    public LinkerGroup linkerGroup;

    ProxyClient proxyClient;
    ProxyServer proxyServer;

    HashMap<String, ProxyChannel> proxyChannelMap = new HashMap<>();
    HashMap<UUID, ProxyChannel> proxyChannelUUIDMap = new HashMap<>();

    ByteBuffer packetsByteBuffer;//用于记录剩余的数据包数据
    String username;
    String ip;

    public LinkerClient(String host, int port, String ip, String username) {
        this.ip = ip;
        this.username = username;
        System.out.println("客户端初始化");

        clientBootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(131128, 0, 4, -4, 0));
                        ch.pipeline().addLast(new ClientHandler());
                    }
                });

        channelFuture = clientBootstrap.connect(host, port).awaitUninterruptibly();


    }

    public static void sendProxy(Channel channel, ByteBuffer buffer) {
        //System.out.println("Linker客户端 代理发送数据" + channel);
        ByteBuf data = channel.alloc().buffer(buffer.limit());
        data.writeBytes(buffer);
        channel.writeAndFlush(data);
    }

    private void sendPack(BasePack basePack) {
        //System.out.println("Linker客户端 发送包:" + basePack);
        //System.out.println(channel);
        //System.out.println(channel.isWritable());
        //ByteBufferHexPrinter.printByteBufferAsHex(basePack.buildData(byteBufAllocator).nioBuffer());
        //System.out.println(channel);
        channel.writeAndFlush(basePack.buildData(byteBufAllocator));
    }

    public void createGroup(String name, int port) {
        sendPack(new EventPack(EventType.GROUP_CREAT, linkerUser, name));

        proxyClient = new ProxyClient(ip, port, new ProxyInterface() { //启动代理客户
            @Override
            public void getData(Channel channel, ByteBuffer buffer) {
                //System.out.println("[" + linkerUser.name + "]Linker客户端 收到目标服务器数据" + channel);
                if (proxyChannelMap.get(Integer.toHexString(channel.hashCode())) != null)
                    sendPack(new DataPack(proxyChannelMap.get(Integer.toHexString(channel.hashCode())).uuid, buffer));
                else {
                    //System.out.println(proxyChannelMap);
                    //System.out.println(Integer.toHexString(channel.hashCode()));
                    //System.out.println("[" + linkerUser.name + "]Linker客户端 连接丢失无法发送数据包" + channel);
                }


            }

            @Override
            public void disconnect(Channel channel) {
                //发送掉线事件
                if (proxyChannelMap.get(Integer.toHexString(channel.hashCode())) != null) {
                    sendPack(new EventPack(EventType.CHANNEL_DISCONNECT, linkerUser, proxyChannelMap.get(Integer.toHexString(channel.hashCode())).uuid.toString()));
                    proxyChannelUUIDMap.remove(proxyChannelMap.get(Integer.toHexString(channel.hashCode())).uuid);
                    proxyChannelMap.remove(Integer.toHexString(channel.hashCode()));
                    channel.deregister();
                }

            }

            @Override
            public void connect(Channel channel) {
                //System.out.println("[" + linkerUser.name + "]Linker客户端 代理客户连接到目标服务器" + channel + " " + Integer.toHexString(channel.hashCode()));
                //System.out.println(proxyChannelMap);
                //System.out.println(proxyChannelUUIDMap);


                /*ProxyChannel proxyChannel = new ProxyChannel(UUID.fromString(eventPack.getParameter()), channel);
                //System.out.println(proxyChannel.channel);
                proxyChannelUUIDMap.put(proxyChannel.uuid, proxyChannel);*/
                //ProxyChannel proxyChannel = proxyChannelMap.get(Integer.toHexString(channel.hashCode()));
                //proxyChannel.channel = channel;


                //UUID proxyUUID = UUID.randomUUID();
                //sendPack(new EventPack(EventType.CHANNEL_CONNECT, linkerUser, proxyUUID.toString()));
                //ProxyChannel proxyChannel = new ProxyChannel(proxyUUID, linkerUser);
                //proxyChannelUUIDMap.put(proxyUUID, proxyChannel);
                //proxyChannelMap.put(channel, proxyChannel);
            }
        });
    }

    public void joinGroup(String groupUUID, int port) {//代理服务器接受数据发送给主机

        sendPack(new EventPack(EventType.GROUP_JOIN, linkerUser, groupUUID));


        proxyServer = new ProxyServer(port, new ProxyInterface() { //启动代理服务器
            @Override
            public void getData(Channel channel, ByteBuffer buffer) {
                sendPack(new DataPack(proxyChannelMap.get(Integer.toHexString(channel.hashCode())).uuid, buffer));
            }

            @Override
            public void disconnect(Channel channel) {//代理客户将数据给目标服务器
                //发送掉线事件
                sendPack(new EventPack(EventType.CHANNEL_DISCONNECT, linkerUser, proxyChannelMap.get(Integer.toHexString(channel.hashCode())).uuid.toString()));
                proxyChannelUUIDMap.remove(proxyChannelMap.get(Integer.toHexString(channel.hashCode())).uuid);
                proxyChannelMap.remove(Integer.toHexString(channel.hashCode()));
                channel.deregister();
            }

            @Override
            public void connect(Channel channel) {
                UUID proxyUUID = UUID.randomUUID();
                sendPack(new EventPack(EventType.CHANNEL_CONNECT, linkerUser, proxyUUID.toString()));
                ProxyChannel proxyChannel = new ProxyChannel(proxyUUID, linkerUser);
                proxyChannel.channel = channel;
                proxyChannelUUIDMap.put(proxyUUID, proxyChannel);
                proxyChannelMap.put(Integer.toHexString(channel.hashCode()), proxyChannel);

            }
        });


    }

    private class ClientHandler extends ChannelHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            byteBufAllocator = ctx.alloc();
            System.out.println("Linker客户端 申请身份");
            channel = ctx.channel();
            sendPack(new InitPack(username));


        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            System.out.println("Linker客户端 断开服务器连接");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuffer byteBuffer;
            //获取客户端发送过来的消息
            if (msg instanceof ByteBuffer) {
                byteBuffer = (ByteBuffer) msg;
            } else {
                ByteBuf byteBuf = (ByteBuf) msg;
                byteBuffer = byteBuf.nioBuffer();
            }


            BasePack pack = BasePack.analyzePack(byteBuffer);
            //System.out.println("Linker客户端 收到数据" + ctx.channel() + " " + pack);
            if (pack == null) {
                //System.out.println("Linker客户端 未知数据包。");
                return;
            }
            if (pack instanceof EventPack eventPack) {
                switch (eventPack.getType()) {
                    case GROUP_LEAVE -> {
                        //System.out.println(eventPack.getSourceUser() + "离开了组");
                    }
                    case GROUP_JOIN -> {
                        //System.out.println(eventPack.getSourceUser() + "加入了组");
                    }
                    case CHANNEL_CONNECT -> {//主机模式


                        proxyClient.creatChannel().addListener((ChannelFuture future) -> {
                            if (future.isSuccess()) {
                                Channel channel = future.channel();
                                //System.out.println("[" + linkerUser.name + "]代理客户 启动成功");

                                ProxyChannel proxyChannel = new ProxyChannel(UUID.fromString(eventPack.getParameter()), channel);
                                //System.out.println(proxyChannel.channel);
                                proxyChannelUUIDMap.put(proxyChannel.uuid, proxyChannel);
                                proxyChannelMap.put(Integer.toHexString(channel.hashCode()), proxyChannel);
                                //channel.attr(AttributeKey.valueOf("UUID")).set(proxyChannel.uuid);
                            } else {
                                System.err.println("连接失败");
                                future.cause().printStackTrace();
                                // 处理连接失败的情况
                            }
                        });


                    }
                    case CHANNEL_DISCONNECT -> {
                        ProxyChannel proxyChannel = proxyChannelUUIDMap.get(UUID.fromString(eventPack.getParameter()));
                        if (proxyChannel != null) {
                            //System.out.println("[" + linkerUser.name + "]丢失连接" + channel);
                            proxyChannel.channel.close();
                            proxyChannelMap.remove(Integer.toHexString(proxyChannel.channel.hashCode()));
                            proxyChannelUUIDMap.remove(proxyChannel.uuid);
                        }

                    }
                }

            } else if (pack instanceof MessagePack messagePack) {
                System.out.println("[" + linkerUser.name + "]Linker客户端 消息： " + messagePack.getMessage());
            } else if (pack instanceof UserPack userPack) {
                linkerUser = userPack.getUser();
                System.out.println("[" + linkerUser.name + "]Linker客户端 获得身份");
            } else if (pack instanceof GroupPack groupPack) {
                linkerGroup = groupPack.group;

            } else if (pack instanceof DataPack dataPack) {//收到数据包
                //System.out.println("[" + linkerUser.name + "]Linker客户端 收到转发数据包");
                while (proxyChannelUUIDMap.get(dataPack.uuid) == null) {
                    Thread.sleep(10);
                    //System.out.println("[" + linkerUser.name + "]等待连接");
                }
                while (proxyChannelUUIDMap.get(dataPack.uuid).channel == null) {
                    Thread.sleep(100);
                    //System.out.println("[" + linkerUser.name + "]等待连接" + proxyChannelUUIDMap.get(dataPack.uuid));
                }
                if (proxyClient != null) {//转发给目标服务器
                    //System.out.println("[" + linkerUser.name + "]Linker客户端 转发给目标服务器");
                    sendProxy(proxyChannelUUIDMap.get(dataPack.uuid).channel, dataPack.sourceData);
                } else if (proxyServer != null) {//收到目标服务器数据
                    //System.out.println("[" + linkerUser.name + "]Linker客户端 收到目标服务器数据");
                    sendProxy(proxyChannelUUIDMap.get(dataPack.uuid).channel, dataPack.sourceData);
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
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
