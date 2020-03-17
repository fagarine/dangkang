package cn.laoshini.dk.gm.constant;

/**
 * @author fagarine
 */
public class GmConstants {
    /**
     * 从GM服务器向GM后台服务器注册的HTTP请求默认URI
     */
    public static final String GM_REGISTER_URI = "/dk/server/register";
    /**
     * 从GM后台服务器发送到GM服务器的HTTP请求默认URI
     */
    public static final String GM_URI = "/dk/gm";
    /** GM消息id基础值 */
    public static final int GM_HEAD = 100;

    // *********************消息ID相关常量 Begin********************** //
    public static final int GET_MODULE_LIST_REQ = 1;
    public static final int RELOAD_MODULES_REQ = 3;
    public static final int REMOVE_MODULE_REQ = 5;
    public static final int DO_HOTFIX_REQ = 7;
    public static final int GET_HOTFIX_HISTORY_REQ = 9;
    public static final int GET_GAME_SERVER_INFO_REQ = 11;
    public static final int PAUSE_GAME_SERVER_REQ = 13;
    public static final int RELEASE_GAME_SERVER_REQ = 15;

    private GmConstants() {
    }

    // **********************消息ID相关常量 End*********************** //
}
