package org.leon.myrpc.registry.zookeeper;

import org.leon.myrpc.registry.ServiceMetadata;


/**
 * @author Leon Song
 * @date 2020-11-29
 */
public class ProviderUtils {

    public static String makeKey(ServiceMetadata metadata) {

        return String.join(":", metadata.getServiceName(), metadata.getServiceVersion());

    }

    public static String makeKey(String serviceName, String serviceVersion) {

        return String.join(":", serviceName, serviceVersion);

    }
}
