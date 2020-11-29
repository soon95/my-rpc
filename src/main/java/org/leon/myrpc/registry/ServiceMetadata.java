package org.leon.myrpc.registry;

import lombok.Data;

/**
 * @author Leon Song
 * @date 2020-11-28
 */
@Data
public class ServiceMetadata {

    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 服务版本号
     */
    private String serviceVersion;

    /**
     * 服务ip地址
     */
    private String address;

    /**
     * 服务端口号
     */
    private Integer port;

    public ServiceMetadata(String serviceName, String serviceVersion, String address, Integer port) {
        this.serviceName = serviceName;
        this.serviceVersion = serviceVersion;
        this.address = address;
        this.port = port;
    }
}
