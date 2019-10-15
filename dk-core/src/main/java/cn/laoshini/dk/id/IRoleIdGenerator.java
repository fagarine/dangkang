package cn.laoshini.dk.id;

import cn.laoshini.dk.annotation.ConfigurableFunction;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.function.VariousWaysManager;

/**
 * 游戏角色id生成器（实现类实现具体的生成规则和格式）
 *
 * @author fagarine
 */
@ConfigurableFunction(key = "dk.id.role", description = "游戏角色id生成器")
public interface IRoleIdGenerator {

    /**
     * 返回下一个角色id
     *
     * @param platNo 用户来源，渠道号
     * @param gameId 角色所属游戏id
     * @param serverId 角色所属服务器id
     * @return 正整数
     */
    long nextRoleId(int platNo, int gameId, int serverId) throws BusinessException;

    /**
     * 创建并返回一个id自增器，使用缺省名称（"role_id"）
     *
     * @return 如果找不到IIdIncrementer的实现类，会抛出异常
     */
    default IIdIncrementer newRoleIdIncrementer() {
        return VariousWaysManager.getFunctionCurrentImpl(IIdIncrementer.class, "role_id");
    }
}
