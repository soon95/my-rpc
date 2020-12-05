package org.leon.myrpc.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Leon Song
 * @date 2020-12-05
 */
@Data
public class RpcRequest implements Serializable {

    private String requestId;
    private String className;
    private String methodName;
    private String serviceVersion;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
}
