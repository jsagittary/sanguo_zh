package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb4.ActionPointRq;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-01-21 15:11
 * @Description: 埋点Service
 * @Modified By:
 */
@Service
public class PointService {

    @Autowired
    private PlayerDataManager playerDataManager;

    /**
     * 记录埋点信息
     *
     * @param roleId
     * @param req
     */
    public void recordPoint(Long roleId, ActionPointRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int actionType = req.getActionType();
        String param = req.getParam();
        LogLordHelper.actionPoint(player, actionType, 1, param);
    }


}
