package org.leon.myrpc.consumer;

import org.leon.myrpc.provider.RpcProperties;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Leon Song
 * @date 2020-12-05
 */
@Configuration
@EnableConfigurationProperties(RpcProperties.class)
public class RpcConsumerAutoConfiguration {

    @Bean
    public BeanFactoryPostProcessor rpcConsumerPostProcess() {
        return new RpcConsumerPostProcessor();
    }

}
