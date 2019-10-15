package cn.laoshini.dk.dao.query;

import lombok.Getter;
import lombok.Setter;

/**
 * 分页查询条件
 *
 * @author fagarine
 */
@Getter
@Setter
public class PageQueryCondition extends ListQueryCondition {

    /**
     * 页码，从1开始
     */
    private Integer pageNo;

    /**
     * 单页数据条数
     */
    private Integer pageSize;

    private String orderBy;

    private String orderSort;

    public Integer getStartOffset() {
        return (pageNo - 1) * pageSize;
    }

}
