package cn.laoshini.dk.id;

import org.springframework.stereotype.Component;

import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.condition.ConditionalOnPropertyValue;
import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.exception.BusinessException;

/**
 * 游戏角色id生成器的默认实现，该生成器生成的游戏角色id格式如下：
 * <p>
 * +-----------+-----------+----------------+
 * +   platNo  +  serverId +    sequence    +
 * +      4    +     4     +       10       +
 * +  [0~9999] +  [0~9999] + [0~9999999999] +
 * 一个完整的游戏角色id示例：100108880000201904
 * </p>
 *
 * @author fagarine
 */
@Component
@FunctionVariousWays
@ConditionalOnPropertyValue(propertyName = "dk.id.role", havingValue = Constants.DEFAULT_PROPERTY_NAME)
public class DefaultRoleIdGenerator implements IRoleIdGenerator {

    private static final int PLAT_OFFSET = 14;

    private static final long PLAT_HEAD = (long) Math.pow(10, PLAT_OFFSET);

    private static final int SERVER_OFFSET = 10;

    private static final long SERVER_HEAD = (long) Math.pow(10, SERVER_OFFSET);

    private IIdIncrementer idIncrementer;

    @Override
    public long nextRoleId(int platNo, int gameId, int serverId) throws BusinessException {
        if (idIncrementer == null) {
            synchronized (this) {
                if (idIncrementer == null) {
                    idIncrementer = newRoleIdIncrementer();
                }
            }
        }
        return platNo * PLAT_HEAD + serverId * SERVER_HEAD + idIncrementer.nextId();
    }
}
