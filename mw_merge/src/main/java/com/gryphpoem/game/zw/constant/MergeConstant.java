package com.gryphpoem.game.zw.constant;

import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * @ClassName MergeConstant.java
 * @Description 合服使用的常量
 * @author QiuKun
 * @date 2018年9月8日
 */
public interface MergeConstant {
    public String DATASOURCE_KEY_PREFIX_SRC = "merge_";

    public String DATASOURCE_KEY_PREFIX_DST = "sanguo_merge_";

    public String CREATE_TABLE_DDL_COLUMN = "Create Table";
    public String CREATE_TABLE_NAME_COLUMN = "Table";

    public String SERVER_SETTING_TABLE_NAME = "s_server_setting";

    // &rewriteBatchedStatements=true&allowMultiQueries=true
    public String MYSQL_PARAMS = "?characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull";
    /*-------------------------数据源的key值------------------------------*/
    /** 默认的DATASOURCE */
    public String MASTER_DATASOURCE_KEY = "masterDataSource";

    /**
     * 获取查询的 DatasourceKey
     * 
     * @param serverId
     * @return
     */
    public static String getSrcDatasourceKey(int serverId) {
        return DATASOURCE_KEY_PREFIX_SRC + serverId;
    }

    /**
     * 获取目标的 DatasourceKey
     * 
     * @param serverId
     * @return
     */
    public static String getDstDatasourceKey(int serverId) {
        int currentDay = TimeHelper.getCurrentDay();
        return DATASOURCE_KEY_PREFIX_DST + currentDay + "_" + serverId;
    }

}
