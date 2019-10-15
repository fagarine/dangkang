package cn.laoshini.dk.constant;

import cn.laoshini.dk.common.SpringContextHolder;
import cn.laoshini.dk.function.VariousWaysManager;
import cn.laoshini.dk.manager.ResourceHolderManager;
import cn.laoshini.dk.util.ClassHelper;

/**
 * 表达式代码依赖类型枚举
 *
 * @author fagarine
 */
public enum ExpressionDependEnum {
    /**
     * 依赖：Spring托管对象
     */
    SPRING_BEAN {
        @Override
        public Object getValue(String name) {
            return SpringContextHolder.getBean(name);
        }
    },
    /**
     * 依赖：资源持有者Holder
     */
    HOLDER {
        @Override
        public Object getValue(String name) {
            return ResourceHolderManager.getHolder(name);
        }
    },
    /**
     * 依赖：可配置功能
     */
    FUNCTION {
        @Override
        public Object getValue(String name) {
            return VariousWaysManager.getCurrentImpl(name);
        }
    },
    /**
     * 依赖：class
     */
    CLASS {
        @Override
        public Object getValue(String name) {
            return ClassHelper.getClassAnywhere(name);
        }
    },
    /**
     * 依赖：临时变量
     */
    LOCAL {
        @Override
        public Object getValue(String name) {
            return null;
        }
    },
    ;

    public abstract Object getValue(String name);

}
