package com.gryphpoem.game.zw.gameplay.local.world.map;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.util.dto.AttackParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.army.BaseArmy;
import com.gryphpoem.game.zw.gameplay.local.world.army.CollectArmy;
import com.gryphpoem.game.zw.gameplay.local.world.army.MapMarch;
import com.gryphpoem.game.zw.gameplay.local.world.army.PlayerArmy;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.MapEntityPb;
import com.gryphpoem.game.zw.pb.CommonPb.MapForce.Builder;
import com.gryphpoem.game.zw.pb.CommonPb.MapMinePb;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb5.AttackCrossPosRs;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticMine;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.Guard;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.HeroService;
import com.gryphpoem.game.zw.service.MineService;
import com.gryphpoem.game.zw.service.WorldService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName MineMapEntity.java
 * @Description
 * @date 2019???3???20???
 */
public class MineMapEntity extends BaseWorldEntity {

    private int mineId;
    private int lv;
    private int mineType;
    // ????????????
    private int remainRes;
    // ???????????????
    private Guard guard;

    /**
     * ?????????????????????
     *
     * @return AreaMine
     */
    public CommonPb.AreaMine toAreaForcePb() {
        CommonPb.AreaMine.Builder builder = CommonPb.AreaMine.newBuilder();
        builder.setPos(pos);
        return builder.build();
    }

    public MineMapEntity(int pos, int mineId, int lv, int mineType, int remainRes) {
        super(pos, WorldEntityType.MINE);
        this.mineId = mineId;
        this.lv = lv;
        this.mineType = mineType;
        this.remainRes = remainRes;
    }

    public int getRemainRes() {
        return remainRes;
    }

    public void setRemainRes(int remainRes) {
        this.remainRes = remainRes;
    }

    public int getMineId() {
        return mineId;
    }

    public void setMineId(int mineId) {
        this.mineId = mineId;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getMineType() {
        return mineType;
    }

    public void setMineType(int mineType) {
        this.mineType = mineType;
    }

    public Guard getGuard() {
        return guard;
    }

    public void setGuard(Guard guard) {
        this.guard = guard;
    }

    public StaticMine getCfgMine() {
        return StaticWorldDataMgr.getMineMap().get(mineId);
    }

    public boolean hasGuard() {
        return guard != null;
    }

    @Override
    public MapEntityPb.Builder toDbData() {
        CommonPb.MapEntityPb.Builder dbData = super.toDbData();
        MapMinePb.Builder mineBuilder = MapMinePb.newBuilder();
        mineBuilder.setLv(this.lv);
        mineBuilder.setMineType(this.mineType);
        mineBuilder.setRemainRes(this.remainRes);
        dbData.setMine(mineBuilder);
        return dbData;
    }

    @Override
    protected int getCfgId() {
        return mineId;
    }

    @Override
    public Builder toMapForcePb(CrossWorldMap cMap) {
        Builder builder = super.toMapForcePb(cMap);
        builder.setParam(mineId);
        if (hasGuard()) {// ????????? ????????????????????????????????????
            builder.setCollect(PbHelper.createCollectBuilder(guard));
        }
        builder.setResource(calcRemainRes());
        return builder;
    }

    @Override
    public void attackPos(AttackParamDto param) throws MwException {
        Player invokePlayer = param.getInvokePlayer();
        long roleId = invokePlayer.roleId;
        CrossWorldMap cmap = param.getCrossWorldMap();
        // ????????????????????????
        checkCollectArmy(roleId, cmap);
        WorldService worldService = DataResource.ac.getBean(WorldService.class);
        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        boolean mineHasPlayer = guard != null;
        List<List<Integer>> needCost = worldService.combineAttackMineCost(param.getNeedFood(), mineHasPlayer);
        rewardDataManager.checkAndSubPlayerRes(invokePlayer, needCost, AwardFrom.ATK_POS); // ?????????????????????
        if (mineHasPlayer) { // ????????????
            if (guard.getPlayer().roleId != roleId) {
                worldService.removeProTect(invokePlayer, AwardFrom.COLLECT_WAR, pos); // ???????????????
            }
        }
        // ????????????
        List<TwoInt> form = param.getHeroIdList().stream().map(heroId -> {
            Hero hero = invokePlayer.heros.get(heroId);
            return PbHelper.createTwoIntPb(heroId, hero.getCount());
        }).collect(Collectors.toList());

        int now = TimeHelper.getCurrentSecond();
        int marchTime = cmap.marchTime(cmap, invokePlayer, invokePlayer.lord.getPos(), pos);
        int endTime = now + marchTime;
        Army army = new Army(invokePlayer.maxKey(), ArmyConstant.ARMY_TYPE_COLLECT, pos, ArmyConstant.ARMY_STATE_MARCH,
                form, marchTime, endTime - 1, invokePlayer.getDressUp());
        army.setLordId(roleId);
        army.setTargetId(mineId);
        army.setOriginPos(invokePlayer.lord.getPos());

        // ??????????????????
        CollectArmy collectArmy = new CollectArmy(army);
        collectArmy.setArmyPlayerHeroState(cmap.getMapMarchArmy(), ArmyConstant.ARMY_STATE_MARCH);
        cmap.getMapMarchArmy().addArmy(collectArmy);
        cmap.publishMapEvent(collectArmy.createMapEvent(MapCurdEvent.CREATE),
                MapEvent.mapEntity(invokePlayer.lord.getPos(), MapCurdEvent.UPDATE));// ????????????

        // ???????????????
        AttackCrossPosRs.Builder builder = param.getBuilder();
        builder.setArmy(PbHelper.createArmyPb(army, false));
    }

    /**
     * ????????????????????????
     *
     * @param roleId
     * @param cmap
     * @throws MwException
     */
    void checkCollectArmy(long roleId, CrossWorldMap cmap) throws MwException {
        PlayerArmy playerArmy = cmap.getMapMarchArmy().getPlayerArmyMap().get(roleId);
        if (playerArmy != null) {
            int cnt = 0;
            for (BaseArmy baseArmy : playerArmy.getArmy().values()) {
                Army army = baseArmy.getArmy();
                if (army.getType() == ArmyConstant.ARMY_TYPE_COLLECT
                        || army.getType() == ArmyConstant.ARMY_TYPE_COLLECT_SUPERMINE) {
                    cnt++;
                }
                if (army.getTarget() == pos && army.getState() != ArmyConstant.ARMY_STATE_RETREAT) {
                    throw new MwException(GameError.ALREADY_COLLECT_HERO.getCode(), "???????????????????????????????????????, roleId:", roleId);
                }
            }
            if (cnt >= WorldConstant.MINE_MAX_CNT) {
                throw new MwException(GameError.MINE_MAX_NUM.getCode(), "??????????????????????????????, roleId:", roleId, ", pos:", pos);
            }
        }
    }

    /**
     * ????????????,???????????????army???,???????????????????????????,?????????????????????????????????,????????????????????????
     */
    public CommonPb.MailCollect settleCollect(CrossWorldMap cMap) {
        if (guard == null) {
            return null;
        }
        int now = TimeHelper.getCurrentSecond();
        int add = calcCollect(now);
        StaticMine staticMine = StaticWorldDataMgr.getMineMap().get(mineId);
        MineService mineService = DataResource.ac.getBean(MineService.class);
        HeroService heroService = DataResource.ac.getBean(HeroService.class);
        // ????????????????????? ?????? ???????????? ???????????????
        Player player = guard.getPlayer();
        Army army = guard.getArmy();
        final Date startDate = new Date(army.getBeginTime() * 1000L);
        add = mineService.calcGainCollectEffectCnt(player, add, staticMine.getMineType(), startDate, army);
        Award award = PbHelper.createAwardPb(staticMine.getReward().get(0).get(0), staticMine.getReward().get(0).get(1),
                add);
        List<CommonPb.Award> grab = new ArrayList<>(1);
        grab.add(award);
        army.setGrab(grab);
        int collectTime = now - army.getBeginTime(); // ???????????????
        army.setCollectTime(collectTime);
        remainRes -= add; // ?????????????????????
        // ????????????
        MapMarch mapMarchArmy = cMap.getMapMarchArmy();
        BaseArmy baseArmy = mapMarchArmy.getBaseArmyByLordIdAndKeyId(army.getLordId(), army.getKeyId());
        if (baseArmy != null) {
            baseArmy.normalRetreatArmy(mapMarchArmy);
        }
        // ???????????????????????????????????????
        int canCollectTime = MineService.calcMineGuardTime(staticMine, remainRes);
        if (canCollectTime < Constant.MINE_RM_MIN_TIME) {
            cMap.removeWorldEntity(getPos()); // ????????????
        }
        // ??????????????????
        Hero hero = player.heros.get(guard.getHeroId());
        int addExp = (int) Math.ceil(collectTime * 1.0 / Constant.MINUTE) * 20;
        addExp = heroService.adaptHeroAddExp(player, addExp);
        addExp = heroService.addHeroExp(hero, addExp, player.lord.getLevel(), player);
        // ??????????????????
        LogUtil.debug("roleId:", player.roleId, ", ===????????????:", guard.getArmy().getCollectTime(), ", ????????????:", addExp,
                ", ????????????:" + guard.getGrab());
        boolean effect = mineService.hasCollectEffect(player, staticMine.getMineType(), startDate, guard.getArmy());// ????????????;
        CommonPb.MailCollect collect = PbHelper.createMailCollectPb(collectTime, hero, addExp, guard.getGrab(), effect);
        // ????????????
        this.guard = null;
        return collect;

    }

    /**
     * ??????????????????
     *
     * @return
     */
    public int calcRemainRes() {
        int now = TimeHelper.getCurrentSecond();
        return remainRes - calcCollect(now);
    }

    /**
     * ???????????????????????????
     *
     * @param now
     * @return
     */
    public int calcCollect(int now) {
        if (guard == null) {
            return 0;
        }
        StaticMine staticMine = StaticWorldDataMgr.getMineMap().get(mineId);
        if (staticMine == null) {
            return 0;
        }
        int collectTime = now - guard.getBeginTime(); // ???????????????
        collectTime = Math.max(collectTime, 0);
        double speed = staticMine.getSpeed();
        int resource = (int) Math.floor(speed * collectTime * 1.0 / Constant.HOUR);
        resource = Math.min(resource, remainRes);
        return resource;
    }

    /**
     * ?????????????????????
     *
     * @param cMap
     * @param staticHero
     * @return
     */
    public int canCollectMaxTime(CrossWorldMap cMap, StaticHero staticHero) {
        // ??????????????????????????????
        int maxTime = staticHero.getCollect();
        StaticMine staticMine = StaticWorldDataMgr.getMineMap().get(mineId);
        int speed = staticMine.getSpeed();
        double maxCollect = speed * 1.0d * maxTime / Constant.HOUR;
        if (remainRes < maxCollect) {
            maxCollect = remainRes;
        }
        maxTime = (int) Math.ceil(maxCollect * Constant.HOUR / speed);
        return maxTime;
    }
}
