package cn.laoshini.dk.qrcode;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.util.StringUtil;

import static cn.laoshini.dk.qrcode.QrCodeUtil.*;

/**
 * @author fagarine
 */
class QrCodeBuilder {

    /**
     * 二维码内容
     */
    private String content;

    /**
     * 记录LOGO图片地址，如果有的话
     */
    private String logoPath;

    /**
     * 二维码边长
     */
    private int len = DEFAULT_QR_CODE_LEN;

    /**
     * 二维码颜色（前景色）
     */
    private int rgb;

    /**
     * 是否压缩
     */
    private boolean compress = false;

    /**
     * 内容编码格式
     */
    private String charset = DEFAULT_CHARSET;

    private BitMatrix bitMatrix;

    QrCodeBuilder(String content) {
        this.content = content;
    }

    QrCodeBuilder(String content, int len) {
        this.content = content;
        this.len = len;
    }

    QrCodeBuilder(String content, int len, int rgb) {
        this.content = content;
        this.len = len;
        this.rgb = rgb;
    }

    public QrCodeBuilder(String content, String logoPath) {
        this.content = content;
        this.logoPath = logoPath;
    }

    QrCodeBuilder(String content, String logoPath, int len, int rgb, boolean compress, String charset) {
        this.content = content;
        this.logoPath = logoPath;
        this.len = len;
        this.rgb = rgb;
        this.compress = compress;
        this.charset = charset;
    }

    private void encode() {
        if (len < 100 || len > 1024) {
            len = DEFAULT_QR_CODE_LEN;
        }
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, charset);
        hints.put(EncodeHintType.MARGIN, 1);
        try {
            bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, len, len, hints);
        } catch (WriterException e) {
            throw new BusinessException("qr.code.encode.error", "生成二维码数据出错", e);
        }
    }

    byte[] toBytes() {
        encode();

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        try {
            MatrixToImageWriter.writeToStream(bitMatrix, DEFAULT_FORMAT, pngOutputStream);
            return pngOutputStream.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("qr.code.error", "二维码数据转换出错", e);
        }
    }

    BufferedImage toImage() {
        return createImage();
    }

    private BufferedImage createImage() {
        encode();

        // 二维码上色
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int color = rgb;
        if (rgb < 0) {
            color = 0;
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? color : 0xFFFFFFFF);
            }
        }

        if (StringUtil.isEmptyString(logoPath)) {
            return image;
        }
        File file = new File(logoPath);
        if (!file.exists()) {
            throw new BusinessException("logo.img.missing", "LOGO图片文件不存在:" + logoPath);
        }

        // 插入LOGO图片
        insertImage(image, file);

        return image;
    }

    /**
     * 插入LOGO
     *
     * @param source 二维码图片
     * @param logoFile LOGO图片
     */
    private void insertImage(BufferedImage source, File logoFile) {
        Image src;
        try {
            src = ImageIO.read(logoFile);
        } catch (IOException e) {
            throw new BusinessException("read.logo.error", "读取LOGO文件出错:" + logoPath, e);
        }

        int width = src.getWidth(null);
        int height = src.getHeight(null);
        if (compress) {
            int maxLen = len * LOGO_MAX_PERCENT / 100;
            maxLen = Math.min(maxLen, LOGO_MAX_LEN);
            // 压缩LOGO
            if (width > maxLen) {
                width = maxLen;
            }
            if (height > maxLen) {
                height = maxLen;
            }
            Image image = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = tag.getGraphics();
            // 绘制缩小后的图
            g.drawImage(image, 0, 0, null);
            g.dispose();
            src = image;
        }

        // 插入LOGO
        Graphics2D graph = source.createGraphics();
        int x = (len - width) / 2;
        int y = (len - height) / 2;
        graph.drawImage(src, x, y, width, height, null);
        Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
        graph.setStroke(new BasicStroke(3f));
        graph.draw(shape);
        graph.dispose();
    }

}
