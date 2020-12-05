package org.leon.myrpc.consumer;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.leon.myrpc.protocol.RpcRequest;
import org.leon.myrpc.protocol.RpcResponse;
import org.leon.myrpc.registry.ServiceRegistry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author Leon Song
 * @date 2020-12-05
 */
@Data
@Slf4j
public class RpcInvokeHandler implements InvocationHandler {

    private static final String EQUALS = "equals";
    private static final String HASH_CODE = "hashcode";
    private static final String TO_STRING = "toString";

    private String serviceVersion;

    private ServiceRegistry serviceRegistry;

    public RpcInvokeHandler(String serviceVersion, ServiceRegistry serviceRegistry) {
        this.serviceVersion = serviceVersion;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 调用object类的基础方法需要特殊考虑
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if (EQUALS.equals(name)) {
                return proxy == args[0];
            } else if (HASH_CODE.equals(name)) {
                return System.identityHashCode(proxy);
            } else if (TO_STRING.equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with invocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setServiceVersion(this.serviceVersion);
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);

        RpcConsumer rpcConsumer = new RpcConsumer(this.serviceRegistry);
        RpcResponse response = rpcConsumer.sendRequest(request);
        if (response != null) {
            log.debug("consumer receive provider rpc response: {}", response);
            return response.getResult();
        } else {
            throw new RuntimeException("consumer rpc fail, response is null");
        }
    }
}
