package org.leon.myrpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务生产者注解
 *
 * @author Leon Song
 * @date 2020-11-29
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface RPCProvider {

    Class<?> serviceInterface() default Object.class;

    String serviceVersion() default "1.0.0";
}
