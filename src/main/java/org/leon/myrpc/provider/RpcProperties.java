package org.leon.myrpc.provider;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Leon Song
 * @date 2020-12-05
 */
@Data
@ConfigurationProperties(prefix = "rpc")
public class RpcProperties {

    private String serviceAddress;

    private String serviceRegistryAddress;

    private String serviceRegistryType;
}
