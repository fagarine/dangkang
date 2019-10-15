package cn.laoshini.dk.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 游戏服务器通用配置项，目前采用从数据库读取，放弃从配置文件读取
 *
 * @author fagarine
 */
@Getter
@Setter
@ToString
@Deprecated
public class GameServerProperties {

    /**
     * 游戏唯一id
     */
    private int id;

    /**
     * 游戏名称
     */
    private String name;

    /**
     * 游戏简写，默认为"game"
     */
    private String abbreviation = "game";

    /**
     * 游戏描述信息
     */
    private String description;

    /**
     * 游戏服务器使用什么协议通信（HTTP,TCP,UDP等），默认使用TCP协议，类型参见：GameServerProtocolEnum
     */
    private String protocol = "TCP";

    /**
     * TCP游戏服务器占用端口，默认端口号：9420，仅在使用TCP通信时有效
     */
    private int port = 9420;

    /**
     * 游戏服务器使用什么消息格式通信（Json, ProtoBuf 等），默认使用JSON通信，类型参见：MessageFormatEnum
     */
    private String messageFormatter = "JSON";

    /**
     * 常量表单读取来源（excel,Json,数据库等），默认从JSON文件中读取数据，类型参见：ConstTableSourceEnum
     */
    private String constSource = "JSON";

    /**
     * 如果常量表单数据从文件读取，该值指定文件存放目录，可以是工程的相对路径，也可以是绝对路径，默认为项目根目录下的configs文件夹
     */
    private String sourceDir = "configs";
}
