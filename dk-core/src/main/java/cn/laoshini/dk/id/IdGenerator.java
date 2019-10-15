package cn.laoshini.dk.id;

import cn.laoshini.dk.annotation.FunctionDependent;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.function.VariousWaysManager;

/**
 * id生成器
 *
 * @author fagarine
 */
public enum IdGenerator {
    /**
     * 枚举实现单例
     */
    INSTANCE;

    @FunctionDependent
    private IUserIdGenerator userIdGenerator;

    @FunctionDependent
    private IRoleIdGenerator roleIdGenerator;

    public static IUserIdGenerator getUserIdGenerator() {
        if (INSTANCE.userIdGenerator == null) {
            INSTANCE.userIdGenerator = VariousWaysManager.getCurrentImpl(IUserIdGenerator.class);
            if (INSTANCE.userIdGenerator == null) {
                throw new BusinessException("user.id.generator.missing", "找不到用户的ID生成器实现，请添加相关配置或依赖");
            }
        }
        return INSTANCE.userIdGenerator;
    }

    public static IRoleIdGenerator getRoleIdGenerator() {
        if (INSTANCE.roleIdGenerator == null) {
            INSTANCE.roleIdGenerator = VariousWaysManager.getCurrentImpl(IRoleIdGenerator.class);
            if (INSTANCE.roleIdGenerator == null) {
                throw new BusinessException("role.id.generator.missing", "找不到游戏角色的ID生成器实现，请添加相关配置或依赖");
            }
        }
        return INSTANCE.roleIdGenerator;
    }

    public static long nextUserId(int platNo) {
        return getUserIdGenerator().nextUserId(platNo);
    }

    public static long nextRoleId(int platNo, int gameId, int serverId) {
        return getRoleIdGenerator().nextRoleId(platNo, gameId, serverId);
    }
}
