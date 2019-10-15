package cn.laoshini.dk.util;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * @author fagarine
 */
public class NetUtil {
    private NetUtil() {
    }

    private static final String LOCAL_IP = "127.0.0.1";

    /**
     * 远程端口是否可用
     *
     * @param ip
     * @param port
     * @return
     */
    public static boolean remotePortAble(String ip, int port) {
        try (Socket s = new Socket()) {
            SocketAddress add = new InetSocketAddress(ip, port);
            s.connect(add, 3000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 本机端口是否可用
     *
     * @param port 本机端口
     * @return
     */
    public static boolean localPortAble(int port) {
        try (Socket s = new Socket()) {
            s.bind(new InetSocketAddress(LOCAL_IP, port));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
