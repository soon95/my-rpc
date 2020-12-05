package org.leon.myrpc.consumer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.leon.myrpc.protocol.RpcDecoder;
import org.leon.myrpc.protocol.RpcEncoder;
import org.leon.myrpc.protocol.RpcRequest;
import org.leon.myrpc.protocol.RpcResponse;
import org.leon.myrpc.registry.ServiceMetadata;
import org.leon.myrpc.registry.ServiceRegistry;
import org.leon.myrpc.registry.zookeeper.ProviderUtils;

import java.lang.reflect.Proxy;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Leon Song
 * @date 2020-12-05
 */
@Slf4j
public class RpcConsumer extends SimpleChannelInboundHandler<RpcResponse> {

    private Lock lock = new ReentrantLock();
    private ServiceRegistry registry;
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    private Channel channel;
    private RpcResponse response;

    public RpcConsumer(ServiceRegistry registry) {
        this.registry = registry;
    }

    public static <T> T create(Class<T> interfaceClass, String serviceVersion, ServiceRegistry serviceRegistry) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new RpcInvokeHandler(serviceVersion, serviceRegistry)
        );
    }

    public RpcResponse sendRequest(RpcRequest request) throws Exception {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            log.debug("init consumer request...");
                            ch.pipeline()
                                    .addLast(new RpcEncoder())
                                    .addLast(new RpcDecoder())
                                    .addLast(RpcConsumer.this);
                        }
                    });
            String targetService = ProviderUtils.makeKey(request.getClassName(), request.getServiceVersion());

            ServiceMetadata serviceMetadata = registry.discovery(targetService);
            if (serviceMetadata == null) {
                throw new RuntimeException("no available service provider for " + targetService);
            }

            log.debug("discovery provider for {} - {}", targetService, serviceMetadata);

            ChannelFuture future = bootstrap.connect(serviceMetadata.getAddress(), serviceMetadata.getPort()).sync();

            future.addListener((arg) -> {
                if (future.isSuccess()) {
                    log.debug("connect rpc provider success");
                } else {
                    log.debug("connect rpc provider fail");
                    future.cause().printStackTrace();
                    eventLoopGroup.shutdownGracefully();
                }
            });


            this.channel = future.channel();
            this.channel.writeAndFlush(request).sync();

            lock.lock();
            try {
                // 在这里等待远程结果
                lock.wait();
            } finally {
                lock.unlock();
            }


            return this.response;

        } finally {

            this.closeGracefully();

        }

    }

    /**
     * 优雅地关闭相关资源
     */
    private void closeGracefully() {

        if (this.channel != null) {
            this.channel.close();
        }

        if (this.eventLoopGroup != null) {
            this.eventLoopGroup.shutdownGracefully();
        }

        log.debug("shutdown consumer...");

    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {

        this.response = msg;

        lock.lock();
        try {
            // 拿到结果后主动唤起等待线程
            lock.notifyAll();
        } finally {
            lock.unlock();
        }
    }
}
