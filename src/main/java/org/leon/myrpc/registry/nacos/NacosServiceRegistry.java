package org.leon.myrpc.registry.nacos;

import org.leon.myrpc.registry.ServiceMetadata;
import org.leon.myrpc.registry.ServiceRegistry;

/**
 * nacos的实现
 *
 * @author Leon Song
 * @date 2020-11-28
 */
public class NacosServiceRegistry implements ServiceRegistry {
    public void registry(ServiceMetadata metadata) {

    }

    public void unRegistry(ServiceMetadata metadata) {

    }

    public ServiceMetadata discovery(String serviceName) {
        return null;
    }

    public void close() {

    }
}
