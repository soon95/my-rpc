package org.leon.myrpc.registry;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Leon Song
 * @date 2020-12-05
 */
@Slf4j
public class ServiceDiscoveryTest {

    private ServiceRegistry serviceRegistry;

    private static final String ADDRESS = "47.96.74.202:2181";

    @Before
    public void init() {
        this.serviceRegistry = ServiceRegistryFactory.getInstance(ServiceRegistryType.ZOOKEEPER, ADDRESS);
    }

    @After
    public void close() {
        serviceRegistry.close();
    }


    @Test
    public void test() {
        ServiceMetadata metadata = new ServiceMetadata();
        metadata.setServiceName("test");
        metadata.setAddress("127.0.0.1");
        metadata.setPort(8080);
        metadata.setServiceVersion("1.0.0");


        serviceRegistry.registry(metadata);

        ServiceMetadata result = serviceRegistry.discovery("test:1.0.0");

        Assert.assertNotNull(result);

        serviceRegistry.unRegistry(metadata);

    }


}
