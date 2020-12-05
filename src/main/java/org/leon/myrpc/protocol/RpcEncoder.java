package org.leon.myrpc.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.leon.myrpc.protocol.serialize.HessionSerialize;

/**
 * @author Leon Song
 * @date 2020-12-05
 */
public class RpcEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        byte[] serialize = HessionSerialize.serialize(o);
        assert serialize != null;
        byteBuf.writeInt(serialize.length);
        byteBuf.writeBytes(serialize);
    }
}
