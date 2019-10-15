package cn.laoshini.dk.domain.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 热修复文件信息
 *
 * @author fagarine
 */
@Getter
@Setter
@ToString
public class HotfixFile {

    /**
     * 类的全限定名
     */
    private String fullClassName;

    /**
     * 类文件的绝对路径
     */
    private String filePath;

    /**
     * 文件的最后修改时间
     */
    private long lastModifyTime;
}
