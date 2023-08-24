package mode.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;

public class ProxyServer {
    ProxyInterface proxyInterface;

    public ProxyServer(int port, ProxyInterface proxyInterface) {
        this.proxyInterface = proxyInterface;
        //System.out.println("服务器初始化");
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        new Thread(()->{
            try {
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .option(ChannelOption.SO_TIMEOUT,30000)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                //添加客户端通道的处理器
                                ch.pipeline().addLast(new ServerHandler());
                            }
                        });
                //System.out.println("          代理服务器 正在监听");
                ChannelFuture channelFuture= serverBootstrap.bind(port).sync().channel().closeFuture().sync();
                if (channelFuture.isSuccess()) {
                    //System.out.println("          代理服务器 启动成功");
                } else {
                    throw new RuntimeException("连接失败");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }).start();

    }


    private class ServerHandler extends ChannelHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //获取客户端发送过来的消息
            ByteBuf byteBuf = (ByteBuf) msg;
            ByteBuffer byteBuffer = byteBuf.nioBuffer();
            proxyInterface.getData(ctx.channel(), byteBuffer);
            //System.out.println("          代理服务器 收到数据" + ctx.channel().remoteAddress());

                byteBuf.release();
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            //System.out.println("          代理服务器 客户连接");
            proxyInterface.connect(ctx.channel());
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            //System.out.println("          代理服务器 客户断开");
            proxyInterface.disconnect(ctx.channel());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
