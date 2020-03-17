package cn.laoshini.dk.generator.id;

import cn.laoshini.dk.annotation.FunctionDependent;
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

    private long nextId = 0;

    private long maxId = 0;

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
        if (maxId == nextId) {
            String key = IdIncrementerConstant.ID_INCREMENTER_PREFIX + keySuffix;
            if (maxId == 0) {
                String value = pairDbDao.getByString(key);
                if (!StringUtil.isEmptyString(value)) {
                    maxId = nextId = Long.parseLong(value);
                }
            }
            maxId += cacheSize();
            pairDbDao.saveKeyValue(key, String.valueOf(nextId));
        }
        nextId++;

        return nextId;
    }

    @Override
    public int cacheSize() {
        return IdIncrementerConstant.PAIR_DB_CACHE_SIZE;
    }
}
