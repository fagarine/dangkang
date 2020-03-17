package cn.laoshini.dk.qrcode;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import cn.laoshini.dk.exception.BusinessException;

import static cn.laoshini.dk.qrcode.QrCodeUtil.DEFAULT_CHARSET;

/**
 * @author fagarine
 */
class QrCodeReader {

    private File file;

    private InputStream in;

    private Result result;

    QrCodeReader(File file) {
        this.file = file;
    }

    public QrCodeReader(InputStream in) {
        this.in = in;
    }

    byte[] readWithBytes() {
        read();
        return result.getRawBytes();
    }

    String readWithText() {
        read();
        return result.getText();
    }

    private void read() {
        BufferedImage image;
        try {
            if (file != null) {
                image = ImageIO.read(file);
            } else {
                image = ImageIO.read(in);
            }
        } catch (IOException e) {
            throw new BusinessException("qr.code.error", "读取二维码图片出错:" + file, e);
        }
        if (image == null) {
            throw new BusinessException("qr.code.invalid", "未读取二维码数据:" + file);
        }

        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET, DEFAULT_CHARSET);
        try {
            result = new MultiFormatReader().decode(bitmap, hints);
        } catch (NotFoundException e) {
            throw new BusinessException("qr.code.error", "读取二维码内容出错:" + file, e);
        }
    }
}
