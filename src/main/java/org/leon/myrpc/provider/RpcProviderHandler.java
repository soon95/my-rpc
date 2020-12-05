package org.leon.myrpc.provider;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.leon.myrpc.protocol.RpcRequest;
import org.leon.myrpc.protocol.RpcResponse;
import org.leon.myrpc.registry.zookeeper.ProviderUtils;
import org.springframework.cglib.reflect.FastClass;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * 服务处理器
 *
 * @author Leon Song
 * @date 2020-12-05
 */
@Slf4j
public class RpcProviderHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final Map<String, Object> handlerMap;

    public RpcProviderHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        RpcProvider.submit(() -> {
            log.debug("Receive request {}", rpcRequest.getRequestId());
            RpcResponse response = new RpcResponse();
            response.setRequestId(rpcRequest.getRequestId());
            try {
                Object result = this.handle(rpcRequest);
            } catch (Throwable e) {
                response.setError(e.toString());
                log.error("RPC server handler request error", e);
            }

            channelHandlerContext.writeAndFlush(response).addListener(
                    channelFuture ->
                            log.debug("Send response for request " + rpcRequest.getRequestId()));

        });
    }

    /**
     * 处理请求的地方
     * 这里抛出异常
     */
    private Object handle(RpcRequest request) throws InvocationTargetException {

        String providerKey = ProviderUtils.makeKey(request.getClassName(), request.getServiceVersion());

        Object providerBean = handlerMap.get(providerKey);

        if (null == providerBean) {
            throw new RuntimeException(String.format("provider not exist: %s:%s", request.getClassName(), request.getMethodName()));
        }

        Class<?> providerBeanClass = providerBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        log.debug("handle:{}-{}", providerBeanClass.getSimpleName(), methodName);

        FastClass providerFastClass = FastClass.create(providerBeanClass);
        int methodIndex = providerFastClass.getIndex(methodName, parameterTypes);
        return providerFastClass.invoke(methodIndex, providerBean, parameters);
    }
}
