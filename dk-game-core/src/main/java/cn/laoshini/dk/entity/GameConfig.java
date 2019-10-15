package cn.laoshini.dk.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.constant.InnerTableNameConst;
import cn.laoshini.dk.dao.TableKey;
import cn.laoshini.dk.dao.TableMapping;

/**
 * 游戏配置信息
 *
 * @author fagarine
 */
@Getter
@Setter
@ToString
@TableMapping(value = InnerTableNameConst.GAME_CONFIG, description = "游戏配置信息")
public class GameConfig {
    /**
     * 游戏唯一id
     */
    @TableKey
    private int gameId;

    /**
     * 游戏名称
     */
    private String name;

    /**
     * 游戏简写
     */
    private String abbreviation;

    /**
     * 游戏描述信息
     */
    private String description;

    /**
     * 游戏服务器使用什么协议通信（HTTP,TCP,UDP等），类型参见：GameServerProtocolEnum
     */
    private String protocol;

    /**
     * TCP游戏服务器占用端口，仅在使用TCP通信时有效
     */
    private int port;

    /**
     * 游戏服务器使用什么消息格式通信（Json, ProtoBuf 等），默认使用JSON通信，类型参见：MessageFormatEnum
     */
    private String messageFormatter;

    /**
     * 常量表单读取来源（excel,Json,数据库等），默认从JSON文件中读取数据，类型参见：ConstTableSourceEnum
     */
    private String constSource;

    /**
     * 如果常量表单数据从文件读取，该值指定文件存放目录，可以是工程的相对路径，也可以是绝对路径，默认为项目根目录下的configs文件夹
     */
    private String sourceDir = Constants.CONST_FILE_DIR;

    /**
     * 游戏状态
     */
    private int status;
}
