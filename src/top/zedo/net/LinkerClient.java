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
import top.zedo.net.data.BasePacket;
import top.zedo.net.data.ChannelPacket;
import top.zedo.net.data.JsonPacket;
import top.zedo.util.HexPrinter;

import java.net.InetSocketAddress;

public class LinkerClient {
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap clientBootstrap = new Bootstrap();
    InetSocketAddress LinkerServerAddress;

    Channel linkerServerChannel;

    public LinkerClient() {
        clientBootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(131128, 0, 4, 0, 4));
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
    public void connect() {
        linkerServerChannel = clientBootstrap.connect(LinkerServerAddress).awaitUninterruptibly().channel();
    }

    /**
     * 向服务器发送数据包
     *
     * @param packet 数据包
     */
    private void sendPacket(BasePacket packet) {
        ByteBuf nettyByteBuf = Unpooled.wrappedBuffer(packet.buildPack());
        HexPrinter.printHex( nettyByteBuf.nioBuffer());
        linkerServerChannel.writeAndFlush(nettyByteBuf);
    }

    private class ClientHandler extends ChannelHandlerAdapter {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            LinkerLogger.warning("连接异常" + cause.getMessage());
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            LinkerLogger.info("连接" + ctx.channel());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("你好", "WDNSM");
            System.out.println(new JsonPacket(jsonObject).buildPack());
            sendPacket(new JsonPacket(jsonObject));
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            LinkerLogger.info("连接断开" + ctx.channel());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf data = (ByteBuf) msg;
            BasePacket packet = BasePacket.resolved(data.nioBuffer());
            if (packet instanceof ChannelPacket channelPacket) {

            } else if (packet instanceof JsonPacket jsonPacket) {
                LinkerLogger.info("接受json包" + jsonPacket);

            }
        }
    }
}
