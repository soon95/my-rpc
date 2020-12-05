package org.leon.myrpc.protocol.serialize;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Hession 序列化与反序列化
 *
 * @author Leon Song
 * @date 2020-12-05
 */
public class HessionSerialize {


    /**
     * 序列化
     */
    public static <T> byte[] serialize(T obj) {

        HessianOutput hessianOutput = null;

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            hessianOutput = new HessianOutput(byteArrayOutputStream);

            hessianOutput.writeObject(obj);

            return byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (hessianOutput != null) {
                try {
                    hessianOutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * 反序列化
     */
    public static <T> T deserialize(byte[] bytes) {
        HessianInput hessianInput = null;

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {

            hessianInput = new HessianInput(byteArrayInputStream);

            return (T) hessianInput.readObject();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (hessianInput != null) {
                hessianInput.close();
            }

        }

        return null;
    }
}
