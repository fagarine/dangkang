package cn.laoshini.dk.constant;

/**
 * @author fagarine
 */
public class Constants {
    public static final String SUCCESS = "SUCCESS";
    public static final String DEFAULT_PROPERTY_NAME = "DEFAULT";
    public static final byte[] EMPTY_BYTES = new byte[0];
    /**
     * 文件后缀名：jar
     */
    public static final String JAR_FILE_SUFFIX = ".jar";
    /**
     * 文件后缀名：zip
     */
    public static final String ZIP_FILE_SUFFIX = ".zip";
    /**
     * JAVA源文件后缀名
     */
    public static final String JAVA_FILE_SUFFIX = ".java";
    /**
     * Class文件后缀名
     */
    public static final String CLASS_FILE_SUFFIX = ".class";
    /**
     * 逗号分隔符
     */
    public static final String SEPARATOR_COMMA = ",";
    /**
     * 单引号
     */
    public static final String SINGLE_QUOTES = "'";
    /**
     * 下划线
     */
    public static final String UNDERLINE = "_";
    /**
     * 冒号
     */
    public static final String COLON = ":";
    /**
     * 换行符
     */
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    /**
     * 外置模块文件默认存放目录（相对项目根目录）
     */
    public static final String MODULE_ROOT_DIR = "/modules";
    /**
     * 存放热修复类文件的根目录
     */
    public static final String HOTFIX_DIR = "/hotfix";
    /**
     * 常量表单文件默认存放路径（相对项目根目录）
     */
    public static final String CONST_FILE_DIR = "/configs";
    /**
     * 如果使用了LevelDB，却没有配置数据库路径，使用该缺省目录路径（相对项目根目录）
     */
    public static final String DEFAULT_LEVEL_DB_DIR = "/levelDB";
    /**
     * yaml配置文件的后缀名
     */
    public static final String[] YAML_FILE_SUFFIX = { ".yaml", ".yml" };
    /**
     * 当康系统包路径前缀
     */
    public static final String DK_PACKAGE_PREFIX = "cn.laoshini.dk";
    /**
     * 记录在资源中心的，包扫描路径对应的key
     */
    public static final String PACKAGE_PREFIXES_RESOURCE_KEY = "PACKAGE PREFIXES RESOURCE KEY";
    /**
     * 记录在资源中心的，Spring配置文件路径对应的key
     */
    public static final String SPRING_LOCATIONS_RESOURCE_KEY = "SPRING LOCATIONS RESOURCE KEY";
    /**
     * 记录在资源中心的，程序启动项参数对应的key
     */
    public static final String START_ARGS_RESOURCE_KEY = "START ARGS RESOURCE KEY";
    /**
     * 记录在资源中心的，配置项文件路径参数对应的key
     */
    public static final String PROPERTY_RESOURCE_KEY = "PROPERTY RESOURCE KEY";
    /**
     * 记录在资源中心的，配置中心客户端配置文件对应的key
     */
    public static final String CONFIG_CLIENT_FILE_RESOURCE_KEY = "CONFIG CLIENT FILE RESOURCE KEY";
    /**
     * 消息处理时间阈值，超过该值将打印日志，单位：毫秒
     */
    public static final int HANDLER_RESPONSE_LIMIT = 20;
    /**
     * 游戏服进程运行环境默认值，同时也是配置中心记录游戏服label的默认值
     */
    public static final String DEFAULT_GAME_SERVER_ENV = "master";

    private Constants() {
    }

}
