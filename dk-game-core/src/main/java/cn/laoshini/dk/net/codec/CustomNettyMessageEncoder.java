package cn.laoshini.dk.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.net.msg.INettyDto;
import cn.laoshini.dk.net.msg.ReqNettyCustomMessage;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.ZlibUtil;

/**
 * Netty自定义消息类型编码器
 * <p>
 * 响应协议：长度 + 消息id ＋ 消息内容（字节数组，可能会压缩）
 * </p>
 *
 * @author fagarine
 */
public class CustomNettyMessageEncoder extends MessageToByteEncoder<ReqNettyCustomMessage<INettyDto>>
        implements INettyMessageEncoder<ReqNettyCustomMessage<INettyDto>> {
    /** 消息体的大小超过512KB则压缩 */
    private static final int COMPRESS_BYTES = 512000;

    @Override
    protected void encode(ChannelHandlerContext ctx, ReqNettyCustomMessage<INettyDto> resMsg, ByteBuf out) {
        // 占位
        out.writeInt(0);
        out.writeInt(0);

        boolean zip = writeAndCompressMessage(resMsg, out);
        // 消息长度
        int length = out.readableBytes() - Integer.BYTES;

        /*
         * 长度的最高位：1代表压缩，0：未压缩，如果压缩将最高位设置为1
         */
        if (zip) {
            length |= 0x80000000;
        }
        // 长度
        out.setInt(0, length);
        // 消息id
        out.setInt(4, resMsg.getId());

        LogUtil.s2cMessage("返回消息内容[{}]", resMsg);
    }

    private boolean writeAndCompressMessage(ReqNettyCustomMessage<INettyDto> resMsg, ByteBuf out) {
        // 消息体的字节缓冲（默认大小为256字节）
        ByteBuf msgBuf = Unpooled.buffer();
        // 写入消息体内容
        resMsg.write(msgBuf);

        // 超过超度，压缩
        if (msgBuf.readableBytes() > COMPRESS_BYTES) {
            byte[] dst = new byte[msgBuf.readableBytes()];
            msgBuf.getBytes(0, dst);
            out.writeBytes(ZlibUtil.compress(dst));
            return true;
        } else {
            out.writeBytes(msgBuf);
        }

        // 返回是否zip压缩
        return false;
    }

    @Override
    public ByteBuf encode(ReqNettyCustomMessage<INettyDto> resMsg, GameSubject subject) {
        ByteBuf out = Unpooled.buffer();
        // 占位
        out.writeInt(0);
        out.writeInt(0);

        boolean zip = writeAndCompressMessage(resMsg, out);
        // 消息长度
        int length = out.readableBytes() - Integer.BYTES;

        /*
         * 长度的最高位：1代表压缩，0：未压缩，如果压缩将最高位设置为1
         */
        if (zip) {
            length |= 0x80000000;
        }
        // 长度
        out.setInt(0, length);
        // 消息id
        out.setInt(4, resMsg.getId());

        LogUtil.s2cMessage("返回消息内容[{}]", resMsg);

        return out;
    }
}
