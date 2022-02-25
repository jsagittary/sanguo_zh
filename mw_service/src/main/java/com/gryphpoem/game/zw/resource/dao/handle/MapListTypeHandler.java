package com.gryphpoem.game.zw.resource.dao.handle;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.alibaba.fastjson.JSONArray;

public class MapListTypeHandler extends BaseTypeHandler<Map<Integer, List<Integer>>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<Integer, List<Integer>> parameter,
            JdbcType jdbcType) throws SQLException {

    }

    @Override
    public Map<Integer, List<Integer>> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String columnValue = rs.getString(columnName);
        return this.getMapList(columnValue);
    }

    @Override
    public Map<Integer, List<Integer>> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Map<Integer, List<Integer>> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return null;
    }

    private Map<Integer, List<Integer>> getMapList(String columnValue) {
        Map<Integer, List<Integer>> mapList = new HashMap<Integer, List<Integer>>();
        if (columnValue == null || columnValue.isEmpty()) {
            return mapList;
        }

        // JSONArray arrays = JSONArray.fromObject(columnValue);
        JSONArray arrays = JSONArray.parseArray(columnValue);
        for (int i = 0; i < arrays.size(); i++) {
            List<Integer> list = new ArrayList<Integer>();
            JSONArray array = arrays.getJSONArray(i);
            for (int j = 0; j < array.size(); j++) {
                list.add(array.getIntValue(j));
            }

            if (!list.isEmpty()) {
                mapList.put(list.get(0), list);
            }
        }

        return mapList;
    }

}
