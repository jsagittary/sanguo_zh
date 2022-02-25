package com.gryphpoem.game.zw.gameplay.local.world.battle;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.local.util.dto.AttackParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.army.BaseArmy;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.WorldService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName AbsCommonBattle.java
 * @Description
 * @author QiuKun
 * @date 2019年4月10日
 */
public abstract class AbsCommonBattle extends BaseMapBattle {

    public AbsCommonBattle(Battle battle) {
        super(battle);
    }

    protected BaseArmy createBaseArmy(AttackParamDto param, int now, int armyType) {
        Player player = param.getInvokePlayer();
        int pos = getBattle().getPos();
        int marchTime = param.getMarchTime();
        List<CommonPb.TwoInt> form = param.getHeroIdList().stream().map(heroId -> {
            Hero hero = player.heros.get(heroId);
            return PbHelper.createTwoIntPb(heroId, hero.getCount());
        }).collect(Collectors.toList());
        Army army = new Army(player.maxKey(), armyType, pos, ArmyConstant.ARMY_STATE_MARCH, form, marchTime,
                now + marchTime, player.getDressUp());
        army.setBattleId(getBattleId());
        army.setLordId(player.lord.getLordId());
        army.setTarLordId(battle.getDefencerId());
        army.setBattleTime(battle.getBattleTime());
        army.setOriginPos(player.lord.getPos());
        // 添加行军路线
        BaseArmy baseArmy = BaseArmy.baseArmyFactory(army);
        CrossWorldMap crossWorldMap = param.getCrossWorldMap();
        baseArmy.setArmyPlayerHeroState(crossWorldMap.getMapMarchArmy(), ArmyConstant.ARMY_STATE_MARCH);
        crossWorldMap.getMapMarchArmy().addArmy(baseArmy);
        return baseArmy;
    }

    protected void addBattleRole(AttackParamDto param,AwardFrom from) {
        Player player = param.getInvokePlayer();
        long roleId = player.lord.getLordId();
        int camp = player.lord.getCamp();
        if (camp == battle.getAtkCamp()) {
            WorldService worldService = DataResource.ac.getBean(WorldService.class);
            worldService.removeProTect(player,from,battle.getPos());
            battle.getAtkRoles().add(roleId);
        } else if (camp == battle.getDefCamp()) {
            battle.getDefRoles().add(roleId);
        }
        battle.updateArm(camp, param.getArmCount());
    }

    protected void checkAndSubFood(AttackParamDto param) throws MwException {
        Player player = param.getInvokePlayer();
        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        // 检测战斗消耗
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD,
                param.getNeedFood(), AwardFrom.ATK_POS);
    }
}
