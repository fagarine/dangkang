package cn.laoshini.dk.util;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * 数据压缩工具类
 *
 * @author fagarine
 */
public class ZlibUtil {
    private ZlibUtil() {
    }

    /**
     * 压缩
     *
     * @param data
     * @return
     */
    public static byte[] compress(byte[] data) {
        byte[] output = null;

        Deflater compressor = new Deflater();
        compressor.reset();
        compressor.setInput(data);
        compressor.finish();

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);) {
            byte[] buf = new byte[1024];
            while (!compressor.finished()) {
                int i = compressor.deflate(buf);
                bos.write(buf, 0, i);
            }

            output = bos.toByteArray();
        } catch (Exception e) {
            output = data;
            LogUtil.error("压缩失败", e);
        }

        compressor.end();
        return output;
    }

    /**
     * 解压
     *
     * @param data
     */
    public static byte[] decompress(byte[] data) {
        byte[] output = null;

        Inflater decompressor = new Inflater();
        decompressor.reset();
        decompressor.setInput(data);

        try (ByteArrayOutputStream o = new ByteArrayOutputStream();) {
            byte[] buf = new byte[1024];
            while (!decompressor.finished()) {
                int i = decompressor.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            LogUtil.error("解压失败", e);
        }

        decompressor.end();
        return output;
    }
}
