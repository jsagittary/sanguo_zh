package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.service.session.SeasonService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class LoadService {

    /**
     * 重加载s_system表数据，并重新初始化相关数据
     */
    public void loadSystem() {
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
        RobotConstant.loadSystem();
        LogUtil.common("------------------RobotConstant加载完成-------------");
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

    /**
     * 加载所有配置数据
     */
    public void loadAll() {
        LogUtil.common("------------------加载数据：玩家昵称、初始属性等---------------");
        StaticIniDataMgr.init();
        LogUtil.common("------------------加载数据：玩家属性相关配置-----------------");
        StaticLordDataMgr.init();
        LogUtil.common("------------------加载数据：将领相关-----------------");
        StaticCiaDataMgr.init();
        LogUtil.common("------------------加载数据：情报部相关-----------------");
        StaticHeroDataMgr.init();
        LogUtil.common("------------------加载数据：柏林会战相关-----------------");
        StaticBerlinWarDataMgr.init();
        LogUtil.common("------------------加载数据：世界地图相关---------------");
        StaticWorldDataMgr.init("load");
        LogUtil.common("------------------加载数据：流寇相关-----------------");
        StaticBanditDataMgr.init();
        LogUtil.common("------------------加载数据：建筑相关-----------------");
        StaticBuildingDataMgr.init();
        LogUtil.common("------------------加载数据：道具、装备相关-----------------");
        StaticPropDataMgr.init();
        LogUtil.common("------------------加载数据：建筑章节关卡-----------------");
        StaticCombatDataMgr.init();
        LogUtil.common("------------------加载数据：随机奖励-----------------");
        StaticRewardDataMgr.init();
        LogUtil.common("------------------加载数据：邮件相关-----------------");
        StaticMailDataMgr.init();
        LogUtil.common("------------------加载数据：NPC数据-----------------");
        StaticNpcDataMgr.init();
        LogUtil.common("------------------加载数据：任务-----------------");
        StaticTaskDataMgr.init();
        LogUtil.common("------------------加载数据：商店-----------------");
        StaticShopDataMgr.init();
        LogUtil.common("------------------加载数据：VIP-----------------");
        StaticVipDataMgr.init();
        LogUtil.common("------------------加载数据：侦查相关-----------------");
        StaticScoutDataMgr.init();
        LogUtil.common("------------------加载数据：军团相关-----------------");
        StaticPartyDataMgr.init();
        LogUtil.common("------------------加载数据：个人资源点相关-------------");
        StaticAcquisitionDataMgr.init();
        LogUtil.common("------------------加载数据：活动-----------------");
        StaticActivityDataMgr.init();
        LogUtil.common("------------------加载数据：功能解锁条件-----------------");
        StaticFunctionDataMgr.init();
        LogUtil.common("------------------加载数据：好友师徒相关-----------------");
        StaticFriendDataMgr.init();
        LogUtil.common("------------------加载数据：推送相关-----------------");
        StaticPushDataMgr.init();
        LogUtil.common("------------------加载数据：聊天相关-----------------");
        StaticChatDataMgr.init();
        LogUtil.common("------------------加载数据：节气相关-----------------");
        StaticSolarTermsDataMgr.init();
        LogUtil.common("------------------加载数据：机器人相关-----------------");
        StaticRobotDataMgr.init();
        LogUtil.common("------------------加载数据：闪电战-----------------");
        StaticLightningWarDataMgr.init();
        LogUtil.common("------------------加载数据：勋章-----------------");
        StaticMedalDataMgr.init();
        LogUtil.common("------------------加载数据：战机相关-----------------");
        StaticWarPlaneDataMgr.init();
        LogUtil.common("------------------加载数据：教官相关-----------------");
        StaticMentorDataMgr.init();
        LogUtil.common("------------------加载数据：战斗相关-----------------");
        StaticFightDataMgr.init();
        LogUtil.common("------------------加载数据：签到模块-----------------");
        StaticSignInDataMgr.init();
        LogUtil.common("------------------加载数据：战令相关-----------------");
        StaticBattlePassDataMgr.init();
        LogUtil.common("------------------加载数据：合服banner相关-----------------");
        StaticBannerDataMgr.init();
        LogUtil.common("------------------加载数据：本地跨服相关配置-----------------");
        StaticCrossWorldDataMgr.init();
        LogUtil.common("------------------加载数据：跨服相关配置-----------------");
        StaticCrossDataMgr.init();
        LogUtil.common("------------------加载数据：system-----------------");
        loadSystem();
        LogUtil.common("------------------加载数据：跨服活动数据-----------------");
        StaticActivityCrossDataMgr.init();

        //之后加载配表的方式使用下面的方式
        List<StaticIniService> iniServices = DataResource.getBeans(StaticIniService.class);
        Collections.sort(iniServices, Comparator.comparingInt(o -> o.priority().ordinal()));
        iniServices.forEach(service -> {
            service.load();
            service.check();
        });

        LogUtil.common("------------------所有配置表数据重加载完成-----------------");
    }

    /**
     * 后台单独加载聊天、屏蔽词相关
     */
    public void loadChat() {
        LogUtil.common("------------------加载数据：聊天、屏蔽词相关-----------------");
        StaticChatDataMgr.init();
    }

    /**
     * 后台单独加载合服banner相关
     */
    public void loadBanner() {
        LogUtil.common("------------------加载数据：合服banner相关-----------------");
        StaticBannerDataMgr.init();
    }

    public void checkValid() throws MwException {
        DataResource.ac.getBean(SeasonService.class).checkStaticValid();
        DataResource.ac.getBean(SeasonTalentService.class).checkSeasonTalentConfig();
    }

}
