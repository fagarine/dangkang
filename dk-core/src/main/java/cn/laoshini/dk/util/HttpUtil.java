package cn.laoshini.dk.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author fagarine
 */
public class HttpUtil {
    private static final int TIME_OUT_MILLS = 5 * 1000;
    private static final Map<String, String> JSON_CONTENT_HEADER;

    static {
        Map<String, String> header = new HashMap<>(1);
        header.put("Content-Type", "application/json;charset=UTF-8");
        JSON_CONTENT_HEADER = Collections.unmodifiableMap(header);
    }

    private HttpUtil() {
    }

    /**
     * 通过HTTP GET 发送请求
     *
     * @param httpUrl URL
     * @param parameter 请求参数
     * @return 返回HTTP SERVER的处理结果，如果发送失败，返回null
     */
    public static String sendGet(String httpUrl, Map<String, String> parameter) {
        if (parameter == null || httpUrl == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : parameter.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }

            String key = entry.getKey();
            String value;
            try {
                value = URLEncoder.encode(entry.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                value = "";
            }
            sb.append(key).append('=').append(value);
        }

        String urlStr;
        if (httpUrl.lastIndexOf('?') != -1) {
            urlStr = httpUrl + '&' + sb.toString();
        } else {
            urlStr = httpUrl + '?' + sb.toString();
        }

        HttpURLConnection httpCon = null;
        String responseBody = null;
        try {
            URL url = new URL(urlStr);
            httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("GET");
            httpCon.setConnectTimeout(TIME_OUT_MILLS);
            httpCon.setReadTimeout(TIME_OUT_MILLS);

            // 开始读取返回的内容
            InputStream in = httpCon.getInputStream();
            byte[] readByte = new byte[1024];
            // 读取返回的内容
            int readCount = in.read(readByte, 0, 1024);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while (readCount != -1) {
                outputStream.write(readByte, 0, readCount);
                readCount = in.read(readByte, 0, 1024);
            }
            responseBody = new String(outputStream.toByteArray(), UTF_8);
            outputStream.close();
        } catch (Exception e) {
            LogUtil.error("Http Get Error:" + httpUrl + ", params:" + parameter, e);
        } finally {
            if (httpCon != null) {
                httpCon.disconnect();
            }
        }
        return responseBody;
    }

    /**
     * 使用HTTP POST 发送文本
     *
     * @param httpUrl 发送的地址
     * @param postBody 发送的内容
     * @return 返回HTTP SERVER的处理结果，如果发送失败，返回null
     */
    public static String sendPost(String httpUrl, String postBody) {
        return sendPost(httpUrl, postBody, "UTF-8", null);
    }

    /**
     * 使用HTTP POST 发送文本（指定字符编码类型）
     *
     * @param httpUrl 发送的地址
     * @param postBody 发送的内容
     * @param encoding 指定字符编码类型
     * @return 返回HTTP SERVER的处理结果，如果发送失败，返回null
     */
    public static String sendPostEncoding(String httpUrl, String postBody, String encoding) {
        return sendPost(httpUrl, postBody, encoding, null);
    }

    /**
     * 使用HTTP POST 发送文本（指定Http头信息）
     *
     * @param httpUrl 目的地址
     * @param postBody post的包体
     * @param headerMap 增加的Http头信息
     * @return 返回HTTP SERVER的处理结果，如果发送失败，返回null
     */
    public static String sendPost(String httpUrl, String postBody, Map<String, String> headerMap) {
        return sendPost(httpUrl, postBody, "UTF-8", headerMap);
    }

    /**
     * 使用HTTP POST 发送文本
     *
     * @param httpUrl 发送的地址
     * @param postBody 发送的内容
     * @param encoding 发送的内容的编码
     * @param headerMap 增加的Http头信息
     * @return 返回HTTP SERVER的处理结果，如果发送失败，返回null
     */
    public static String sendPost(String httpUrl, String postBody, String encoding, Map<String, String> headerMap) {
        HttpURLConnection httpCon;
        String responseBody;
        URL url;
        try {
            url = new URL(httpUrl);
        } catch (MalformedURLException e) {
            LogUtil.error("URL null", e);
            return null;
        }

        try {
            httpCon = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            LogUtil.error("openConnection exception:" + httpUrl, e);
            return null;
        }
        if (httpCon == null) {
            System.out.println("openConnection null");
            return null;
        }
        httpCon.setDoOutput(true);
        httpCon.setConnectTimeout(TIME_OUT_MILLS);
        httpCon.setReadTimeout(TIME_OUT_MILLS);
        httpCon.setDoOutput(true);
        httpCon.setUseCaches(false);
        try {
            httpCon.setRequestMethod("POST");
        } catch (ProtocolException e) {
            LogUtil.error("post request exception:" + httpUrl, e);
            return null;
        }

        if (CollectionUtil.isNotEmpty(headerMap)) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpCon.addRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        OutputStream output;
        try {
            output = httpCon.getOutputStream();
        } catch (IOException e) {
            LogUtil.error("post request exception:" + httpUrl, e);
            return null;
        }
        try {
            if (postBody != null) {
                output.write(postBody.getBytes(encoding));
            }
        } catch (IOException e) {
            LogUtil.error("post request write exception:" + httpUrl, e);
            return null;
        }
        try {
            output.flush();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // 开始读取返回的内容
        InputStream in;
        try {
            in = httpCon.getInputStream();
        } catch (IOException e) {
            LogUtil.error("post request read exception:" + httpUrl, e);
            return null;
        }
        /*
         * 这个方法可以在读写操作前先得知数据流里有多少个字节可以读取。 需要注意的是，如果这个方法用在从本地文件读取数据时，一般不会遇到问题，
         * 但如果是用于网络操作，就经常会遇到一些麻烦。
         * 比如，Socket通讯时，对方明明发来了1000个字节，但是自己的程序调用available()方法却只得到900，或者100，甚至是0，
         * 感觉有点莫名其妙，怎么也找不到原因。 其实，这是因为网络通讯往往是间断性的，一串字节往往分几批进行发送。
         * 本地程序调用available()方法有时得到0，这可能是对方还没有响应，也可能是对方已经响应了，但是数据还没有送达本地。
         * 对方发送了1000个字节给你，也许分成3批到达，这你就要调用3次available()方法才能将数据总数全部得到。
         *
         * 经常出现size为0的情况，导致下面readCount为0使之死循环(while (readCount != -1)
         * {xxxx})，出现死机问题
         */
        int size;
        try {
            size = in.available();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (size == 0) {
            size = 1024;
        }
        byte[] readByte = new byte[size];
        // 读取返回的内容
        int readCount;
        try {
            readCount = in.read(readByte, 0, size);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while (readCount != -1) {
            outputStream.write(readByte, 0, readCount);
            try {
                readCount = in.read(readByte, 0, size);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        try {
            responseBody = new String(outputStream.toByteArray(), encoding);
        } catch (UnsupportedEncodingException e) {
            return null;
        } finally {
            httpCon.disconnect();
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return responseBody;
    }

    /**
     * 使用HTTP POST 发送JSON字符串（json作为请求体发送）
     *
     * @param httpUrl 发送的地址
     * @param jsonString JSON字符串
     * @return 返回HTTP SERVER的处理结果，如果发送失败，返回null
     */
    public static String sendJsonPost(String httpUrl, String jsonString) {
        return sendPost(httpUrl, jsonString, "UTF-8", JSON_CONTENT_HEADER);
    }
}
