package com.gryphpoem.game.zw.gameplay.local.world.map;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.local.util.dto.AttackParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.BaseMapEntiyPb;
import com.gryphpoem.game.zw.pb.CommonPb.MapEntityPb;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName BaseWorldEntity.java
 * @Description
 * @author QiuKun
 * @date 2019年3月20日
 */
public abstract class BaseWorldEntity {
    protected int pos;
    protected final WorldEntityType type;

    public BaseWorldEntity(int pos, WorldEntityType type) {
        this.pos = pos;
        this.type = type;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public WorldEntityType getType() {
        return type;
    }

    public CommonPb.MapForce.Builder toMapForcePb(CrossWorldMap cMap) {
        CommonPb.MapForce.Builder builder = CommonPb.MapForce.newBuilder();
        builder.setPos(pos);
        builder.setType(type.getType());
        builder.setParam(0);
        return builder;
    }

    public MapEntityPb.Builder toDbData() {
        MapEntityPb.Builder builder = MapEntityPb.newBuilder();
        BaseMapEntiyPb.Builder baseBuilder = BaseMapEntiyPb.newBuilder();
        baseBuilder.setCfgId(getCfgId());
        baseBuilder.setPos(pos);
        baseBuilder.setEntityType(type.getType());
        builder.setBase(baseBuilder);
        return builder;
    }

    protected int getCfgId() {
        return 0;
    }

    /**
     * 攻击该点
     * 
     * @param param
     * @return
     * @throws MwException
     */
    public abstract void attackPos(AttackParamDto param) throws MwException;

    /**
     * 创建一个攻打部队
     * 
     * @param param
     * @param invokePlayer
     * @param armyState
     * @param baseEntity
     * @return
     * @throws MwException
     */
    public static Army checkAndCreateArmy(AttackParamDto param, Player invokePlayer, int armyState,
            BaseWorldEntity baseEntity) throws MwException {
        long roleId = invokePlayer.lord.getLordId();
        List<Integer> heroIdList = param.getHeroIdList();
        int pos = baseEntity.getPos();
        CrossWorldMap cMap = param.getCrossWorldMap();
        // 资源扣除
        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        rewardDataManager.checkAndSubPlayerResHasSync(invokePlayer, AwardType.RESOURCE, AwardType.Resource.FOOD,
                param.getNeedFood(), AwardFrom.ATK_POS);
        // 部队添加
        List<TwoInt> form = heroIdList.stream().map(heroId -> {
            Hero hero = invokePlayer.heros.get(heroId);
            return PbHelper.createTwoIntPb(heroId, hero.getCount());
        }).collect(Collectors.toList());

        int now = TimeHelper.getCurrentSecond();
        int marchTime = cMap.marchTime(cMap, invokePlayer, invokePlayer.lord.getPos(), pos);
        int endTime = now + marchTime;
        Army army = new Army(invokePlayer.maxKey(), armyState, pos, ArmyConstant.ARMY_STATE_MARCH, form, marchTime,
                endTime - 1, invokePlayer.getDressUp());
        army.setLordId(roleId);
        army.setOriginPos(invokePlayer.lord.getPos());
        return army;
    }

}
