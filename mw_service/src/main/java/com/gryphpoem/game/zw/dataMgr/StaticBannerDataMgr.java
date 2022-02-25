package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticMergeBanner;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName: StaticBannerDataMgr
 * Date:      2020/12/9 10:26
 * author     shi.pei
 */
public class StaticBannerDataMgr {
    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);
    private static List<StaticMergeBanner> mergeBannerList;
    private static ServerSetting serverSetting = DataResource.ac.getBean(ServerSetting.class);

    public static void init() {
        StaticBannerDataMgr.mergeBannerList = staticDataDao.selectStaticMergeBannerList();
    }
    
    public static List<StaticMergeBanner> getMergeBannerList(){
        Date now = new Date();
        return mergeBannerList.stream().filter(smb -> DateHelper.isInTime(now, smb.getTimeBegin(), smb.getTimeEnd())&&isSelfSever(smb.getServer()))
                .collect(Collectors.toList());
    }

    /**
     * 检测是否包含当前区服
     */
    public static boolean isSelfSever(List<Integer> server) {
        int serverId = serverSetting.getServerID();
        for (int sid:server){
            if (sid == serverId){
                return true;
            }
        }
        return false;
    }
}
