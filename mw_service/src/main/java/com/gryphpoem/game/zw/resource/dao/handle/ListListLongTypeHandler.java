package com.gryphpoem.game.zw.resource.dao.handle;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.alibaba.fastjson.JSONArray;

public class ListListLongTypeHandler implements TypeHandler<List<List<Long>>> {
    private String listToString(List<List<Long>> parameter) {
        JSONArray arrays = null;
        if (parameter == null || parameter.isEmpty()) {
            arrays = new JSONArray();
            return arrays.toJSONString();
        }

        // arrays = JSONArray.fromObject(parameter);
        return JSONArray.toJSONString(parameter);
    }

    private List<List<Long>> getListList(String columnValue) {
        List<List<Long>> listList = new ArrayList<List<Long>>();
        if (columnValue == null || columnValue.isEmpty()) {
            return listList;
        }

        // JSONArray arrays = JSONArray.fromObject(columnValue);
        JSONArray arrays = JSONArray.parseArray(columnValue);
        for (int i = 0; i < arrays.size(); i++) {
            List<Long> list = new ArrayList<Long>();
            JSONArray array = arrays.getJSONArray(i);
            for (int j = 0; j < array.size(); j++) {
                list.add(array.getLong(j));
            }

            if (!list.isEmpty()) {
                listList.add(list);
            }
        }
        return listList;
    }

    public void setParameter(PreparedStatement ps, int i, List<List<Long>> parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, this.listToString(parameter));
    }

    public List<List<Long>> getResult(ResultSet rs, String columnName) throws SQLException {
        String columnValue = rs.getString(columnName);
        return this.getListList(columnValue);
    }

    public List<List<Long>> getResult(ResultSet rs, int columnIndex) throws SQLException {
        return null;
    }

    public List<List<Long>> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return null;
    }
}
