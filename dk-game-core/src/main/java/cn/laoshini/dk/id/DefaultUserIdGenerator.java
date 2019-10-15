package cn.laoshini.dk.id;

import org.springframework.stereotype.Component;

import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.condition.ConditionalOnPropertyValue;
import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.exception.BusinessException;

/**
 * 用户id生成器的默认实现，该生成器生成的用户id格式如下：
 * <p>
 * +-----------+----------------+
 * +   platNo  +    sequence    +
 * +     4     +       10       +
 * +  [0~9999] + [0~9999999999] +
 * 一个完整的用户id示例：10010000201904
 * </p>
 *
 * @author fagarine
 */
@Component
@FunctionVariousWays
@ConditionalOnPropertyValue(propertyName = "dk.id.user", havingValue = Constants.DEFAULT_PROPERTY_NAME)
public class DefaultUserIdGenerator implements IUserIdGenerator {

    private static final int USER_ID_OFFSET = 10;

    private static final long USER_ID_HEAD = (long) Math.pow(10, USER_ID_OFFSET);

    private IIdIncrementer idIncrementer;

    @Override
    public long nextUserId(int platNo) throws BusinessException {
        if (idIncrementer == null) {
            synchronized (this) {
                if (idIncrementer == null) {
                    idIncrementer = newUserIdIncrementer();
                }
            }
        }

        return platNo * USER_ID_HEAD + idIncrementer.nextId();
    }
}
