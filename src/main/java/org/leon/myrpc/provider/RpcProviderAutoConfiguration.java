package org.leon.myrpc.provider;

import org.leon.myrpc.registry.ServiceRegistry;
import org.leon.myrpc.registry.ServiceRegistryFactory;
import org.leon.myrpc.registry.ServiceRegistryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Leon Song
 * @date 2020-12-05
 */
@Configuration
@EnableConfigurationProperties(RpcProperties.class)
public class RpcProviderAutoConfiguration {

    @Autowired
    private RpcProperties rpcProperties;

    @Bean
    public RpcProvider rpcProvider() {
        ServiceRegistryType type = ServiceRegistryType.valueOf(rpcProperties.getServiceRegistryType());
        ServiceRegistry serviceRegistry = ServiceRegistryFactory.getInstance(type, rpcProperties.getServiceRegistryAddress());
        return new RpcProvider(rpcProperties.getServiceAddress(), serviceRegistry);
    }
}
