package cn.laoshini.dk.dao;

import java.util.ArrayList;
import java.util.Map;

import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.constant.QueryConditionKeyEnum;
import cn.laoshini.dk.dao.query.AbstractQueryCondition;
import cn.laoshini.dk.util.StringUtil;

/**
 * 键值对数据库查询条件相关功能
 *
 * @author fagarine
 */
public interface IPairConditional {

    /**
     * 匹配单层key的（两个连接符之间的字符串）正则表达式，匹配数字、英文和中文
     * <p>
     * 例如：key: table_id_name 可以匹配 table_[A-Za-z0-9\u4e00-\u9fa5]+_[A-Za-z0-9\u4e00-\u9fa5]+
     * </p>
     */
    String SINGLE_REG_EXP = "[A-Za-z0-9\\u4e00-\\u9fa5]+";

    /**
     * 将查询条件转换为键值对的key返回
     *
     * @param queryCondition 查询条件
     * @return 返回key
     */
    default String toKey(AbstractQueryCondition queryCondition) {
        Map<String, Object> filters = queryCondition.getFilters();
        return appendKeys(new ArrayList<>(filters.values()).toArray());
    }

    /**
     * 将查询条件转换为键值对的key的正则表达式返回
     *
     * @param queryCondition 查询条件
     * @return 该方法不会返回null
     */
    default String toRegExp(AbstractQueryCondition queryCondition) {
        Map<String, Object> filters = queryCondition.getFilters();
        String prefix = appendKeys(new ArrayList<>(filters.values()).toArray());
        StringBuilder sb = new StringBuilder(prefix);
        filters.remove(QueryConditionKeyEnum.TABLE_NAME.getKey());
        for (int i = 0; i < filters.size(); i++) {
            sb.append(Constants.UNDERLINE).append(SINGLE_REG_EXP);
        }
        return sb.toString();
    }

    /**
     * 顺序拼接所有数据为字符串
     *
     * @param keys
     * @return
     */
    default String appendKeys(Object... keys) {
        return StringUtil.appendKeys(keys, Constants.COLON);
    }
}
