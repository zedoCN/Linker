package top.zedo.util;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import top.zedo.LinkerLogger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Test {

    private static int testIndex = 0;
    private static int testSize = 20480000;

    public static class Client {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap clientBootstrap = new Bootstrap();

        public Client() {
            clientBootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_RCVBUF, 8 * 1024 * 1024)
                    .option(ChannelOption.SO_SNDBUF, 8 * 1024 * 1024)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ClientHandler());
                        }
                    });

        }

        public Channel connect(SocketAddress remoteAddress) {
            return clientBootstrap.connect(remoteAddress).awaitUninterruptibly().channel();
        }

        static class ClientHandler extends ChannelHandlerAdapter {
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
                //linkerClientEvent.handleEvent(linkerUser, LinkerEvent.USER_LEAVE, null);
                LinkerLogger.info("连接断开" + ctx.channel());
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

                //System.out.println("客户端收到数据: " + ((ByteBuf) msg).readableBytes());
                if (testIndex == -1) return;
                ByteBuf data = ((ByteBuf) msg);
                //HexPrinter.printHex(((ByteBuf) msg).nioBuffer());
                //data.resetReaderIndex();
                while (data.readableBytes() >= 4) {
                    int testData = data.readInt();
                    if (testIndex != testData) {
                        System.out.println("校验错误 因:" + testIndex + " 源:" + testData);
                        testIndex = -1;
                        System.exit(-1);
                    }
                    testIndex++;
                    if (testIndex == testSize) {
                        System.out.println("测试完毕");
                        testIndex=0;
                    }
                }
                ((ByteBuf) msg).release();//释放

            }
        }
    }


    public static class Server {
        public Server(int port) {
            EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 创建一个单线程的 bossGroup
            EventLoopGroup workerGroup = new NioEventLoopGroup(); // 使用默认的 workerGroup

            try {
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_RCVBUF, 8 * 1024 * 1024)
                        .option(ChannelOption.SO_SNDBUF, 8 * 1024 * 1024)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ch.pipeline()
                                        .addLast(new ServerHandler());
                            }
                        })
                        .option(ChannelOption.SO_KEEPALIVE, true);

                ChannelFuture cf = serverBootstrap.bind(port).sync();
                LinkerLogger.info("服务器启动成功");

                cf.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                LinkerLogger.info("服务器已关闭");
            }
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
                //System.out.println("服务器收到数据: " + ((ByteBuf) msg).readableBytes());
                //HexPrinter.printHex(((ByteBuf) msg).nioBuffer());
                if (testIndex == -1) return;
                ByteBuf data = ((ByteBuf) msg);
                //HexPrinter.printHex(((ByteBuf) msg).nioBuffer());
                //data.resetReaderIndex();
                while (data.readableBytes() >= 4) {
                    int testData = data.readInt();
                    if (testIndex != testData) {
                        System.out.println("校验错误 因:" + testIndex + " 源:" + testData);
                        testIndex = -1;
                        System.exit(-1);
                    }
                    testIndex++;
                    if (testIndex == testSize) {
                        System.out.println("测试完毕");
                        testIndex=0;
                        ByteBuf test = Unpooled.buffer(testSize * 4);
                        for (int i = 0; i < testSize; i++) {
                            test.writeInt(i);
                        }
                        ctx.write(test);
                        ctx.flush();

                    }
                }

                //ctx.flush();
                //((ByteBuf) msg).release();//释放
            }
        }

    }

    public static void main(String[] args) throws Exception {


        new Thread(() -> {
            Server server = new Server(25565);
        }).start();
        Client client = new Client();
        Channel channel = client.connect(new InetSocketAddress("127.0.0.1", 6789));


        ByteBuf test = Unpooled.buffer(testSize * 4);
        for (int i = 0; i < testSize; i++) {
            test.writeInt(i);
        }
        channel.write(test);
        channel.flush();
        //channel.writeAndFlush(Unpooled.wrappedBuffer("NMSL".repeat(10240).getBytes()));
        //channel.writeAndFlush(Unpooled.wrappedBuffer("6666".getBytes()));
        //channel.writeAndFlush(Unpooled.wrappedBuffer("1234".repeat(1024).getBytes()));
    }

}
