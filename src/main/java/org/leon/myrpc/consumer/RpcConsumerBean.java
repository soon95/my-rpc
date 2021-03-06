package org.leon.myrpc.consumer;

import lombok.extern.slf4j.Slf4j;
import org.leon.myrpc.registry.ServiceRegistryFactory;
import org.leon.myrpc.registry.ServiceRegistryType;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author Leon Song
 * @date 2020-12-05
 */
@Slf4j
public class RpcConsumerBean implements FactoryBean {

    private Class<?> interfaceClass;

    private String serviceVersion;

    private String registryType;

    private String registryAddress;

    private Object object;

    @Override
    public Object getObject() throws Exception {
        return this.object;
    }

    @Override
    public Class<?> getObjectType() {
        return this.interfaceClass;
    }

    /**
     * 这个方法在Bean初始化时会被调用
     */
    public void init() throws Exception {
        this.object = RpcConsumer.create(this.interfaceClass,
                this.serviceVersion,
                ServiceRegistryFactory.getInstance(
                        ServiceRegistryType.valueOf(this.registryType),
                        this.registryAddress));

        log.info("RpcConsumerBean {} init...", interfaceClass.getName());
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getRegistryType() {
        return registryType;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }
}
