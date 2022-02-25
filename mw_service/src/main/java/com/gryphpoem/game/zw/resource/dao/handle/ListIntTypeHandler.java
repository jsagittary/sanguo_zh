package com.gryphpoem.game.zw.resource.dao.handle;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.gryphpoem.game.zw.core.util.LogUtil;

public class ListIntTypeHandler implements TypeHandler<List<Integer>> {
    private List<Integer> getIntegerList(String columnValue, String columnName) {
        List<Integer> list = new ArrayList<Integer>();
        if (columnValue == null || columnValue.isEmpty() || "".equals(StringUtils.trim(columnValue))) {
            return list;
        }

        try {
            JSONArray array = JSONArray.parseArray(columnValue);
            for (int i = 0; i < array.size(); i++) {
                int value = array.getIntValue(i);
                list.add(value);
            }
        } catch (Exception e) {
            LogUtil.error("解析错误: columnName:", columnName, " columnValue:", columnValue);
        }
        return list;
    }

    private String listToString(List<Integer> parameter) {
        JSONArray arrays = null;
        if (parameter == null || parameter.isEmpty()) {
            arrays = new JSONArray();
            return arrays.toJSONString();
        }

        return JSON.toJSONString(parameter);
    }

    public void setParameter(PreparedStatement ps, int i, List<Integer> parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, this.listToString(parameter));
    }

    public List<Integer> getResult(ResultSet rs, String columnName) throws SQLException {
        String columnValue = rs.getString(columnName);
        return this.getIntegerList(columnValue, columnName);
    }

    public List<Integer> getResult(ResultSet rs, int columnIndex) throws SQLException {
        return null;
    }

    public List<Integer> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return null;
    }
}
