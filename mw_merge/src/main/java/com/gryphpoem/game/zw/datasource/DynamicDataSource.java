package com.gryphpoem.game.zw.datasource;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.gryphpoem.game.zw.core.util.LogUtil;

/**
 * @ClassName DynamicDataSource.java
 * @Description
 * @author QiuKun
 * @date 2018年8月29日
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    // 数据源的map
    private static Map<Object, Object> dataSourceMap = new HashMap<Object, Object>();

    
    /**
     * 动态的添加连接池
     * 
     * @param dataKey
     * @param dataSource
     */
    public void addDataSocurce(String dataKey, DataSource dataSource) {
        if (dataSourceMap.containsKey(dataKey)) {
            LogUtil.debug("已经存在链接源 dataKey:", dataKey);
            return;
        }
        dataSourceMap.put(dataKey, dataSource);
        setTargetDataSources(dataSourceMap);
    }

    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);
        if (dataSourceMap.isEmpty()) { //
            dataSourceMap.putAll(targetDataSources);
        }
        afterPropertiesSet();// 必须添加该句，否则新添加数据源无法识别到
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDBType();
    }

    public static Map<Object, Object> getDataSourceMap() {
        return dataSourceMap;
    }

    public static class DataSourceContextHolder {
        private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();

        public static synchronized void setDBType(String dbType) {
            contextHolder.set(dbType);
        }

        public static String getDBType() {
            return contextHolder.get();
        }

        public static void clearDBType() {
            contextHolder.remove();
        }
    }
}
