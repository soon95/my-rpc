package org.leon.myrpc.registry.zookeeper;

import lombok.SneakyThrows;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;
import org.leon.myrpc.constant.Constants;
import org.leon.myrpc.registry.ServiceMetadata;
import org.leon.myrpc.registry.ServiceRegistry;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ZooKeeper实现
 *
 * @author Leon Song
 * @date 2020-11-28
 */
public class ZooKeeperServiceRegistry implements ServiceRegistry {

    private CuratorFramework client;

    private ReentrantLock lock;

    private ServiceDiscovery<ServiceMetadata> serviceDiscovery;

    /**
     * 用于缓存
     */
    private Map<String, ServiceProvider<ServiceMetadata>> serviceProviderCache;

    private List<Closeable> cloneableProviders;

    @SneakyThrows
    public ZooKeeperServiceRegistry(String address) {
        this.serviceProviderCache = new ConcurrentHashMap<>(16);
        this.cloneableProviders = new ArrayList<>(16);

        this.client = CuratorFrameworkFactory.newClient(address, new ExponentialBackoffRetry(1000, 3));
        this.client.start();
        JsonInstanceSerializer<ServiceMetadata> serializer = new JsonInstanceSerializer<ServiceMetadata>(ServiceMetadata.class);
        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMetadata.class)
                .client(this.client)
                .serializer(serializer)
                .basePath(Constants.BASE_PATH)
                .build();

        serviceDiscovery.start();
    }


    @SneakyThrows
    @Override
    public void registry(ServiceMetadata metadata) {

        ServiceInstance<ServiceMetadata> serviceInstance = generateInstance(metadata);

        serviceDiscovery.registerService(serviceInstance);
    }


    @SneakyThrows
    @Override
    public void unRegistry(ServiceMetadata metadata) {

        ServiceInstance<ServiceMetadata> serviceInstance = generateInstance(metadata);

        serviceDiscovery.unregisterService(serviceInstance);

    }

    @SneakyThrows
    @Override
    public ServiceMetadata discovery(String serviceName) {

        // 首先从缓存中查找
        ServiceProvider<ServiceMetadata> serviceProvider = serviceProviderCache.get(serviceName);
        if (null == serviceProvider) {
            lock.lock();
            try {
                serviceProvider = serviceDiscovery
                        .serviceProviderBuilder()
                        .serviceName(serviceName)
                        .providerStrategy(new RoundRobinStrategy<>())
                        .build();
                serviceProvider.start();
                cloneableProviders.add(serviceProvider);
                serviceProviderCache.put(serviceName, serviceProvider);
            } finally {
                lock.unlock();
            }
        }

        ServiceInstance<ServiceMetadata> instance = serviceProvider.getInstance();
        return instance != null ? instance.getPayload() : null;
    }

    @SneakyThrows
    @Override
    public void close() {
        for (Closeable closeable : cloneableProviders) {
            CloseableUtils.closeQuietly(closeable);
        }
        serviceDiscovery.close();
    }

    /**
     * 生成一个实例
     */
    @SneakyThrows
    private static ServiceInstance<ServiceMetadata> generateInstance(ServiceMetadata metadata) {
        ServiceInstance<ServiceMetadata> serviceInstance = ServiceInstance
                .<ServiceMetadata>builder()
                .name(ProviderUtils.makeKey(metadata))
                .port(metadata.getPort())
                .payload(metadata)
                .uriSpec(new UriSpec(Constants.URI_SPEC))
                .build();

        return serviceInstance;
    }
}
