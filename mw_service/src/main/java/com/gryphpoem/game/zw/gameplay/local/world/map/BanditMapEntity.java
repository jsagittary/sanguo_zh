package com.gryphpoem.game.zw.gameplay.local.world.map;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.dto.AttackParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.army.AttackBanditArmy;
import com.gryphpoem.game.zw.dataMgr.StaticBanditDataMgr;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.MapForce.Builder;
import com.gryphpoem.game.zw.pb.GamePb5.*;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticBandit;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName BanditMapEntity.java
 * @Description 流寇
 * @date 2019年3月20日
 */
public class BanditMapEntity extends BaseWorldEntity {

    /**
     * 流寇的id
     */
    private final int banditId;

    public BanditMapEntity(int pos, int banditId) {
        super(pos, WorldEntityType.BANDIT);
        this.banditId = banditId;
    }

    public int getBanditId() {
        return banditId;
    }

    @Override
    public Builder toMapForcePb(CrossWorldMap cMap) {
        Builder builder = super.toMapForcePb(cMap);
        builder.setParam(banditId);
        return builder;
    }

    @Override
    public void attackPos(AttackParamDto param) throws MwException {
        long roleId = param.getInvokePlayer().roleId;
        Player player = param.getInvokePlayer();

        int now = TimeHelper.getCurrentSecond();

        if (player.getBanditCnt() >= Constant.ATTACK_BANDIT_MAX) {
            throw new MwException(GameError.BANDIT_OVER_MAX_CNT.getCode(), "攻打流寇已超过上限:", roleId, ", BanditCnt:",
                    player.getBanditCnt());
        }
        StaticBandit staticBandit = StaticBanditDataMgr.getBanditMap().get(banditId);
        if (null == staticBandit) {
            LogUtil.error("流寇id未配置, banditId:", banditId);
            throw new MwException(GameError.EMPTY_POS.getCode(), "攻打流寇，不存在, roleId:", roleId, ", pos:", pos);
        }
        // 校验流寇等级
        Integer banditLv = player.trophy.get(TrophyConstant.TROPHY_1);
        banditLv = banditLv != null ? banditLv : 0;
        if (staticBandit.getLv() > banditLv + 1) {
            throw new MwException(GameError.BANDIT_LV_ERROR.getCode(), "不能跨级打流寇, roleId:", roleId, ", pos:", pos);
        }

        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);

        // 检查补给
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD,
                param.getNeedFood(), AwardFrom.ATK_POS);

        // 部队逻辑
        List<CommonPb.TwoInt> form = param.getHeroIdList().stream().map(heroId -> {
            Hero hero = player.heros.get(heroId);
            // 改变行军状态
            hero.setState(ArmyConstant.ARMY_STATE_MARCH);
            return PbHelper.createTwoIntPb(heroId, hero.getCount());
        }).collect(Collectors.toList());
        int marchTime = param.getMarchTime();
        int endTime = now + marchTime;
        Army army = new Army(player.maxKey(), ArmyConstant.ARMY_TYPE_ATK_BANDIT, pos, ArmyConstant.ARMY_STATE_MARCH,
                form, marchTime, endTime - 1, player.getDressUp());
        army.setLordId(roleId);
        army.setTargetId(banditId);
        army.setOriginPos(player.lord.getPos());

        CrossWorldMap crossWorldMap = param.getCrossWorldMap();
        // 添加行军路线
        AttackBanditArmy attackBanditArmy = new AttackBanditArmy(army);
        attackBanditArmy.setArmyPlayerHeroState(crossWorldMap.getMapMarchArmy(), ArmyConstant.ARMY_STATE_MARCH);
        crossWorldMap.getMapMarchArmy().addArmy(attackBanditArmy);

        // 事件通知
        crossWorldMap.publishMapEvent(attackBanditArmy.createMapEvent(MapCurdEvent.CREATE));

        // 填充返回值
        AttackCrossPosRs.Builder builder = param.getBuilder();
        builder.setArmy(PbHelper.createArmyPb(army, false));

    }

    @Override
    protected int getCfgId() {
        return banditId;
    }
}
