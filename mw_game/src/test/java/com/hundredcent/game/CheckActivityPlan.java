package com.hundredcent.game;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.s.StaticServerList;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * @ClassName CheckActivityPlan.java
 * @Description
 * @author QiuKun
 * @date 2019年1月14日
 */

public class CheckActivityPlan {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext ac = (ClassPathXmlApplicationContext) DataResource.ac;
        StaticActivityDataMgr.init();
        LogUtil.start("----------活动数据加载完毕----------");
        StaticDataDao dao = ac.getBean(StaticDataDao.class);
        List<StaticServerList> serverList = dao.selectStaticServerList();
        for (StaticServerList ssl : serverList) {
            findSameActType(ssl);
        }
        ac.close();
        System.exit(0);
    }

    private static void findSameActType(StaticServerList ssl) {
        List<ActivityBase> activityList = StaticActivityDataMgr.getActivityBaseByServerIdAndMoldId(ssl.getServerId(),
                ssl.getOpenTime(), ssl.getActMold());
        if (!CheckNull.isEmpty(activityList)) {
            activityList.stream().collect(Collectors.toMap(ActivityBase::getActivityType, ab -> {
                return ab;
            }, (old, last) -> {
                LogUtil.debug("serverId:" + ssl.getServerId() + " oldActType:", old.getActivityType(), ", oldKeyId:",
                        old.getPlan().getKeyId(), " --- newActType:", last.getActivityType(), ", newKeyId:",
                        last.getPlan().getKeyId());
                return last;
            }));
        }
    }

}
