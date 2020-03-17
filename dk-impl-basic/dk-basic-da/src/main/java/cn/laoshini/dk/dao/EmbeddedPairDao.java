package cn.laoshini.dk.dao;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.cache.DefaultCacheImpl;
import cn.laoshini.dk.condition.ConditionalOnPropertyMissing;
import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.exception.DaoException;
import cn.laoshini.dk.serialization.IDataSerializable;
import cn.laoshini.dk.serialization.StringDataSerialization;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * 当康系统实现的嵌入式键值对数据库访问对象，使用LevelDB实现，仅在不使用关系数据库的情况下创建实例
 *
 * @author fagarine
 */
@Order
@Component
@FunctionVariousWays
@ConditionalOnPropertyMissing(prefix = "dk.rdb", name = { "url", "username" })
public class EmbeddedPairDao implements IPairDbDao {

    @Value("${dk.pair.level-db-folder:/levelDB}")
    private String dbFolder;

    /**
     * 正则表达式的缓存
     */
    private DefaultCacheImpl<String, Pattern> regExpCache = new DefaultCacheImpl<>();

    private IDataSerializable serialization;

    private DB db;

    private boolean initialized;

    public void initDB() {
        serialization = new StringDataSerialization();

        // 尝试打开数据库连接
        openLevelDB();
    }

    /**
     * 打开数据库连接
     */
    private void openLevelDB() {
        if (StringUtil.isNotEmptyString(getDbFolder())) {
            // 首先将文件夹路径当作绝对路径看待，如果不存在，则当作项目根目录下的相对路径来看待，如果还是不存在，则使用系统默认路径
            File dbFile = new File(getDbFolder());
            if (!dbFile.exists() || dbFile.isFile()) {
                LogUtil.debug("没有找到LevelDB的文件目录，尝试作为相对路径查找, path:{}", getDbFolder());
                String path = System.getProperty("user.dir") + "/" + getDbFolder();
                dbFile = new File(path);
                if (!dbFile.exists() || dbFile.isFile()) {
                    dbFolder = getDefaultLevelDBDir();
                    LogUtil.debug("仍旧不是有效的LevelDB文件目录，使用系统默认路径, path:{}, 默认目录:{}", path, getDbFolder());
                } else {
                    dbFolder = dbFile.getAbsolutePath();
                }
            }
        } else {
            // 当没有配置LevelDB的目录时，使用默认目录路径
            dbFolder = getDefaultLevelDBDir();
            LogUtil.debug("用户没有配置LevelDB的文件目录，使用系统默认路径, path:{}", getDbFolder());
        }

        DBFactory factory = new Iq80DBFactory();
        Options options = new Options();
        options.createIfMissing(true);
        try {
            //folder 是db存储目录
            DB db = factory.open(new File(getDbFolder()), options);
            setDb(db);
        } catch (IOException e) {
            throw new DaoException("levelDB.open.error", String.format("LevelDB数据库打开出错, folder:%s", getDbFolder()), e);
        }
    }

    /**
     * 获取LevelDB缺省路径
     *
     * @return
     */
    private String getDefaultLevelDBDir() {
        return System.getProperty("user.dir") + Constants.DEFAULT_LEVEL_DB_DIR + "/";
    }

    /**
     * 保存数据
     *
     * @param key key
     * @param value value
     */
    public void save(String key, Object value) {
        checkDBIsConnect();
        getDb().put(keyToBytes(key), getValueSerialization().toBytes(value));
    }

    /**
     * 保存数据
     *
     * @param value 保存数据
     * @param keys 使用该值所有数据拼接后的字符串作为key
     */
    public void save(Object value, Object... keys) {
        save(StringUtil.appendKeys(keys), value);
    }

    @Override
    public void saveKeyValue(Object key, Object value) {
        LogUtil.debug("LevelDB保存数据, key:{}, value:{}", key, value);
        save(String.valueOf(key), value);
    }

    @Override
    public void saveMap(Map<String, Object> map) {
        checkDBIsConnect();
        // 批量保存，批量修改
        DB db = getDb();
        WriteBatch writeBatch = db.createWriteBatch();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            writeBatch.put(keyToBytes(entry.getKey()), getValueSerialization().toBytes(entry.getValue()));
        }
        db.write(writeBatch);
    }

    /**
     * 获取数据对象的值，以二进制形式返回
     *
     * @param key key
     * @return 返回二进制格式的数据
     */
    @Override
    public byte[] getBytes(String key) {
        checkDBIsConnect();
        return getDb().get(keyToBytes(key));
    }

    @Override
    public <T> Map<String, T> selectByRegExp(String regExp, Class<T> toType) {
        Pattern pattern = getPattern(regExp);
        Map<String, T> result = new HashMap<>();
        DBIterator its = getDb().iterator();
        its.forEachRemaining((entry) -> {
            String key = bytesToKey(entry.getKey());
            if (key != null && pattern.matcher(key).find()) {
                try {
                    result.put(key, getValueSerialization().toAssignedTypeObject(entry.getValue(), toType));
                } catch (Exception e) {
                    LogUtil.error("byte数组转换为对象出错, key:" + key, e);
                }
            }
        });
        return result;
    }

    private Pattern getPattern(String regExp) {
        Pattern pattern = regExpCache.get(regExp);
        if (pattern == null) {
            pattern = Pattern.compile(regExp);
            regExpCache.put(regExp, pattern);
        }
        return pattern;
    }

    @Override
    public void deleteByKey(String key) {
        getDb().delete(keyToBytes(key));
    }

    /**
     * 数据库是否已连接
     *
     * @return 返回数据库是否已连接
     */
    public boolean connected() {
        return getDb() != null;
    }

    /**
     * 检查数据库是否已连接成功，如果没有，则抛出异常
     */
    public void checkDBIsConnect() {
        if (!initialized) {
            initialized = true;
            initDB();
        }

        if (!connected()) {
            throw new DaoException("db.not.connect", "数据库未连接成功");
        }
    }

    /**
     * 关闭数据库连接
     */
    public void closeDB() {
        if (!connected()) {
            return;
        }

        try {
            getDb().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getString(String key) {
        return (String) selectByKey(key, String.class);
    }

    @Override
    public IDataSerializable getValueSerialization() {
        return serialization;
    }

    public void setSerialization(IDataSerializable serialization) {
        this.serialization = serialization;
    }

    public String getDbFolder() {
        return dbFolder;
    }

    public void setDbFolder(String dbFolder) {
        this.dbFolder = dbFolder;
    }

    public DB getDb() {
        return db;
    }

    public void setDb(DB db) {
        this.db = db;
    }
}
