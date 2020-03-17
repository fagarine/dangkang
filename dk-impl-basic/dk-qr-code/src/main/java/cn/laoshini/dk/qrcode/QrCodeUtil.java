package cn.laoshini.dk.qrcode;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

import cn.laoshini.dk.exception.BusinessException;

/**
 * @author fagarine
 */
public class QrCodeUtil {
    static final String DEFAULT_CHARSET = "UTF-8";
    static final String DEFAULT_FORMAT = "JPG";
    /**
     * 二维码尺寸缺省值
     */
    static final int DEFAULT_QR_CODE_LEN = 300;
    /**
     * LOGO图片的最大尺寸，仅在压缩时有效
     */
    static final int LOGO_MAX_LEN = 100;
    /**
     * LOGO图片占二维码的最大百分比（边长占比），仅在压缩时有效
     */
    static final int LOGO_MAX_PERCENT = 30;

    private QrCodeUtil() {
    }

    /**
     * 根据传入数据创建二维码，并将数据以字节数组的形式返回
     *
     * @param content 二维码数据内容
     * @param logoPath 如果要显示LOGO，传入LOGO图片路径
     * @param len 二维码边长，支持返回[100, 1024]，其他值会被转为默认值{@link #DEFAULT_QR_CODE_LEN}
     * @param rgb 二维码颜色，传入非正整数，则会使用默认颜色：黑色
     * @param compress 如果使用LOGO图片，LOGO图片是否需要压缩，当LOGO图片尺寸较大时，应选择压缩以避免被LOGO覆盖太大面积
     * @param charset 数据内容使用的编码集，默认为UTF-8
     * @return 返回二维码数据，字节数组形式，该方法不会返回null，但可能抛出{@link BusinessException}
     */
    public static byte[] buildToBytes(String content, String logoPath, int len, int rgb, boolean compress,
            String charset) {
        return new QrCodeBuilder(content, logoPath, len, rgb, compress, charset).toBytes();
    }

    /**
     * 根据传入数据创建二维码，并将数据以字节数组的形式返回
     *
     * @param content 二维码数据内容
     * @param len 二维码边长，支持返回[100, 1024]，其他值会被转为默认值{@link #DEFAULT_QR_CODE_LEN}
     * @param rgb 二维码颜色，传入非正整数，则会使用默认颜色：黑色
     * @return 返回二维码数据，字节数组形式，该方法不会返回null，但可能抛出{@link BusinessException}
     */
    public static byte[] buildToBytes(String content, int len, int rgb) {
        return new QrCodeBuilder(content, len, rgb).toBytes();
    }

    /**
     * 根据传入数据创建二维码，并将数据以字节数组的形式返回
     *
     * @param content 二维码数据内容
     * @param len 二维码边长，支持返回[100, 1024]，其他值会被转为默认值{@link #DEFAULT_QR_CODE_LEN}
     * @return 返回二维码数据，字节数组形式，该方法不会返回null，但可能抛出{@link BusinessException}
     */
    public static byte[] buildToBytes(String content, int len) {
        return new QrCodeBuilder(content, len).toBytes();
    }

    /**
     * 根据传入数据创建二维码，并将数据以字节数组的形式返回
     *
     * @param content 二维码数据内容
     * @param logoPath 如果要显示LOGO，传入LOGO图片路径
     * @return 返回二维码数据，字节数组形式，该方法不会返回null，但可能抛出{@link BusinessException}
     */
    public static byte[] buildToBytes(String content, String logoPath) {
        return new QrCodeBuilder(content, logoPath).toBytes();
    }

    /**
     * 根据传入数据创建二维码，并将数据以字节数组的形式返回
     *
     * @param content 二维码数据内容
     * @return 返回二维码数据，字节数组形式，该方法不会返回null，但可能抛出{@link BusinessException}
     */
    public static byte[] buildToBytes(String content) {
        return new QrCodeBuilder(content).toBytes();
    }

    /**
     * 根据传入数据创建二维码，并将数据以BufferedImage的形式返回
     *
     * @param content 二维码数据内容
     * @param logoPath 如果要显示LOGO，传入LOGO图片路径
     * @param len 二维码边长，支持返回[100, 1024]，其他值会被转为默认值{@link #DEFAULT_QR_CODE_LEN}
     * @param rgb 二维码颜色，传入非正整数，则会使用默认颜色：黑色
     * @param compress 如果使用LOGO图片，LOGO图片是否需要压缩，当LOGO图片尺寸较大时，应选择压缩以避免被LOGO覆盖太大面积
     * @param charset 数据内容使用的编码集，默认为UTF-8
     * @return 返回二维码数据，BufferedImage形式，该方法不会返回null，但可能抛出{@link BusinessException}
     */
    public static BufferedImage buildToImage(String content, String logoPath, int len, int rgb, boolean compress,
            String charset) {
        return new QrCodeBuilder(content, logoPath, len, rgb, compress, charset).toImage();
    }

    /**
     * 根据传入数据创建二维码，并将数据以BufferedImage的形式返回
     *
     * @param content 二维码数据内容
     * @param len 二维码边长，支持返回[100, 1024]，其他值会被转为默认值{@link #DEFAULT_QR_CODE_LEN}
     * @param rgb 二维码颜色，传入非正整数，则会使用默认颜色：黑色
     * @return 返回二维码数据，BufferedImage形式，该方法不会返回null，但可能抛出{@link BusinessException}
     */
    public static BufferedImage buildToImage(String content, int len, int rgb) {
        return new QrCodeBuilder(content, len, rgb).toImage();
    }

    /**
     * 根据传入数据创建二维码，并将数据以BufferedImage的形式返回
     *
     * @param content 二维码数据内容
     * @param len 二维码边长，支持返回[100, 1024]，其他值会被转为默认值{@link #DEFAULT_QR_CODE_LEN}
     * @return 返回二维码数据，BufferedImage形式，该方法不会返回null，但可能抛出{@link BusinessException}
     */
    public static BufferedImage buildToImage(String content, int len) {
        return new QrCodeBuilder(content, len).toImage();
    }

    /**
     * 根据传入数据创建二维码，并将数据以BufferedImage的形式返回
     *
     * @param content 二维码数据内容
     * @param logoPath 如果要显示LOGO，传入LOGO图片路径
     * @return 返回二维码数据，BufferedImage形式，该方法不会返回null，但可能抛出{@link BusinessException}
     */
    public static BufferedImage buildToImage(String content, String logoPath) {
        return new QrCodeBuilder(content, logoPath).toImage();
    }

    /**
     * 根据传入数据创建二维码，并将数据以BufferedImage的形式返回
     *
     * @param content 二维码数据内容
     * @return 返回二维码数据，BufferedImage形式，该方法不会返回null，但可能抛出{@link BusinessException}
     */
    public static BufferedImage buildToImage(String content) {
        return new QrCodeBuilder(content).toImage();
    }

    /**
     * 根据传入数据创建二维码，并将数据以传入的文件名保存
     *
     * @param content 二维码数据内容
     * @param logoPath 如果要显示LOGO，传入LOGO图片路径
     * @param len 二维码边长，支持返回[100, 1024]，其他值会被转为默认值{@link #DEFAULT_QR_CODE_LEN}
     * @param rgb 二维码颜色，传入非正整数，则会使用默认颜色：黑色
     * @param compress 如果使用LOGO图片，LOGO图片是否需要压缩，当LOGO图片尺寸较大时，应选择压缩以避免被LOGO覆盖太大面积
     * @param charset 数据内容使用的编码集，默认为UTF-8
     * @param destFilePath 文件保存路径
     */
    public static void buildToFile(String content, String logoPath, int len, int rgb, boolean compress, String charset,
            String destFilePath) {
        BufferedImage image = buildToImage(content, logoPath, len, rgb, compress, charset);
        try {
            ImageIO.write(image, DEFAULT_FORMAT, new FileOutputStream(destFilePath));
        } catch (IOException e) {
            throw new BusinessException("qr.code.write.error", "二维码数据写入文件出错:" + destFilePath, e);
        }
    }

    public static void buildToFile(String content, String destFilePath) {
        BufferedImage image = buildToImage(content);
        try {
            ImageIO.write(image, DEFAULT_FORMAT, new FileOutputStream(destFilePath));
        } catch (IOException e) {
            throw new BusinessException("qr.code.write.error", "二维码数据写入文件出错:" + destFilePath, e);
        }
    }

    /**
     * 根据传入数据创建二维码，并将数据写入指定目录下，随机一个文件名保存
     *
     * @param content 二维码数据内容
     * @param logoPath 如果要显示LOGO，传入LOGO图片路径
     * @param len 二维码边长，支持返回[100, 1024]，其他值会被转为默认值{@link #DEFAULT_QR_CODE_LEN}
     * @param rgb 二维码颜色，传入非正整数，则会使用默认颜色：黑色
     * @param compress 如果使用LOGO图片，LOGO图片是否需要压缩，当LOGO图片尺寸较大时，应选择压缩以避免被LOGO覆盖太大面积
     * @param charset 数据内容使用的编码集，默认为UTF-8
     * @param destDir 文件保存目录
     */
    public static void buildToRandomFile(String content, String logoPath, int len, int rgb, boolean compress,
            String charset, String destDir) {
        BufferedImage image = buildToImage(content, logoPath, len, rgb, compress, charset);
        String filepath = destDir + File.separator + new Random().nextInt(999999) + "." + DEFAULT_FORMAT;
        try {
            ImageIO.write(image, DEFAULT_FORMAT, new FileOutputStream(filepath));
        } catch (IOException e) {
            throw new BusinessException("qr.code.write.error", "二维码数据写入随机文件出错:" + filepath, e);
        }
    }

    public static void buildToRandomFile(String content, String destDir) {
        BufferedImage image = buildToImage(content);
        String filepath = destDir + File.separator + new Random().nextInt(999999) + "." + DEFAULT_FORMAT;
        try {
            ImageIO.write(image, DEFAULT_FORMAT, new FileOutputStream(filepath));
        } catch (IOException e) {
            throw new BusinessException("qr.code.write.error", "二维码数据写入随机文件出错:" + filepath, e);
        }
    }

    /**
     * 根据传入数据创建二维码，并将数据写入输出流中
     *
     * @param content 二维码数据内容
     * @param logoPath 如果要显示LOGO，传入LOGO图片路径
     * @param len 二维码边长，支持返回[100, 1024]，其他值会被转为默认值{@link #DEFAULT_QR_CODE_LEN}
     * @param rgb 二维码颜色，传入非正整数，则会使用默认颜色：黑色
     * @param compress 如果使用LOGO图片，LOGO图片是否需要压缩，当LOGO图片尺寸较大时，应选择压缩以避免被LOGO覆盖太大面积
     * @param charset 数据内容使用的编码集，默认为UTF-8
     */
    public static void buildAndWrite(String content, String logoPath, int len, int rgb, boolean compress,
            String charset, OutputStream outputStream) {
        BufferedImage image = buildToImage(content, logoPath, len, rgb, compress, charset);
        try {
            ImageIO.write(image, DEFAULT_FORMAT, outputStream);
        } catch (IOException e) {
            throw new BusinessException("qr.code.write.error", "二维码数据写入文件出错", e);
        }
    }

    public static void buildAndWrite(String content, OutputStream outputStream) {
        BufferedImage image = buildToImage(content);
        try {
            ImageIO.write(image, DEFAULT_FORMAT, outputStream);
        } catch (IOException e) {
            throw new BusinessException("qr.code.write.error", "二维码数据写入文件出错", e);
        }
    }

    /**
     * 读取二维码数据，并以字节数组的形式返回
     *
     * @param filepath 二维码图片文件路径
     * @return 返回读取到的数据，该方法不会返回null，但可能抛出{@link BusinessException}
     */
    public static byte[] readWithBytes(String filepath) {
        return new QrCodeReader(new File(filepath)).readWithBytes();
    }

    /**
     * 读取二维码数据，并以字符串的形式返回
     *
     * @param filepath 二维码图片文件路径
     * @return 返回读取到的数据，该方法不会返回null，但可能抛出{@link BusinessException}
     */
    public static String readWithText(String filepath) {
        return new QrCodeReader(new File(filepath)).readWithText();
    }

    /**
     * 读取二维码数据，并以字节数组的形式返回
     *
     * @param in 二维码图片数据流
     * @return 返回读取到的数据，该方法不会返回null，但可能抛出{@link BusinessException}
     */
    public static byte[] readWithBytes(InputStream in) {
        return new QrCodeReader(in).readWithBytes();
    }

    /**
     * 读取二维码数据，并以字符串的形式返回
     *
     * @param in 二维码图片数据流
     * @return 返回读取到的数据，该方法不会返回null，但可能抛出{@link BusinessException}
     */
    public static String readWithText(InputStream in) {
        return new QrCodeReader(in).readWithText();
    }

}
