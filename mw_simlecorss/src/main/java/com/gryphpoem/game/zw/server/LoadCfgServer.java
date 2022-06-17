package com.gryphpoem.game.zw.server;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticCrossDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFightDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticIniDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticMedalDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticMentorDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWarPlaneDataMgr;
import com.gryphpoem.game.zw.resource.constant.*;

/**
 * @ClassName LoadCfgService.java
 * @Description
 * @author QiuKun
 * @date 2019年4月29日
 */

public class LoadCfgServer {

    public static void loadCfg() {
        LogUtil.common("------------------加载数据：勋章-----------------");
        StaticMedalDataMgr.init();
        LogUtil.common("------------------加载数据：战机相关-----------------");
        StaticWarPlaneDataMgr.init();
        LogUtil.common("------------------加载数据：教官相关-----------------");
        StaticMentorDataMgr.init();
        LogUtil.common("------------------加载数据：NPC-----------------");
        StaticNpcDataMgr.init();
        LogUtil.common("------------------加载数据：战斗相关-----------------");
        StaticFightDataMgr.init();
        LogUtil.common("------------------加载数据：system-----------------");
        StaticCrossDataMgr.init();
        LogUtil.common("-----------------加载数据：跨服相关数据加载完成-----------");
        loadSystem();
        LogUtil.common("------------------所有配置表数据重加载完成-----------------");
    }

    public static void loadSystem() {
        StaticIniDataMgr.initSystem();
        Constant.loadSystem();
        LogUtil.common("------------------Constant加载完成------------------");
        ActParamConstant.loadSystem();
        LogUtil.common("------------------ActParamConstant加载完成-------------");
        HeroConstant.loadSystem();
        LogUtil.common("------------------HeroConstant加载完成--------------");
        MailConstant.loadSystem();
        LogUtil.common("------------------MailConstant加载完成--------------");
        ChatConst.loadSystem();
        LogUtil.common("------------------ChatConst加载完成-----------------");
        WorldConstant.loadSystem();
        LogUtil.common("------------------WorldConstant加载完成-------------");
        PartyConstant.loadSystem();
        LogUtil.common("------------------PartyConstant加载完成-------------");
        MedalConst.loadSystem();
        LogUtil.common("------------------MedalConst加载完成-------------");
        PlaneConstant.loadSystem();
        LogUtil.common("------------------PlaneConstant加载完成-------------");
        MentorConstant.loadSystem();
        LogUtil.common("------------------MentorConstant加载完成-------------");
        EquipConstant.loadSystem();
        LogUtil.common("------------------EquipConstant加载完成-------------");
        TreasureChallengePlayerConstant.loadSystem();
        LogUtil.common("------------------TreasureChallengePlayer加载完成-------------");

    }
}
