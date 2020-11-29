package org.leon.myrpc.provider;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.leon.myrpc.annotation.RPCProvider;
import org.leon.myrpc.registry.ServiceMetadata;
import org.leon.myrpc.registry.ServiceRegistry;
import org.leon.myrpc.registry.zookeeper.ProviderUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Leon Song
 * @date 2020-11-29
 */
@Slf4j
public class RpcProvider implements BeanPostProcessor {

    private static ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("rpc-provider-pool-%d").build();

    /**
     * 这个需要写成单例模式
     */
    private static ThreadPoolExecutor threadPoolExecutor;


    private String serverAddress;
    private ServiceRegistry serviceRegistry;
    private Map<String, Object> handlerMap = new HashMap<>(16);
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;


    public RpcProvider(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcProvider(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    public static void submit(Runnable task) {
        if (null == threadPoolExecutor) {
            synchronized (RpcProvider.class) {
                if (null == threadPoolExecutor) {
                    threadPoolExecutor = new ThreadPoolExecutor(256, 256,
                            600L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1024),
                            threadFactory);
                }
            }
        }
        threadPoolExecutor.submit(task);
    }

    public void start() throws InterruptedException {
        if (bossGroup == null || workerGroup == null) {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
                            // TODO 这里还少很多 序列化相关

                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);


            String[] split = serverAddress.split(":");
            String host = split[0];
            int port = Integer.parseInt(split[1]);

            ChannelFuture future = bootstrap.bind(host, port).sync();
            log.info("server started on port:{}", port);

            future.channel().closeFuture().sync();
        }
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }


    /**
     * 如果有 RPCProvider 注解，则注册到注册中心
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        RPCProvider rpcProvider = bean.getClass().getAnnotation(RPCProvider.class);
        if (rpcProvider == null) {
            return bean;
        }

        String serviceName = rpcProvider.serviceInterface().getName();
        String serviceVersion = rpcProvider.serviceVersion();
        String providerKey = ProviderUtils.makeKey(serviceName, serviceVersion);

        handlerMap.put(providerKey, bean);

        String[] split = serverAddress.split(":");
        String host = split[0];
        int port = Integer.parseInt(split[1]);

        ServiceMetadata metadata = new ServiceMetadata(serviceName, serviceVersion, host, port);

        try {
            serviceRegistry.registry(metadata);
            log.info("register service:{}", metadata);
        } catch (Exception e) {
            log.error("register service fail,{},{}", metadata, e);
        }

        return bean;
    }
}
