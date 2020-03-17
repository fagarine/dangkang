package cn.laoshini.dk.function;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import cn.laoshini.dk.util.CollectionUtil;

/**
 * @author fagarine
 */
enum FuncContainer {
    /**
     * 枚举实现单例
     */
    INSTANCE;

    private static final List<Func> FUNC_LIST = new Vector<>();

    static void add(Func<?> func) {
        FUNC_LIST.add(func);
    }

    static void refreshAll(Collection<String> changedFunctionKeys) {
        // 移除无效的可配置功能依赖
        FUNC_LIST.removeIf(func -> {
            boolean valid = func.isValid();
            if (!valid) {
                func.clear();
            }
            return !valid;
        });

        if (!CollectionUtil.isEmpty(changedFunctionKeys)) {
            for (Func func : FUNC_LIST) {
                if (changedFunctionKeys.contains(func.getFunctionKey())) {
                    func.refresh();
                }
            }
        }
    }

}
