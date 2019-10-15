package cn.laoshini.dk.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.laoshini.dk.constant.LogLabel;

/**
 * 统一日志输出工具类
 *
 * @author fagarine
 */
public class LogUtil {
    private LogUtil() {
    }

    private static Logger MESSAGE_LOGGER = LoggerFactory.getLogger("DK_MESSAGE");
    //    private static Logger COMMON_LOGGER = LoggerFactory.getLogger("DK_COMMON");
    //    private static Logger ERROR_LOGGER = LoggerFactory.getLogger("DK_ERROR");
    //    private static Logger WARN_LOGGER = LoggerFactory.getLogger("DK_WARN");
    private static Logger COMMON_LOGGER = LoggerFactory.getLogger(LogUtil.class);
    private static Logger ERROR_LOGGER = COMMON_LOGGER;
    private static Logger WARN_LOGGER = COMMON_LOGGER;

    /**
     * 带异常的错误日志
     *
     * @param message 日志信息
     * @param t 异常信息
     */
    public static void error(String message, Throwable t) {
        if (ERROR_LOGGER.isErrorEnabled()) {
            ERROR_LOGGER.error("[" + LogLabel.ERROR.getLabel() + "] " + getClassPath() + message, t);
        }
    }

    /**
     * 带异常的错误日志
     *
     * @param t 异常信息
     * @param message 日志信息
     */
    public static void error(Throwable t, String message) {
        if (ERROR_LOGGER.isErrorEnabled()) {
            error(message, t);
        }
    }

    /**
     * 记录错误信息（使用指定标签）
     *
     * @param label 日志标签
     * @param message 日志信息
     * @param t 异常信息
     */
    public static void error(LogLabel label, String message, Throwable t) {
        if (ERROR_LOGGER.isErrorEnabled()) {
            ERROR_LOGGER.error("[" + label.getLabel() + "] " + getClassPath(4) + message, t);
        }
    }

    /**
     * 记录错误信息（使用指定标签）
     *
     * @param label 日志标签
     * @param format 日志输出格式
     * @param arguments 填入参数
     */
    public static void error(LogLabel label, String format, Object... arguments) {
        if (ERROR_LOGGER.isErrorEnabled()) {
            ERROR_LOGGER.error("[" + label.getLabel() + "] " + getClassPath(4) + format, arguments);
        }
    }

    /**
     * 记录错误信息（使用指定标签）
     *
     * @param label 日志标签
     * @param message 日志信息
     */
    public static void error(LogLabel label, String message) {
        if (ERROR_LOGGER.isErrorEnabled()) {
            ERROR_LOGGER.error("[" + label.getLabel() + "] " + getClassPath(4) + message);
        }
    }

    /**
     * 记录错误信息（默认标签）
     *
     * @param format 日志输出格式
     * @param arguments 填入参数
     */
    public static void error(String format, Object... arguments) {
        error(LogLabel.ERROR, format, arguments);
    }

    /**
     * 警告日志
     *
     * @param format 日志输出格式
     * @param arguments 填入参数
     */
    public static void warn(String format, Object... arguments) {
        if (WARN_LOGGER.isWarnEnabled()) {
            WARN_LOGGER.warn("[" + LogLabel.WARN.getLabel() + "] " + getClassPath() + format, arguments);
        }
    }

    public static void agent(String message) {
        if (COMMON_LOGGER.isInfoEnabled()) {
            COMMON_LOGGER.info("[" + LogLabel.AGENT.getLabel() + "] " + getClassPath() + message);
        }
    }

    public static void agent(String format, Object... arguments) {
        if (COMMON_LOGGER.isInfoEnabled()) {
            COMMON_LOGGER.info("[" + LogLabel.AGENT.getLabel() + "] " + getClassPath() + format, arguments);
        }
    }

    /**
     * 服务器启动日志
     *
     * @param format 日志输出格式
     * @param arguments 填入参数
     */
    public static void start(String format, Object... arguments) {
        if (COMMON_LOGGER.isInfoEnabled()) {
            COMMON_LOGGER.info("[" + LogLabel.INFO.getLabel() + "] " + getClassPath() + format, arguments);
        }
    }

    public static void trace(String format, Object... arguments) {
        if (COMMON_LOGGER.isTraceEnabled()) {
            COMMON_LOGGER.trace("[trace] " + getClassPath() + format, arguments);
        }
    }

    public static void debug(String format, Object... arguments) {
        if (COMMON_LOGGER.isDebugEnabled()) {
            COMMON_LOGGER.debug("[debug] " + getClassPath() + format, arguments);
        }
    }

    public static void debug(String message) {
        if (COMMON_LOGGER.isDebugEnabled()) {
            COMMON_LOGGER.debug("[debug] " + getClassPath() + message);
        }
    }

    /**
     * INFO级别的日志（使用指定标签）
     *
     * @param label 日志标签
     * @param format 日志输出格式
     * @param arguments 填入参数
     */
    public static void info(LogLabel label, String format, Object... arguments) {
        if (COMMON_LOGGER.isInfoEnabled()) {
            COMMON_LOGGER.info("[" + label.getLabel() + "] " + getClassPath(4) + format, arguments);
        }
    }

    /**
     * INFO级别日志（默认标签）
     *
     * @param format 日志输出格式
     * @param arguments 填入参数
     */
    public static void info(String format, Object... arguments) {
        info(LogLabel.INFO, format, arguments);
    }

    /**
     * session相关日志
     *
     * @param format 日志输出格式
     * @param arguments 填入参数
     */
    public static void session(String format, Object... arguments) {
        info(LogLabel.SESSION, format, arguments);
    }

    /**
     * session相关日志
     *
     * @param message 日志信息
     * @param t 异常信息
     */
    public static void session(String message, Throwable t) {
        info(LogLabel.SESSION, message, t);
    }

    /**
     * 消息日志（使用指定标签）
     *
     * @param label 日志标签
     * @param format 日志输出格式
     * @param arguments 填入参数
     */
    public static void message(LogLabel label, String format, Object... arguments) {
        if (MESSAGE_LOGGER.isInfoEnabled()) {
            MESSAGE_LOGGER.info("[" + label.getLabel() + "] " + getClassPath(4) + format, arguments);
        }
    }

    /**
     * 记录客户端发往服务器的协议
     *
     * @param format 日志输出格式
     * @param arguments 填入参数
     */
    public static void c2sMessage(String format, Object... arguments) {
        message(LogLabel.C2S, format, arguments);
    }

    /**
     * 记录服务器发往客户端的消息
     *
     * @param format 日志输出格式
     * @param arguments 填入参数
     */
    public static void s2cMessage(String format, Object... arguments) {
        message(LogLabel.S2C, format, arguments);
    }

    /**
     * 通用消息日志，不区分上行下行
     *
     * @param format 日志输出格式
     * @param arguments 填入参数
     */
    public static void message(String format, Object... arguments) {
        message(LogLabel.MESSAGE, format, arguments);
    }

    private static String getClassPath() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement ele = stackTraceElements[3];
        return getSimpleClassName(ele.getFileName()) + "." + ele.getMethodName() + "():" + ele.getLineNumber() + " - ";
    }

    private static String getClassPath(int stackIndex) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement ele = stackTraceElements[stackIndex];
        return getSimpleClassName(ele.getFileName()) + "." + ele.getMethodName() + "():" + ele.getLineNumber() + " - ";
    }

    private static String getSimpleClassName(String fileName) {
        int index = fileName.indexOf(".");
        if (index > 0) {
            return fileName.substring(0, index);
        }
        return fileName;
    }
}
