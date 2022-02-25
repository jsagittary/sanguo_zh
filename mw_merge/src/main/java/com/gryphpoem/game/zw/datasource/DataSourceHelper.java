package com.gryphpoem.game.zw.datasource;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import com.gryphpoem.game.zw.domain.ServerDBInfo;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @ClassName DataSourceHelper.java
 * @Description 创建DataSource帮助类
 * @author QiuKun
 * @date 2018年9月5日
 */
public abstract class DataSourceHelper {

    /**
     * 创建c3p0数据连接
     * 
     * @param dbInfo
     * @return
     * @throws PropertyVetoException
     */
    public static DataSource createDataC3p0Source(ServerDBInfo dbInfo) throws PropertyVetoException {
        return createDataC3p0Source(dbInfo.getDBUrl(), dbInfo.getDbUser(), dbInfo.getDbPasswd());

    }

    public static DataSource createDataC3p0Source(String dbUrl, String dbUser, String dbPassWd)
            throws PropertyVetoException {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl(dbUrl);
        dataSource.setUser(dbUser);
        dataSource.setPassword(dbPassWd);
        dataSource.setInitialPoolSize(25);
        dataSource.setMinPoolSize(25);
        dataSource.setMaxPoolSize(250);
        dataSource.setMaxIdleTime(14400);
        dataSource.setAcquireIncrement(5);
        dataSource.setAcquireRetryAttempts(30);
        dataSource.setAcquireRetryDelay(500);
        dataSource.setNumHelperThreads(5);
        dataSource.setIdleConnectionTestPeriod(600);// 每600秒检查所有连接池中的空闲连接
        dataSource.setTestConnectionOnCheckout(false);
        dataSource.setTestConnectionOnCheckin(false);
        dataSource.setPreferredTestQuery("select 'x'");
        return dataSource;

    }

}
