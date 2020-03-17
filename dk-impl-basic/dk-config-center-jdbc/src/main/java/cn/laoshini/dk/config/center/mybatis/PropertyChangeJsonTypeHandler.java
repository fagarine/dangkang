package cn.laoshini.dk.config.center.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import com.alibaba.fastjson.JSON;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import cn.laoshini.dk.config.center.entity.PropertyChange;

/**
 * @author fagarine
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
public class PropertyChangeJsonTypeHandler extends BaseTypeHandler<List<PropertyChange>> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<PropertyChange> parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, JSON.toJSONString(parameter));
    }

    @Override
    public List<PropertyChange> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJson(rs.getString(columnName));
    }

    private List<PropertyChange> parseJson(String sqlJson) {
        if (null != sqlJson) {
            return JSON.parseArray(sqlJson, PropertyChange.class);
        }
        return Collections.emptyList();
    }

    @Override
    public List<PropertyChange> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }

    @Override
    public List<PropertyChange> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }
}
