package org.leon.myrpc.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Leon Song
 * @date 2020-12-05
 */
@Data
public class RpcResponse implements Serializable {

    private String requestId;

    private String error;

    private Object result;
}
