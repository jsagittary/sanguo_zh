package com.gryphpoem.game.zw.gameplay.cross.serivce.map;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.cross.serivce.CrossGamePlayService;
import com.gryphpoem.game.zw.gameplay.cross.util.CrossEntity2Dto;
import com.gryphpoem.game.zw.gameplay.local.manger.aop.CrossGameMapDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.GamePb6;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGamePlayPlan;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.WarService;
import com.gryphpoem.game.zw.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Game2CrossCityService implements GmCmdService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private CrossGameMapDataMgr crossGameMapDataMgr;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private WarService warService;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private WorldService worldService;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private VipDataManager vipDataManager;
    @Autowired
    private CrossGamePlayService crossGamePlayService;

    /**
     * 获取跨服战火燎原城市信息
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public void getNewCrossCityInfo(long roleId, GamePb6.GetCrossWarFireCityInfoRq req) throws Exception {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticCrossGamePlayPlan plan = crossGamePlayService.checkConfigOpen(player, req.getFunctionId(), true);

        int cityId = req.getCityId();
        if (cityId <= 0) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "客户端请求参数错误, roleId: ", roleId);
        }

        crossGameMapDataMgr.getCrossWarFireCityInfo(CrossEntity2Dto.createGame2CrossRequest(player, plan.getKeyId()), cityId);
    }

    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {

    }
}
