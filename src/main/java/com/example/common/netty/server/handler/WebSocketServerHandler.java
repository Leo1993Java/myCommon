package com.example.common.netty.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author ligq
 * @Description:
 * @date 2019/11/24 10:52 上午
 */
public class WebSocketServerHandler extends ChannelInboundHandlerAdapter {

//    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    private Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);



    public static ExecutorService executor = Executors.newFixedThreadPool(7);

    /**
     * 群发消息的时候用到下面的
     * ====================start====================
     */
//    /**
//     * 握手连接
//     */
//    @Override
//    public void handlerAdded(ChannelHandlerContext ctx){
//        Channel channel = ctx.channel();
//        channels.add(channel);
//    }
//
//    @Override
//    public void handlerRemoved(ChannelHandlerContext ctx){
//         Channel cancel = ctx.channel();
//         channels.remove(cancel);
//    }
//
//    /**
//     * 通道建立调用
//     * @param ctx
//     * @throws Exception
//     */
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        logger.info("与客户端建立连接，通道开启！");
//        //将链接存入连接池(自定义)
//        channels.add(ctx.channel());
//       //MyChannelHandlerPool.channelGroup.add(ctx.channel());
//    }
//
//    /**
//     * 通道断开调用
//     * @param ctx
//     * @throws Exception
//     */
//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("与客户端断开连接，通道关闭！");
//        channels.remove(ctx.channel());
//        //添加到channelGroup 通道组
//       // MyChannelHandlerPool.channelGroup.remove(ctx.channel());
//    }

    /**
     * ==========end ====
     */

    /**
     * 通道数据读取
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //首次连接是FullHttpRequest，处理参数 by zhengkai.blog.csdn.net
        if (null != msg && msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            String uri = request.uri();
            Map paramMap=getUrlParams(uri);
            //System.out.println("接收到的参数是："+JSON.toJSONString(paramMap));
            //如果url包含参数，需要处理
            if(uri.contains("?")){
                String newUri=uri.substring(0,uri.indexOf("?"));
                System.out.println(newUri);
                request.setUri(newUri);
            }
        }else if(msg instanceof TextWebSocketFrame){
            //正常的TEXT消息类型
            TextWebSocketFrame frame=(TextWebSocketFrame)msg;
            System.out.println("客户端收到服务器数据：" +frame.text());
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String uuid = UUID.randomUUID().toString().replaceAll("-","");
                    String mes = Thread.currentThread().getId() + " ==> netty reciver ==> " + uuid + " ===> " + frame.text();
                    //ctx.writeAndFlush("netty reciver ==> " + uuid + " ===> " + frame.text());
                    //sendAllMessage(mes);
                    /**
                     * 直接拿当前上下文进行channel 有坑的地方是，务必要用TextWebSocketFrame 进行构造消息体
                     * 否则会出现 客户端 接受不到消息
                     */
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(mes));
                    /**
                     * 如果想要 广播消息，那么就要将信息放置在channel里面 进行处理
                     * 上面isIstance的active的方法就要打开，并且 利用channel通道组 来进行处理
                     */
                    //ctx.channel().writeAndFlush(mes);
                    //channels.writeAndFlush(mes)    ;
                    //super.channelRead(ctx, msg);
                    ctx.writeAndFlush(mes);
                    //ctx.fireChannelActive().writeAndFlush(mes);
                }
            });
        }
    }

    private void sendAllMessage(String message){
        //收到信息后，群发给所有channel
        //MyChannelHandlerPool.channelGroup.writeAndFlush( new TextWebSocketFrame(message));
    }

    private static Map getUrlParams(String url){
        Map<String,String> map = new HashMap<>();
        url = url.replace("?",";");
        if (!url.contains(";")){
            return map;
        }
        if (url.split(";").length > 0){
            String[] arr = url.split(";")[1].split("&");
            for (String s : arr){
                String key = s.split("=")[0];
                String value = s.split("=")[1];
                map.put(key,value);
            }
            return  map;

        }else{
            return map;
        }
    }
}
