package com.gryphpoem.game.zw.gameplay.local.world.map;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.util.dto.AttackParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.army.BaseArmy;
import com.gryphpoem.game.zw.gameplay.local.world.army.MapMarch;
import com.gryphpoem.game.zw.gameplay.local.world.army.WFCollectArmy;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.GlobalWarFire;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.PlayerWarFire;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb5.AttackCrossPosRs;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticMine;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.Guard;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.hero.PartnerHero;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.HeroUtil;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.HeroService;
import com.gryphpoem.game.zw.service.WorldService;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 战火燎原资源点
 *
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2021-01-28 16:12
 */
public class WFMineMapEntity extends MineMapEntity {

    //最后获取采集资源时间
    private int lastScoreTime;

    public WFMineMapEntity(int pos, int mineId, int lv, int mineType, int remainRes) {
        super(pos, mineId, lv, mineType, remainRes);
    }


    @Override
    public CommonPb.MapEntityPb.Builder toDbData() {
        CommonPb.MapEntityPb.Builder builder = super.toDbData();
        CommonPb.WarFireMapEntityExt.Builder extBuilder = CommonPb.WarFireMapEntityExt.newBuilder();
        extBuilder.setLastScoreTime(lastScoreTime);
        builder.setCityExt(extBuilder);
        return builder;
    }

    /**
     * 进攻资源点
     *
     * @param param 参数
     * @throws MwException 自定义异常
     */
    @Override
    public void attackPos(AttackParamDto param) throws MwException {
        Player invokePlayer = param.getInvokePlayer();
        long roleId = invokePlayer.roleId;
        CrossWorldMap cMap = param.getCrossWorldMap();
        // 判断采集队伍上限
        super.checkCollectArmy(roleId, cMap);
        WorldService worldService = DataResource.ac.getBean(WorldService.class);
        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        boolean mineHasPlayer = super.getGuard() != null;
        List<List<Integer>> needCost = worldService.combineAttackMineCost(param.getNeedFood(), false);
        // 检测并扣除资源
        rewardDataManager.checkAndSubPlayerRes(invokePlayer, needCost, AwardFrom.ATK_POS);
        // 部队逻辑
        List<CommonPb.PartnerHeroIdPb> form = param.getHeroIdList().stream().map(heroId -> {
            PartnerHero partnerHero = invokePlayer.getPlayerFormation().getPartnerHero(heroId);
            if (HeroUtil.isEmptyPartner(partnerHero)) return null;
            return partnerHero.convertTo();
        }).filter(pb -> Objects.nonNull(pb)).collect(Collectors.toList());

        int now = TimeHelper.getCurrentSecond();
        int marchTime = cMap.marchTime(cMap, invokePlayer, invokePlayer.lord.getPos(), pos);
        int endTime = now + marchTime;
        Army army = new Army(invokePlayer.maxKey(), ArmyConstant.ARMY_TYPE_COLLECT, pos, ArmyConstant.ARMY_STATE_MARCH, form, marchTime, endTime - 1, invokePlayer.getDressUp());
        army.setLordId(roleId);
        army.setTargetId(super.getMineId());
        army.setOriginPos(invokePlayer.lord.getPos());

        // 添加行军路线
        WFCollectArmy collectArmy = new WFCollectArmy(army);
        collectArmy.setArmyPlayerHeroState(cMap.getMapMarchArmy(), ArmyConstant.ARMY_STATE_MARCH);
        cMap.getMapMarchArmy().addArmy(collectArmy);
        // 事件通知
        cMap.publishMapEvent(collectArmy.createMapEvent(MapCurdEvent.CREATE),
                MapEvent.mapEntity(invokePlayer.lord.getPos(), MapCurdEvent.UPDATE));

        // 填充返回值
        AttackCrossPosRs.Builder builder = param.getBuilder();
        builder.setArmy(PbHelper.createArmyPb(army, false));
    }


    public void removeWorldWFMineEntity(CrossWorldMap cMap) {
        // 正在采集的玩家
//        if (guard == null) guard = getGuard();
//        Player armyPlayer = guard != null ? guard.getPlayer() : null;
//        if (Objects.nonNull(armyPlayer)) {
//            CommonPb.MailCollect mailCollect = settleCollect(cMap);
//            MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);
//            List<CommonPb.Award> grab = guard.getGrab();
//            // 采集邮件
//            StaticMine staticMine = getCfgMine();
//            mailDataManager.sendCollectMail(armyPlayer, null, MailConstant.MOLD_CROSSMAP_COLLECT_BREAK, mailCollect,
//                    TimeHelper.getCurrentSecond(), staticMine.getMineId(), grab.get(0).getCount(), staticMine.getMineId(),
//                    grab.get(0).getCount());
//        }

        LogUtil.world(String.format("战火燎原 --- 采集矿点ID :%d, 矿点位置 :%d, 资源被采光从地图上删除该资源矿点 ", getCfgMine().getMineId(), getPos()));
        // 移除矿点
        cMap.removeWorldEntity(getPos());
        // 地图推送, 如果在外层做, 这里是可以去掉的
        cMap.publishMapEvent(MapEvent.mapEntity(getPos(), MapCurdEvent.DELETE));
    }

    /**
     * 采集的最大时间
     *
     * @param cMap       新地图数据
     * @param staticHero 将领配置
     * @return 最大的采集时间
     */
    @Override
    public int canCollectMaxTime(CrossWorldMap cMap, StaticHero staticHero) {
        Date endDate = cMap.getGlobalWarFire().getEndDate();
        // 功能结束时间, 就是最大的采集时间
        int maxTime = TimeHelper.dateToSecond(endDate);
        // 计算能采集的最大时间
        StaticMine staticMine = StaticWorldDataMgr.getMineMap().get(getMineId());
        int speed = staticMine.getSpeed();
        double maxCollect = speed * 1.0d * maxTime / Constant.HOUR;
        if (getRemainRes() < maxCollect) {
            maxCollect = getRemainRes();
        }
        maxTime = (int) Math.ceil(maxCollect * Constant.HOUR / speed);
        return maxTime;
    }


    /**
     * 剩余的资源量
     *
     * @return 剩余资源量
     */
    @Override
    public int calcRemainRes() {
        return getRemainRes();
    }

    /**
     * 计算当前矿点已经产出的资源
     *
     * @param now 当前时间
     * @return 矿点已经产出的资源
     */
    public int calcCollect(int now) {
        Guard guard = getGuard();
        if (Objects.nonNull(guard) && Objects.nonNull(guard.getArmy())) {
            List<CommonPb.Award> grab = guard.getGrab();
            if (!CheckNull.isEmpty(grab)) {
                return grab.get(0).getCount();
            }
        }
        return 0;
    }

    /**
     * 采集结算, 采集部队返回, 给将领加经验
     *
     * @param cMap 新地图数据
     * @return 采集邮件数据
     */
    @Override
    public CommonPb.MailCollect settleCollect(CrossWorldMap cMap) {
        Guard guard = getGuard();
        if (guard == null) {
            return null;
        }
        int now = TimeHelper.getCurrentSecond();
        HeroService heroService = DataResource.ac.getBean(HeroService.class);
        Player player = guard.getPlayer();
        Army army = guard.getArmy();
        // 采集了多久
        int collectTime = now - army.getBeginTime();

        //结算采集
        if (getRemainRes() > 0 && now - lastScoreTime >= TimeHelper.MINUTE_S) {
            GlobalWarFire gwf = cMap.getGlobalWarFire();
            PlayerWarFire pwf = gwf.getPlayerWarFire(army.getLordId());
            gwf.collectMine(this, pwf, now);
        }

        // 部队返回
        MapMarch mapMarchArmy = cMap.getMapMarchArmy();
        BaseArmy baseArmy = mapMarchArmy.getBaseArmyByLordIdAndKeyId(army.getLordId(), army.getKeyId());
        if (baseArmy != null) {
            baseArmy.normalRetreatArmy(mapMarchArmy);
        }

        //矿点没资源了则删除该资源点
        if (getRemainRes() <= 0) {
            removeWorldWFMineEntity(cMap);
        }

        // 给将领加经验
        Hero hero = player.heros.get(guard.getHeroId());
        int addExp = (int) Math.ceil(collectTime * 1.0 / Constant.MINUTE) * 20;
        addExp = heroService.adaptHeroAddExp(player, addExp);
        int chiefAddExp = heroService.addHeroExp(hero, addExp, player.lord.getLevel(), player);
        // 给副将加经验
        DataResource.ac.getBean(WorldService.class).addDeputyHeroExp(addExp, guard.getArmy().getHero().get(0), player);
        CommonPb.MailCollect collect = PbHelper.createMailCollectPb(collectTime, hero, chiefAddExp, guard.getGrab(), false);
        // 清空驻军
        setGuard(null);
        return collect;
    }

    public int getLastScoreTime() {
        return lastScoreTime;
    }

    public void setLastScoreTime(int lastScoreTime) {
        this.lastScoreTime = lastScoreTime;
    }
}