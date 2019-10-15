package cn.laoshini.dk.autoconfigure;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 键值对数据库相关配置项
 *
 * @author fagarine
 */
@Getter
@Setter
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConfigurationProperties(prefix = "dk.pair")
public class DangKangPairDaoProperties {

    /**
     * 选择键值对数据库数据访问对象的实现方式
     */
    private String dao = "DEFAULT";

    /**
     * 如果使用LevelDB作为数据库，填写数据库文件保存目录路径，可以是绝对路径，也可以使相对项目根目录的相对路径
     * <p>
     * 注意：请保证配置的目录已存在，否则系统将使用默认路径
     */
    private String levelDbFolder;

}
