package cn.laoshini.dk.id;

import cn.laoshini.dk.annotation.ConfigurableFunction;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.function.VariousWaysManager;

/**
 * 用户id生成器（实现类实现具体的生成规则和格式）
 *
 * @author fagarine
 */
@ConfigurableFunction(key = "dk.id.user", description = "用户id生成器")
public interface IUserIdGenerator {

    /**
     * 返回下一个用户id
     *
     * @param platNo 用户来源，渠道号
     * @return 正整数
     */
    long nextUserId(int platNo) throws BusinessException;

    /**
     * 创建并返回一个id自增器，使用缺省名称（"user_id"）
     *
     * @return 如果找不到IIdIncrementer的实现类，会抛出异常
     */
    default IIdIncrementer newUserIdIncrementer() {
        return VariousWaysManager.getFunctionCurrentImpl(IIdIncrementer.class, "user_id");
    }
}
