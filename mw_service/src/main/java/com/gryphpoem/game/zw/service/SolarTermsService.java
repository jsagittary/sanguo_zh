package com.gryphpoem.game.zw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.SolarTermsDataManager;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb2.GetSolarTermsRs;
import com.gryphpoem.game.zw.pb.GamePb2.SyncSolarTermsRs;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticSolarTerms;
import com.gryphpoem.game.zw.resource.util.PbHelper;

/**
 * @ClassName SolarTermsService.java
 * @Description 节气相关
 * @author QiuKun
 * @date 2017年11月21日
 */
@Component
public class SolarTermsService {

    @Autowired
    private SolarTermsDataManager solarTermsDataManager;
    @Autowired
    private PlayerDataManager playerDataManager;

    /**
     * 获取当前节气
     * 
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetSolarTermsRs getSolarTerms(long roleId) throws MwException {
        playerDataManager.checkPlayerIsExist(roleId);
        StaticSolarTerms curSolarTerms = solarTermsDataManager.getCurSolarTerms();
        GetSolarTermsRs.Builder builder = GetSolarTermsRs.newBuilder();
        builder.setId(curSolarTerms == null ? 0 : curSolarTerms.getId());
        return builder.build();
    }

  
}
