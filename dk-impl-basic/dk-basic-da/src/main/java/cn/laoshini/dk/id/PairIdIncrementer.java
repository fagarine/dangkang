package cn.laoshini.dk.id;

import cn.laoshini.dk.annotation.FunctionDependent;
import cn.laoshini.dk.constant.DaConstant;
import cn.laoshini.dk.dao.IPairDbDao;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.util.StringUtil;

/**
 * 使用键值对数据库表实现id自增器
 *
 * @author fagarine
 */
class PairIdIncrementer implements IDbIdIncrementer {

    @FunctionDependent
    private IPairDbDao pairDbDao;

    private String keySuffix;

    PairIdIncrementer(String keySuffix) {
        this.keySuffix = keySuffix;
    }

    @Override
    public String idName() {
        return keySuffix;
    }

    @Override
    public long nextId() {
        try {
            return incrementAndGet();
        } catch (Exception e) {
            throw new BusinessException("id.next.fail", "通关键值对数据库实现id自增失败:" + keySuffix, e);
        }
    }

    private long incrementAndGet() {
        String key = DaConstant.ID_INCREMENTER_PREFIX + keySuffix;
        String value = pairDbDao.getByString(key);

        long nextId = 1;
        if (!StringUtil.isEmptyString(value)) {
            nextId += Long.parseLong(value);
        }

        pairDbDao.saveKeyValue(key, String.valueOf(nextId));
        return nextId;
    }
}
