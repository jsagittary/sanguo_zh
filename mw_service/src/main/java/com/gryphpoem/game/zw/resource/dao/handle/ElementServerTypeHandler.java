package com.gryphpoem.game.zw.resource.dao.handle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gryphpoem.game.zw.resource.domain.s.StaticElementServer;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 跨服元素
 * @description:
 * @author: zhou jie
 * @time: 2021/11/24 17:45
 */
public class ElementServerTypeHandler extends BaseTypeHandler<List<StaticElementServer>> {

    private String obj2Str(List<StaticElementServer> elementServers) {
        if (CheckNull.isEmpty(elementServers)) {
            return "";
        }
        JSONArray arrays = new JSONArray();
        elementServers.stream().map(JSONObject::toJSONString).forEach(arrays::add);
        return arrays.toJSONString();
    }

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, List<StaticElementServer> staticElementServers, JdbcType jdbcType) throws SQLException {
        preparedStatement.setString(i, obj2Str(staticElementServers));

    }

    @Override
    public List<StaticElementServer> getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        String columnValue = resultSet.getString(columnName);
        return str2Obj(columnValue);
    }

    private List<StaticElementServer> str2Obj(String columnValue) {
        if (StringUtils.isBlank(columnValue)) {
            return Collections.emptyList();
        }

        List<StaticElementServer> elementServers = new ArrayList<>();

        if (columnValue.startsWith("[[")) {
            JSONArray arrays = JSONArray.parseArray(columnValue);
            for (int i = 0; i < arrays.size(); i++) {
                JSONArray array = arrays.getJSONArray(i);
                int serverId = array.getIntValue(0);
                int camp = array.getIntValue(1);
                elementServers.add(new StaticElementServer(serverId, camp));
            }
        } else if (columnValue.startsWith("[")) {
            JSONArray array = JSONArray.parseArray(columnValue);
            elementServers.add(new StaticElementServer(array.getInteger(0), array.getInteger(1)));
        }

        return elementServers;
    }

    @Override
    public List<StaticElementServer> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return null;
    }

    @Override
    public List<StaticElementServer> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return null;
    }
}
