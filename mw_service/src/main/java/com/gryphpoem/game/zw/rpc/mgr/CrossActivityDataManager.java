package com.gryphpoem.game.zw.rpc.mgr;

import com.gryphpoem.cross.activity.CrossRechargeActivityService;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-08-17 17:18
 */
@Component
public class CrossActivityDataManager {
    @DubboReference(check = false, lazy = true, cluster = "failfast")
    private CrossRechargeActivityService crossRechargeActivityService;
    @Autowired
    private ServerSetting serverSetting;

    @PostConstruct
    public void init() {

    }
}
