package com.gryphpoem.game.zw.resource.common;

import com.gryphpoem.game.zw.core.util.LogUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Properties;

@Component
public class ServerConfig {
    // @Value("${appId}")
    // private String appId;

    @Value("${jdbcUrl}")
    private String jdbcUrl;

    @Value("${user}")
    private String user;

    @Value("${password}")
    private String password;

    // @Value("${jdbcPath}")
    // private String jdbcPath;

    @Value("${iniJdbcUrl}")
    private String iniJdbcUrl;

    @Value("${iniUser}")
    private String iniUser;

    @Value("${iniPassword}")
    private String iniPassword;

    @Value("${gmFlag}")
    private String gmFlag; // gm开关

    @Value("${registryAddress}")
    private String registryAddress;
    @Value("${registryGroup}")
    private String registryGroup;


    public ServerConfig() {
    }

    public String getIniJdbcUrl() {
        return iniJdbcUrl;
    }

    public void setIniJdbcUrl(String iniJdbcUrl) {
        this.iniJdbcUrl = iniJdbcUrl;
    }

    public String getIniUser() {
        return iniUser;
    }

    public void setIniUser(String iniUser) {
        this.iniUser = iniUser;
    }

    public String getIniPassword() {
        return iniPassword;
    }

    public void setIniPassword(String iniPassword) {
        this.iniPassword = iniPassword;
    }

    @PostConstruct
    public void init() throws IOException {
        // setAppId("1");
        LogUtil.start("load server config begin!!!");
        // String path = jdbcPath + "/" + appId + ".properties";
        // String path = "jdbc.properties";
        String path = "game.properties";
        Resource resource = new FileSystemResource(path);
        if (resource.isReadable()) {
            Properties properties = new Properties();
            try {
                properties.load(resource.getInputStream());
                jdbcUrl = properties.getProperty("jdbcUrl");
                user = properties.getProperty("user");
                password = properties.getProperty("password");
                iniJdbcUrl = properties.getProperty("iniJdbcUrl");
                iniUser = properties.getProperty("iniUser");
                iniPassword = properties.getProperty("iniPassword");
                gmFlag = properties.getProperty("gmFlag");
                registryAddress = properties.getProperty("registryAddress");
                registryGroup = properties.getProperty("registryGroup");
                // appId = properties.getProperty("appId");
                // LogUtil.start("load server config success !!! current game server --> {" + appId + "}");
            } catch (IOException e) {
                LogUtil.error("load server config exception", e);
                throw e;
            }
        } else {
            LogUtil.error("jdbc config resource can not read from out directory");
        }
        LogUtil.start(toString());
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGmFlag() {
        return gmFlag;
    }

    public void setGmFlag(String gmFlag) {
        this.gmFlag = gmFlag;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public String getRegistryGroup() {
        return registryGroup;
    }

    public void setRegistryGroup(String registryGroup) {
        this.registryGroup = registryGroup;
    }

    @Override
    public String toString() {
        return "ServerConfig [jdbcUrl=" + jdbcUrl + ", user=" + user + ", password=" + password + ", iniJdbcUrl="
                + iniJdbcUrl + ", iniUser=" + iniUser + ", iniPassword=" + iniPassword + ", gmFlag=" + gmFlag + "]";
    }

    // public String getAppId() {
    // return appId;
    // }
    //
    // public void setAppId(String appId) {
    // this.appId = appId;
    // }

    // public String getJdbcPath() {
    // return jdbcPath;
    // }
    //
    // public void setJdbcPath(String jdbcPath) {
    // this.jdbcPath = jdbcPath;
    // }
}
