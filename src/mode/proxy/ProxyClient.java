package mode.proxy;

import data.BasePack;
import data.InitPack;
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

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;

public class ProxyClient {
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap clientBootstrap = new Bootstrap();
    ProxyInterface proxyInterface;
    int port;
    String ip;

    public ProxyClient(String ip, int port, ProxyInterface proxyInterface) {
        this.port = port;
        this.proxyInterface = proxyInterface;
        this.ip = ip;
        ////System.out.println("       代理客户 初始化");

        clientBootstrap.group(group)
                .channel(NioSocketChannel.class)
                //长连接
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //添加客户端通道的处理器
                        ch.pipeline().addLast(new ClientHandler());
                    }
                });
    }


    public ChannelFuture creatChannel() {
        ChannelFuture channelFuture = clientBootstrap.connect(ip, port);
        return channelFuture;
    }

    private class ClientHandler extends ChannelHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            proxyInterface.connect(ctx.channel());
            ////System.out.println("       代理客户 连接" + ctx.channel());
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            proxyInterface.disconnect(ctx.channel());
            ////System.out.println("       代理客户 断开连接" + ctx.channel());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //获取客户端发送过来的消息
            ByteBuf byteBuf = (ByteBuf) msg;
            ByteBuffer byteBuffer = byteBuf.nioBuffer();
            proxyInterface.getData(ctx.channel(), byteBuffer);
            byteBuf.release();
            ////System.out.println("       代理客户 收到数据" + ctx.channel().remoteAddress());
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

    }
}
