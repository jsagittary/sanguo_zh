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

public class ListStringTypeHandler implements TypeHandler<List<String>> {
    private List<String> getLongList(String columnValue) {
        List<String> list = new ArrayList<String>();
        if (columnValue == null || columnValue.isEmpty()) {
            return list;
        }

        // JSONArray array = JSONArray.fromObject(columnValue);
        JSONArray array = JSONArray.parseArray(columnValue);
        for (int i = 0; i < array.size(); i++) {
            String value = array.getString(i);
            list.add(value);
        }
        return list;
    }

    private String listToString(List<String> parameter) {
        JSONArray arrays = null;
        if (parameter == null || parameter.isEmpty()) {
            arrays = new JSONArray();
            return arrays.toString();
        }

        // arrays = JSONArray.fromObject(parameter);
        return JSONArray.toJSONString(parameter);
    }

    public void setParameter(PreparedStatement arg0, int arg1, List<String> arg2, JdbcType arg3) throws SQLException {
        arg0.setString(arg1, this.listToString(arg2));
    }

    public List<String> getResult(ResultSet rs, String columnName) throws SQLException {
        String columnValue = rs.getString(columnName);
        return this.getLongList(columnValue);
    }

    public List<String> getResult(ResultSet rs, int columnIndex) throws SQLException {
        return null;
    }

    public List<String> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return null;
    }

}
