package com.gryphpoem.game.zw.resource.dao.handle;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.alibaba.fastjson.JSONArray;

@Deprecated
public class AttributesTypeHandler extends BaseTypeHandler<Map<Integer, Double>> {

    @Override
    public Map<Integer, Double> getNullableResult(ResultSet arg0, String arg1) throws SQLException {
        String columnValue = arg0.getString(arg1);
        return this.getAtrributesMap(columnValue);
    }

    @Override
    public Map<Integer, Double> getNullableResult(ResultSet arg0, int arg1) throws SQLException {
        return null;
    }

    @Override
    public Map<Integer, Double> getNullableResult(CallableStatement arg0, int arg1) throws SQLException {
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement arg0, int arg1, Map<Integer, Double> arg2, JdbcType arg3)
            throws SQLException {

    }

    private Map<Integer, Double> getAtrributesMap(String columnValue) {
        Map<Integer, Double> map = new HashMap<Integer, Double>();

        if (columnValue == null) {
            return map;
        }

        JSONArray arrays = JSONArray.parseArray(columnValue);
        for (int i = 0; i < arrays.size(); i++) {
            JSONArray array = arrays.getJSONArray(i);
            int key = array.getIntValue(0);
            double value = array.getDoubleValue(1);
            map.put(key, value);
        }
        return map;
    }

}
