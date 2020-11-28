package org.leon.myrpc.registry;

/**
 * 用于服务治理
 *
 * @author Leon Song
 * @date 2020-11-28
 */
public interface ServiceRegistry {

    /**
     * 服务注册
     *
     * @param metadata 服务元信息
     */
    void registry(ServiceMetadata metadata);

    /**
     * 服务注销
     *
     * @param metadata 服务元信息
     */
    void unRegistry(ServiceMetadata metadata);

    /**
     * 服务发现
     *
     * @param serviceName 服务名
     * @return 服务元信息
     */
    ServiceMetadata discovery(String serviceName);

    /**
     * 关闭
     */
    void close();

}
