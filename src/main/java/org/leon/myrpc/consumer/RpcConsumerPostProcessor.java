package org.leon.myrpc.consumer;

import lombok.extern.slf4j.Slf4j;
import org.leon.myrpc.annotation.RPCConsumer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 这里将bean进行hack，替换成远程调用的bean
 *
 * @author Leon Song
 * @date 2020-12-05
 */
@Slf4j
public class RpcConsumerPostProcessor implements BeanFactoryPostProcessor, BeanClassLoaderAware, ApplicationContextAware {
    /**
     * 初始化bean的方法名
     */
    private final static String INIT_METHOD = "init";

    private ClassLoader classLoader;

    private ApplicationContext context;

    private Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();

    private ConfigurableListableBeanFactory beanFactory;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        this.beanFactory = beanFactory;

        for (String name : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(name);
            String beanClassName = beanDefinition.getBeanClassName();
            // 当用@Bean返回的类型是Object时，beanClassName是null
            if (beanClassName != null) {
                Class<?> clazz = ClassUtils.resolveClassName(beanClassName, this.classLoader);
                ReflectionUtils.doWithFields(clazz, this::parseElement);
            }
        }

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

        this.beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            if (context.containsBean(beanName)) {
                throw new IllegalArgumentException("[RPC Starter] Spring context already has a bean name " +
                        beanName + ", please change @RPCConsumer field name");
            }
            registry.registerBeanDefinition(beanName, beanDefinitionMap.get(beanName));
            log.info("registered RPCConsumerBean {} in spring context", beanName);
        });

    }

    private void parseElement(Field field) {
        RPCConsumer annotation = AnnotationUtils.getAnnotation(field, RPCConsumer.class);

        // 如果没有这个注解，则无事发生
        if (annotation == null) {
            return;
        }

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RpcConsumerBean.class);
        builder.setInitMethodName(INIT_METHOD);
        builder.addPropertyValue("serviceVersion", annotation.serviceVersion());
        builder.addPropertyValue("interfaceClass", field.getType());
        builder.addPropertyValue("registryType", annotation.registryType());
        builder.addPropertyValue("registryAddress", annotation.registryAddress());

        BeanDefinition beanDefinition = builder.getBeanDefinition();

        beanDefinitionMap.put(field.getName(), beanDefinition);
    }


    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        log.debug("classloader:{}", this.classLoader);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
