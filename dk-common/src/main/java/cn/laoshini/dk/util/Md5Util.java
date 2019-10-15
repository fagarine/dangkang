package cn.laoshini.dk.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * MD5相关工具类
 *
 * @author fagarine
 */
public class Md5Util {
    private Md5Util() {
    }

    /**
     * 获取文件的MD5值
     *
     * @param filePath 文件路径
     * @return
     */
    public static String getFileMD5(String filePath) {
        if (StringUtil.isEmptyString(filePath)) {
            return null;
        }

        File file = new File(filePath);
        return getFileMD5(file);
    }

    /**
     * 获取文件的MD5值
     *
     * @param file 文件
     * @return
     */
    public static String getFileMD5(File file) {
        if (null == file || !file.exists()) {
            return null;
        }

        String md5 = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md.digest());
            // 输出为16进制
            md5 = bi.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return md5;
    }

    /**
     * 返回字符串的MD5值
     *
     * @param s
     * @return
     */
    public static String md5(String s) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

        try {
            byte[] btInput = s.getBytes(UTF_8);

            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");

            // 使用指定的字节更新摘要
            mdInst.update(btInput);

            // 获得密文
            byte[] md = mdInst.digest();

            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }

            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
