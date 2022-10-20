package com.gryphpoem.game.zw.server;

import com.gryphpoem.game.zw.core.util.LogUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-20 11:54
 */
public class ApplicationEventListener implements SpringApplicationRunListener {

    private final SpringApplication application;

    private final String[] args;

    public ApplicationEventListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }
}
