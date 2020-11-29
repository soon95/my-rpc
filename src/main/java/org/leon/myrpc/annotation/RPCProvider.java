package org.leon.myrpc.annotation;

/**
 * 服务生产者注解
 *
 * @author Leon Song
 * @date 2020-11-29
 */
public @interface RPCProvider {

    Class<?> serviceInterface() default Object.class;

    String serviceVersion() default "1.0.0";
}
