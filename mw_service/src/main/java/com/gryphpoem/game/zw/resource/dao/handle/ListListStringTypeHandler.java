package com.gryphpoem.game.zw.resource.dao.handle;

import com.alibaba.fastjson.JSONArray;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ListListStringTypeHandler implements TypeHandler<List<List<String>>> {

    private List<List<String>> getListListString(String columnValue, String columnName) {
        List<List<String>> result = new ArrayList<>();
        if (columnValue == null || columnValue.isEmpty()) {
            return result;
        }

        JSONArray arrays = JSONArray.parseArray(columnValue);
        for (int i = 0; i < arrays.size(); i++) {
            List<String> list = new ArrayList<>();
            JSONArray array = arrays.getJSONArray(i);
            for (int j = 0; j < array.size(); j++) {
                list.add(array.getString(j));
            }

            result.add(list);
        }

        return result;
    }

    private String listToString(List<List<String>> parameter) {
        JSONArray arrays = null;
        if (parameter == null || parameter.isEmpty()) {
            arrays = new JSONArray();
            return arrays.toString();
        }

        return JSONArray.toJSONString(parameter);
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, List<List<String>> parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, this.listToString(parameter));
    }

    @Override
    public List<List<String>> getResult(ResultSet rs, String columnName) throws SQLException {
        String columnValue = rs.getString(columnName);
        return this.getListListString(columnValue, columnName);
    }

    @Override
    public List<List<String>> getResult(ResultSet rs, int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public List<List<String>> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return null;
    }
}
