package com.example.common.netty.server;

import com.example.common.netty.server.handler.WebSocketServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 版权声明：本文为CSDN博主「fjssharpsword」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
 * 原文链接：https://blog.csdn.net/fjssharpsword/article/details/52354098
 */
public class NettyServer {
    private int port;
    public NettyServer(int port) {
        this.port = port;
        bind();
    }

    private void bind() {

        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        try {

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024); //连接数
            bootstrap.option(ChannelOption.TCP_NODELAY, true);  //不延迟，消息立即发送
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true); //长连接
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    //p.addLast(new NettyServerHandler());
                    //用于Http请求的编码或者解码
                    pipeline.addLast("http-codec", new HttpServerCodec());
                    pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                    //把Http消息组成完整地HTTP消息
                    pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                    //向客户端发送HTML5文件
                    pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                    //参数指的是contex_path
                    pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
                    //实际处理的Handler
                    pipeline.addLast("handler", new WebSocketServerHandler());
                    //pipeline.addLast("handler", new SimpleWebSocketServerHandler());
                } });

            ChannelFuture f = bootstrap.bind(port).sync();
            if (f.isSuccess()) {
                System.out.println("启动Netty服务成功，端口号：" + this.port);
            }
            //Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            System.out.println("启动Netty服务异常，异常信息：" + e.getMessage());
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        NettyServer server= new NettyServer(9999);

    }

}
