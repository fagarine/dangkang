package cn.laoshini.dk.util;

import java.net.InetSocketAddress;

import io.netty.channel.Channel;

/**
 * @author fagarine
 */
public class ChannelUtil {
    private ChannelUtil() {
    }

    public static long channel2Id(Channel channel) {
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        String ip = address.getAddress().getHostAddress();
        int port = address.getPort();
        Long id = ip2Long(ip) * 100000L + port;
        return id;
    }

    public static long ip2Long(String ip) {
        long num = 0L;
        if (ip == null) {
            return num;
        }

        try {
            // 去除字符串前的空字符
            ip = ip.replaceAll("[^0-9.]", "");
            String[] ips = ip.split("\\.");
            if (ips.length == 4) {
                num = Long.parseLong(ips[0]) << 24 + Long.parseLong(ips[1]) << 16 + Long.parseLong(ips[2]) << 8 + Long
                        .parseLong(ips[3]);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return num;
    }

}
