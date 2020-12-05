package org.leon.myrpc.registry;

import org.leon.myrpc.registry.zookeeper.ZooKeeperServiceRegistry;

/**
 * @author Leon Song
 * @date 2020-12-05
 */
public class ServiceRegistryFactory {

    /**
     * 因为这个是在容器启动时调用，所以应该不存在多线程争抢的问题
     * 因此不用单例模式应该也ok
     */
    private static volatile ServiceRegistry serviceRegistry;

    /**
     * 获得服务注册实例
     */
    public static ServiceRegistry getInstance(ServiceRegistryType type, String registryAddress) {

        if (null == serviceRegistry) {
            synchronized (ServiceRegistryFactory.class) {
                if (null == serviceRegistry) {
                    switch (type) {
                        case ZOOKEEPER:
                            serviceRegistry = new ZooKeeperServiceRegistry(registryAddress);
                            break;
                        default:
                            throw new RuntimeException("其他注册中心尚未开发完毕，敬请等待！");
                    }
                }
            }
        }

        return serviceRegistry;
    }
}
