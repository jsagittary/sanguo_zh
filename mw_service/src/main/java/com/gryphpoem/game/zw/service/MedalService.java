package com.gryphpoem.game.zw.service;

import com.alibaba.fastjson.JSON;
import com.gryphpoem.cross.constants.PlayerUploadTypeDefine;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticCombatDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticMedalDataMgr;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.manager.MedalDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.constant.Constant.CombatType;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.medal.Medal;
import com.gryphpoem.game.zw.resource.pojo.medal.RedMedal;
import com.gryphpoem.game.zw.resource.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author chenqi
 * @ClassName: MedalService
 * @Description: 勋章 相关
 * @date 2018年9月12日
 */
@Service
public class MedalService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private MedalDataManager medalDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private HeroService heroService;

    /**
     * @param roleId
     * @return GamePb1.GetMedalsRs
     * @throws MwException
     * @Title: getMedals
     * @Description: 获取玩家所有勋章
     */
    public GamePb1.GetMedalsRs getMedals(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GamePb1.GetMedalsRs.Builder builder = GamePb1.GetMedalsRs.newBuilder();
        for (Medal medal : player.medals.values()) {
            builder.addMedal(PbHelper.createMedalPb(medal));
        }
        // 返回所有装备信息的协议
        return builder.build();
    }

    /**
     * @param roleId
     * @param heroId
     * @return GamePb1.GetHeroMedalRs
     * @throws MwException
     * @Title: getHeroMedal
     * @Description: 获取指定将领的勋章
     */
    public GamePb1.GetHeroMedalRs getHeroMedal(long roleId, int heroId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 检查玩家是否有该将领
        Hero hero = player.heros.get(heroId);
        if (hero == null) {
            throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "玩家没有该将领, roleId:", roleId, ", heroId:", heroId);
        }

        List<Medal> medals = medalDataManager.getHeroMedalByHeroId(player, heroId);

        GamePb1.GetHeroMedalRs.Builder builder = GamePb1.GetHeroMedalRs.newBuilder();

        if (!CheckNull.isEmpty(medals)) {
            for (Medal medal : medals) {
                builder.addMedal(PbHelper.createMedalPb(medal));
            }
        }

        return builder.build();
    }

    /**
     * @param roleId
     * @param keyId 勋章 私有id
     * @return GamePb1.MedalLockRs
     * @throws MwException
     * @Title: medalLock
     * @Description: 给勋章 上锁 或 解锁
     */
    public GamePb1.MedalLockRs medalLock(long roleId, int keyId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 检查玩家是否有该勋章
        Medal medal = player.medals.get(keyId);
        if (medal == null) {
            throw new MwException(GameError.MEDAL_NOT_FOUND.getCode(), "玩家没有该勋章, roleId:", roleId, ", keyId:", keyId);
        }

        // 根据该勋章的状态判断是上锁还是解锁
        if (medal.getIsLock() == null || medal.getIsLock() == 0) {// 上锁
            medal.setIsLock(1);
        } else {// 解锁
            medal.setIsLock(0);
        }

        GamePb1.MedalLockRs.Builder builder = GamePb1.MedalLockRs.newBuilder();

        builder.setIsLock(medal.getIsLock());

        return builder.build();
    }

    /**
     * 将领穿戴或更换勋章
     * @param roleId    玩家唯一Id
     * @param heroId    将领id
     * @param keyId     勋章唯一Id
     * @param type      0 穿戴(如果当前位置有勋章, 服务器主动卸下), 1 卸下
     * @return
     * @throws MwException
     */
    public GamePb1.UptHeroMedalRs uptHeroMedal(long roleId, int heroId, int keyId, int type) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        if(type < 0 || type > 2){
            throw new MwException(GameError.PARAM_ERROR.getCode(),GameError.err(roleId,"将领穿戴兵书参数错误",type));
        }

        // 检查玩家是否有该将领
        Hero hero = player.heros.get(heroId);
        if (hero == null) {
            throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "玩家没有该将领, roleId:", roleId, ", heroId:", heroId);
        }

        // 非空闲状态
        if (!hero.isIdle() && hero.getState() != ArmyConstant.ARMY_STATE_RETREAT) {
            throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "AttackPos，将领不在空闲中, roleId:", roleId, ", heroId:",
                    heroId, ", state:", hero.getState());
        }

        // 检查玩家是否有该勋章
        Medal medal = player.medals.get(keyId);
        if (medal == null) {
            throw new MwException(GameError.MEDAL_NOT_FOUND.getCode(), "玩家没有该勋章, roleId:", roleId, ", keyId:", keyId);
        }

        // 勋章的索引
        int index = medal instanceof RedMedal ? MedalConst.HERO_MEDAL_INDEX_1 : MedalConst.HERO_MEDAL_INDEX_0;

        if (type == 0) {
            // 获取该将领穿戴的勋章
            Medal medalHero = medalDataManager.getHeroMedalByHeroIdAndIndex(player, heroId, index);
            if (medalHero != null) {
                // 卸下原先的勋章
                downMedal(player, hero, medalHero, index);
            }
            // 穿戴
            upMedal(player, hero, medal, index);
        } else if(type ==1) {
            // 卸下
            downMedal(player, hero, medal, index);
        } else {
            //将领之间交换兵书
            Hero tgtHero = player.heros.get(medal.getHeroId());
            Medal srcMedal = medalDataManager.getHeroMedalByHeroIdAndIndex(player,heroId,index);
            int srcIndex = 0;
            if(Objects.nonNull(srcMedal)){//卸载源将领兵书
                srcIndex = srcMedal instanceof RedMedal ? MedalConst.HERO_MEDAL_INDEX_1 : MedalConst.HERO_MEDAL_INDEX_0;
                downMedal(player,hero,srcMedal,srcIndex);
            }
            if(Objects.nonNull(tgtHero)){//卸载目标将领兵书
                downMedal(player,tgtHero,medal,index);
                // 重新计算并更新将领属性
                CalculateUtil.processAttr(player, tgtHero);
            }
            //源将领穿戴目标兵书
            upMedal(player,hero,medal,index);
            //目标将领穿戴源兵书
            if(Objects.nonNull(tgtHero) && Objects.nonNull(srcMedal)){
                upMedal(player,tgtHero,srcMedal,srcIndex);

                // 检测红色勋章的技能激活
                checkRedMedalUnLock(player, tgtHero);
                // 重新计算并更新将领属性
                CalculateUtil.processAttr(player, tgtHero);
            }
        }

        // 检测红色勋章的技能激活
        checkRedMedalUnLock(player, hero);

        // 重新计算并更新将领属性
        CalculateUtil.processAttr(player, hero);

        if (hero.isOnBattle() || hero.isOnWall()) {
            List<Long> rolesId = new ArrayList<>();
            EventBus.getDefault().post(
                    new Events.CrossPlayerChangeEvent(PlayerUploadTypeDefine.UPLOAD_TYPE_HERO,
                            0, CrossFunction.CROSS_WAR_FIRE, rolesId));
        }
        GamePb1.UptHeroMedalRs.Builder builder = GamePb1.UptHeroMedalRs.newBuilder();
        return builder.build();
    }

    /**
     * 检测红色勋章的技能激活
     * @param player
     * @param hero
     */
    private void checkRedMedalUnLock(Player player, Hero hero) {
        Medal medal = medalDataManager.getHeroMedalByHeroIdAndIndex(player, hero.getHeroId(), MedalConst.HERO_MEDAL_INDEX_0);
        Medal red = medalDataManager.getHeroMedalByHeroIdAndIndex(player, hero.getHeroId(), MedalConst.HERO_MEDAL_INDEX_1);
        if (red != null) {
            RedMedal redMedal = (RedMedal) red;
            if (redMedal != null) {
                if (medal == null) {
                    redMedal.setAuraUnLock(false);
                    redMedal.setSpecialSkillUnLock(false);
                    return;
                }
                if (redMedal.hasAuraSkill() && medal.hasAuraSkill()) {
                    StaticMedalAuraSkill sMedalAuraSkill = StaticMedalDataMgr.getAuraSkillById(redMedal.getAuraSkillId());
                    if (sMedalAuraSkill != null && !CheckNull.isEmpty(sMedalAuraSkill.getMatchSkill())) {
                        redMedal.setAuraUnLock(sMedalAuraSkill.getMatchSkill().contains(medal.getAuraSkillId()));
                    } else {
                        redMedal.setAuraUnLock(false);
                    }
                }
                if (redMedal.hasSpecialSkill() && medal.hasSpecialSkill()) {
                    StaticMedalSpecialSkill sMedalSpecialSkill = StaticMedalDataMgr
                            .getSpecialSkillById(redMedal.getSpecialSkillId());
                    if (sMedalSpecialSkill != null) {
                        redMedal.setSpecialSkillUnLock(sMedalSpecialSkill.getMatchSkill() == medal.getSpecialSkillId());
                    } else {
                        redMedal.setSpecialSkillUnLock(false);
                    }
                }
            }
        }
    }

    /**
     * 穿戴勋章, Hero属性在外层重新计算
     * @param player    玩家对象
     * @param hero      将领对象
     * @param medal     勋章对象
     * @param index     勋章位置索引
     * @throws MwException  自定义异常
     */
    public void upMedal(Player player, Hero hero, Medal medal, int index) throws MwException {
        long roleId = player.roleId;
        int heroId = hero.getHeroId();
        int keyId = medal.getKeyId();
        // 判断勋章是否已被穿戴
        if (medal.isOnMedal()) {
            throw new MwException(GameError.MEDAL_ALREADY_HAVE_HERO.getCode(), "该勋章已被穿戴, roleId:", roleId,
                    ", keyId:", keyId);
        }

        // 戴上勋章
        medal.onMedal(heroId);
        hero.modifyMedalKey(index, medal.getKeyId());

        // 勋章属性加成
        hero.getAttr()[medal.getMedalAttr().getA()] += medal.getMedalAttr().getB();

        LogUtil.debug("将领戴上勋章--》roleId:", player.roleId,
                " heroId:" + heroId + ",medalId:" + medal.getMedalId() + ",medalKeyId=" + medal.getKeyId() + ", index=" + index);
    }

    /**
     * 卸下勋章, Hero属性在外层重新计算
     * @param player    玩家对象
     * @param hero      将领对象
     * @param medal     勋章对象
     * @param index     勋章位置索引
     */
    public void downMedal( Player player, Hero hero, Medal medal, int index) {
        if (medal != null) {
            medal.downMedal();
            if (index == MedalConst.HERO_MEDAL_INDEX_1) {
                RedMedal redMedal = (RedMedal) medal;
                redMedal.setAuraUnLock(false);
                redMedal.setSpecialSkillUnLock(false);
            }
            hero.modifyMedalKey(index, 0);

            // 卸下勋章时, 去掉勋章的属性加成
            hero.getAttr()[medal.getMedalAttr().getA()] -= medal.getMedalAttr().getB();

            LogUtil.debug("将领卸下勋章--》roleId:", player.roleId,
                    " heroId:" + hero.getHeroId() + ",medalId:" + medal.getMedalId() + ",medalKeyId=" + medal.getKeyId() + ", index=" + index);
        }
    }

    /**
     * @param roleId
     * @return GamePb1.GetHonorGoldBarRs
     * @throws MwException
     * @Title: getHonorGoldBar
     * @Description: 获取当前拥有的荣誉点数和金条数
     */
    public GamePb1.GetHonorGoldBarRs getHonorGoldBar(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GamePb1.GetHonorGoldBarRs.Builder builder = GamePb1.GetHonorGoldBarRs.newBuilder();

        builder.setHonorNum(player.lord.getHonor());
        builder.setGoldBarNum(player.lord.getGoldBar());
        builder.setGoldIngot(player.getMixtureDataById(PlayerConstant.GOLD_INGOT));

        return builder.build();
    }

    /**
     * @param roleId
     * @param type 1获取 2刷新
     * @return GamePb1.GetMedalGoodsRs
     * @throws MwException
     * @Title: getMedalGoods
     * @Description: 获取勋章商店的商品
     */
    public GamePb1.GetMedalGoodsRs getMedalGoods(long roleId, int type) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 判断type
        if (type < MedalConst.MEDAL_GOODS_GET_TYPE || type > MedalConst.MEDAL_GOODS_REFRESH_TYPE) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "类型错误, roleId:", roleId, ", type:", type);
        }
        // 判断勋章刷新配置
        List<Integer> list = MedalConst.MEDAL_GOODS_REFRESH_EVERYDAY;
        if (CheckNull.isEmpty(list)) {
            throw new MwException(GameError.MEDAL_GOODS_REFRESH_CONFIG_ERROR.getCode(), "勋章商品刷新配置异常, roleId:", roleId);
        }

        GamePb1.GetMedalGoodsRs.Builder builder = GamePb1.GetMedalGoodsRs.newBuilder();

        // 判断是否解锁
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, MedalConst.MEDAL_SYS_LOCK)) {
            return builder.build();
        }

        // 第一次进入商店
        // if(CheckNull.isEmpty(player.getMedalGoods())) {
        //     medalDataManager.initMedalGoods(player, 0);//初始化玩家勋章商品
        // }

        // 判断是否刷新
        if (type == MedalConst.MEDAL_GOODS_REFRESH_TYPE) {
            // 判断剩余 次数
            if (player.getMedalGoodsRefNum() >= MedalConst.MEDAL_GOODS_REFRESH_MAX) {
                throw new MwException(GameError.NO_MEDAL_GOODS_REFRESH.getCode(), "勋章商店刷新次数不足, roleId:", roleId,
                        ", medalGoodsRefNum:", player.getMedalGoodsRefNum());
            }

            // 金币扣除
            int needGold = MedalConst.MEDAL_GOODS_REFRESH_GOLD;
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, needGold,
                    AwardFrom.MEDAL_GOODS_REFRESH);
            // 刷新次数 +1
            player.setMedalGoodsRefNum(player.getMedalGoodsRefNum() + 1);
            medalDataManager.initMedalGoods(player, MedalConst.MEDAL_GOODS_REFRESH_GOLD_TYPE);// 刷新
        }
        // 获取下次自动刷新的时间
        int now = TimeHelper.getCurrentSecond();
        int refreshTime = 0;// 下次刷新时间
        for (int time : list) {
            // 获取配置刷新时间的当天秒数
            int refresh = DateHelper.getTimeZoneDate(now, time);
            if (now < refresh) {
                refreshTime = refresh;
                break;
            }
        }
        if (refreshTime == 0) {// 说明下次刷新在转点后
            refreshTime = TimeHelper.getSomeDayAfter(1, 0, 0, 0);
        }
        builder.setRefreshNum(MedalConst.MEDAL_GOODS_REFRESH_MAX - player.getMedalGoodsRefNum());
        if (CheckNull.isEmpty(player.getMedalGoods())) {
            builder.addAllMedalGoodsId(new ArrayList<Integer>());
        } else {
            builder.addAllMedalGoodsId(player.getMedalGoods());
        }
        builder.setRefreshTime(refreshTime);
        return builder.build();
    }

    /**
     * 购买勋章
     *
     * @param roleId 角色id
     * @param medalGoodsId 商品id
     * @return
     * @throws MwException
     */
    public GamePb1.BuyMedalRs buyMedalGoods(long roleId, int medalGoodsId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GamePb1.BuyMedalRs.Builder builder = GamePb1.BuyMedalRs.newBuilder();

        // 判断是否解锁
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, MedalConst.MEDAL_SYS_LOCK)) {
            return builder.build();
        }

        // 判断商品是否存在
        if (!player.getMedalGoods().contains(medalGoodsId)) {
            throw new MwException(GameError.MEDAL_GOODS_NOT_FOUND.getCode(), "勋章商店没有该勋章, roleId:", roleId,
                    ", medalGoodsId:", medalGoodsId);
        }

        // 获取该商品的荣誉点数
        int honor = medalDataManager.getMedalGoodsHonor(medalGoodsId);

        // 判断是否是免费商品
        if (medalDataManager.checkGratis(medalGoodsId)) {// 免费荣誉
            // 增加玩家荣誉点数
            rewardDataManager.addAward(player, AwardType.MONEY, AwardType.Money.HONOR, honor, AwardFrom.GRATIS_HONOR);

        } else { // 勋章

            // 扣除玩家荣誉
            rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.HONOR, honor,
                    AwardFrom.BUY_MEDAL_GOODS, false);

            ArrayList<Medal> medals = rewardDataManager.addMedal(player, 0, medalGoodsId, 1, AwardFrom.BUY_MEDAL_GOODS);
            if (!CheckNull.isEmpty(medals)) {
                Medal medal = medals.get(0);
                if (!CheckNull.isNull(medal)) {
                    builder.setMedal(PbHelper.createMedalPb(medal));
                }
            }

        }
        // 去除该商品
        for (int i = 0; i < player.getMedalGoods().size(); i++) {
            if (player.getMedalGoods().get(i) == medalGoodsId) {
                player.getMedalGoods().remove(i);
                break;
            }
        }
        builder.setHonorNum(player.lord.getHonor());

        return builder.build();
    }

    /**
     * 勋章强化
     *
     * @param roleId
     * @param keyId
     * @return
     * @throws MwException
     */
    public GamePb1.IntensifyMedalRs intensifyMedal(long roleId, int keyId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 检查玩家是否有该勋章
        Medal medal = player.medals.get(keyId);
        if (medal == null) {
            throw new MwException(GameError.MEDAL_NOT_FOUND.getCode(), "玩家没有该勋章, roleId:", roleId, ", keyId:", keyId);
        }

        // 判断勋章是否存在
        StaticMedal staticmedal = StaticMedalDataMgr.getMedalById(medal.getMedalId());
        if (staticmedal == null) {
            throw new MwException(GameError.MEDAL_CONFIG_NOT_FOUND.getCode(), "没有该勋章的配置, roleId:", roleId, ", medalId:",
                    medal.getMedalId());
        }

        // 判断该勋章是否已经达到最大等级
        if (medal.getLevel() >= staticmedal.getMaxLevel()) {
            throw new MwException(GameError.WALLNPC_LV_FULL.getCode(), "已强化到最大等级, roleId:", roleId, ", level:",
                    medal.getLevel());
        }

        // 是否是红色勋章
        boolean redMedal = medal instanceof RedMedal;
        int goldBar = medalDataManager.getIntensifyGoldBar(medal.getLevel(), redMedal);
        if (goldBar == 0) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "勋章强化金条消耗配置异常, roleId:", roleId, ", level:",
                    medal.getLevel());
        }

        // 扣除玩家金条或者金锭
        rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, redMedal ? AwardType.Money.GOLD_INGOT : AwardType.Money.GOLD_BAR, goldBar,
                AwardFrom.MEDAL_INTENSIFY, false);


        // 提升勋章等级
        medalDataManager.addMedalLv(medal, staticmedal);

        // 重新计算并更新将领属性
        if (medal.isOnMedal()) {
            CalculateUtil.processAttr(player, player.heros.get(medal.getHeroId()));
        }

        LogUtil.getLogThread().addCommand(() -> {
            Turple<Integer, Integer> skill1 = medal.getGeneralSkillIdLv(1);
            Turple<Integer, Integer> skill2 = medal.getGeneralSkillIdLv(2);
            Turple<Integer, Integer> skill3 = medal.getGeneralSkillIdLv(3);
            LogLordHelper.gameLog(LogParamConstant.MEDAL_STRENGTHEN, player,
                    AwardFrom.MEDAL_INTENSIFY, medal.getMedalId(), medal.getKeyId(), medal.getLevel(),
                    medal.getMedalAttrLogStr(), medal.getGeneralSkillId(), skill1.getA(), skill1.getB(), skill2.getA(), skill2.getB(),
                    skill3.getA(), skill3.getB(), CheckNull.isNull(medal.getSpecialSkillId()) ? 0 : medal.getSpecialSkillId(),
                    CheckNull.isNull(medal.getSpecialSkillId()) ? 0 : medal.getAuraSkillId());
        });

        GamePb1.IntensifyMedalRs.Builder builder = GamePb1.IntensifyMedalRs.newBuilder();

        builder.setMedal(PbHelper.createMedalPb(medal));
        builder.setGoldBarNum(player.lord.getGoldBar());
        builder.setGoldIngot(player.getMixtureDataById(PlayerConstant.GOLD_INGOT));

        return builder.build();
    }

    /**
     * 勋章捐献
     *
     * @param roleId 角色id
     * @param keyIds 要捐献的勋章id
     * @return
     * @throws MwException
     */
    public GamePb1.DonateMedalRs donateMedal(long roleId, List<Integer> keyIds) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 检查勋章是否都存在 且是否满足捐献条件
        for (int keyId : keyIds) {
            if (player.medals.get(keyId) == null) {
                throw new MwException(GameError.MEDAL_NOT_FOUND.getCode(), "玩家没有该勋章, roleId:", roleId, ", keyId:",
                        keyId);
            }
            if (player.medals.get(keyId).isOnMedal()) {
                throw new MwException(GameError.MEDAL_ALREADY_HAVE_HERO.getCode(), "勋章被穿戴, roleId:", roleId, ", keyId:",
                        keyId);
            }
            if (player.medals.get(keyId) != null && player.medals.get(keyId).isLock()) {
                throw new MwException(GameError.MEDAL_IS_LOCK.getCode(), "勋章已加锁, roleId:", roleId, ", keyId:", keyId);
            }
        }

        // 捐献
        GamePb1.DonateMedalRs.Builder builder = GamePb1.DonateMedalRs.newBuilder();
        int goldBarCnt = 0;// 捐献获得的总金条数
        int honorCnt = 0;// 捐献获得的总荣誉数
        int goldIngot = 0;
        for (int keyId : keyIds) {
            Medal medal = player.medals.get(keyId);
            // 是否是红色勋章
            boolean redMedal = medal instanceof RedMedal;
            // 根据强化等级进行 金条返还
            if (medal.getLevel() > 0) {
                List<List<Integer>> lists = redMedal ? MedalConst.MEDAL_DONATE_GOLD_INGOT_RETURN : MedalConst.MEDAL_DONATE_GOLD_BAR_RETURN;
                int addGoldBar = 0;
                for (List<Integer> list : lists) {
                    if (list.size() > 1 && list.get(0) == medal.getLevel()) {
                        addGoldBar = list.get(1);
                    }
                }
                if (addGoldBar <= 0) {
                    throw new MwException(GameError.MEDAL_DONATE_GOLD_BAR_RTN_CFG_ERROR.getCode(),
                            "勋章捐献金条返还配置异常, roleId:", roleId, ", level:", medal.getLevel());
                }

                if (redMedal) {
                    goldIngot += addGoldBar;
                } else {
                    goldBarCnt += addGoldBar;
                }

                // 增加金条
                rewardDataManager.addAward(player, AwardType.MONEY, redMedal ? AwardType.Money.GOLD_INGOT : AwardType.Money.GOLD_BAR, addGoldBar,
                        AwardFrom.MEDAL_DONATE);
            }

            // 根据权重随机 是升级 还是 获得资源
            StaticMedalDonate staticMedalDonate = StaticMedalDataMgr.getMedalDonateByQuality(medal.getQuality());
            if (staticMedalDonate == null || CheckNull.isEmpty(staticMedalDonate.getWeight())) {
                throw new MwException(GameError.MEDAL_DONATE_CFG_ERROR.getCode(), "勋章捐献配置异常, roleId:", roleId,
                        ", quality:", medal.getQuality());
            }
            // 根据权重获取捐献类型
            List<List<Integer>> donateWeight = staticMedalDonate.getWeight();
            int type = RandomUtil.getRandomByWeight(donateWeight);
            if (type < MedalConst.MEDAL_DONATE_UPGRADE_TYPE || type > MedalConst.MEDAL_DONATE_GET_RESOURCE_TYPE) {
                throw new MwException(GameError.MEDAL_DONATE_CFG_ERROR.getCode(), "勋章捐献配置异常, roleId:", roleId,
                        ", quality:", medal.getQuality());
            }

            if (type == MedalConst.MEDAL_DONATE_UPGRADE_TYPE) {// 品质升级
                int quality;
                if (medal.getQuality() >= Constant.Quality.orange) {// 该勋章已到达最高级品质
                    quality = medal.getQuality();
                } else {
                    quality = medal.getQuality() + 1;
                }
                List<List<Integer>> upgradeWeight = StaticMedalDataMgr.getMedalDonateWeightByQuality(quality);
                if (CheckNull.isEmpty(upgradeWeight)) {
                    throw new MwException(GameError.MEDAL_CFG_ERROR.getCode(), "勋章配置异常, roleId:", roleId, ", quality:",
                            quality);
                }
                // 根据权重随机获取新勋章
                int medalId = RandomUtil.getRandomByWeight(upgradeWeight);
                StaticMedal staticmedal = StaticMedalDataMgr.getMedalById(medalId);
                if (staticmedal == null) {
                    throw new MwException(GameError.MEDAL_CONFIG_NOT_FOUND.getCode(), "没有该勋章的配置, roleId:", roleId,
                            ", medalId:", medalId);
                }
                // 初始化勋章
                Medal newMedal = medalDataManager.initMedal(null, staticmedal);
                newMedal.setKeyId(player.maxKey());// 勋章私有id
                player.medals.put(newMedal.getKeyId(), newMedal);

                builder.addMedal(PbHelper.createMedalPb(newMedal));

                LogUtil.debug("勋章捐献获得新勋章--》roleId:", player.roleId,
                        ",medalId:" + newMedal.getMedalId() + ",medalKeyId=" + newMedal.getKeyId() + ",level="
                                + newMedal.getLevel() + ",medalAttr=" + newMedal.getMedalAttr() + ",auraSkillId="
                                + newMedal.getAuraSkillId() + ",specialSkillId=" + newMedal.getSpecialSkillId()
                                + ",generalSkillId=" + newMedal.getGeneralSkillId());

                // 记录玩家获得新勋章
                LogLordHelper.medal(AwardFrom.MEDAL_DONATE, player.account, player.lord, newMedal.getMedalId(),
                        newMedal.getKeyId(), Constant.ACTION_ADD);

            } else {// 获得资源
                if (staticMedalDonate.getGoldBar() > 0) {
                    if (redMedal) {
                        goldIngot += staticMedalDonate.getGoldBar();
                    } else {
                        goldBarCnt += staticMedalDonate.getGoldBar();
                    }
                    // 增加金条
                    rewardDataManager.addAward(player, AwardType.MONEY, redMedal ? AwardType.Money.GOLD_INGOT : AwardType.Money.GOLD_BAR,
                            staticMedalDonate.getGoldBar(), AwardFrom.MEDAL_DONATE);
                }
                if (staticMedalDonate.getHonor() > 0) {
                    // 增加荣誉
                    honorCnt += staticMedalDonate.getHonor();
                    rewardDataManager.addAward(player, AwardType.MONEY, AwardType.Money.HONOR,
                            staticMedalDonate.getHonor(), AwardFrom.MEDAL_DONATE);
                }
            }
            // 去除捐献的勋章
            player.medals.remove(keyId);
            LogUtil.debug("勋章捐献失去勋章--》roleId:", player.roleId,
                    ",medalId:" + medal.getMedalId() + ",medalKeyId=" + medal.getKeyId() + ",level=" + medal.getLevel()
                            + ",medalAttr=" + medal.getMedalAttr() + ",auraSkillId=" + medal.getAuraSkillId()
                            + ",specialSkillId=" + medal.getSpecialSkillId() + ",generalSkillId="
                            + medal.getGeneralSkillId());

            LogLordHelper.commonLog("DonateMedal", AwardFrom.MEDAL_DONATE, player, JSON.toJSONString(medal));
            // 记录玩家失去勋章
            LogLordHelper.medal(AwardFrom.MEDAL_DONATE, player.account, player.lord, medal.getMedalId(),
                    medal.getKeyId(), Constant.ACTION_SUB);
        }

        builder.setHonorNum(honorCnt);
        builder.setGoldBarNum(goldBarCnt);
        builder.setGoldIngot(goldIngot);

        return builder.build();
    }

    /**
     * @param roleId
     * @param honorGoodsId
     * @return GamePb1.BuyHonorRs
     * @throws MwException
     * @Title: buyHonor
     * @Description: 购买荣誉
     */
    public GamePb1.BuyHonorRs buyHonor(long roleId, int honorGoodsId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 获取商品配置
        StaticHonorGoods staticHonorGoods = StaticMedalDataMgr.getHonorGoodsById(honorGoodsId);

        if (staticHonorGoods == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "该荣誉商品不存在, roleId:", roleId, ", honorGoodsId:",
                    honorGoodsId);
        }

        // 金币扣除
        int needGold = staticHonorGoods.getGold();
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, needGold,
                AwardFrom.BUY_HONOR_GOODS);

        // 增加玩家荣誉
        rewardDataManager.addAward(player, AwardType.MONEY, AwardType.Money.HONOR, staticHonorGoods.getHonorNum(),
                AwardFrom.GRATIS_HONOR);

        GamePb1.BuyHonorRs.Builder builder = GamePb1.BuyHonorRs.newBuilder();

        builder.setHonorNum(player.lord.getHonor());
        return builder.build();
    }

    /**
     * @return void
     * @Title: refreshAllPlayerMedalGoods
     * @Description: 刷新所有玩家的勋章商品
     */
    public void refreshAllPlayerMedalGoods() {
        Iterator<Player> iterator = playerDataManager.getPlayers().values().iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            // 判断是否解锁
            if (!StaticFunctionDataMgr.funcitonIsOpen(player, MedalConst.MEDAL_SYS_LOCK)) {
                continue;
            }
            medalDataManager.initMedalGoods(player, MedalConst.MEDAL_GOODS_REFRESH_JOB_TYPE);// 刷新
        }
    }

    /**
     * @return void
     * @Title: westernPointSpecialTraining
     * @Description: 勋章特技效果 【西点特训】 每日5点执行
     */
    public void westernPointSpecialTraining() {
        Iterator<Player> iterator = playerDataManager.getPlayers().values().iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            // 判断是否解锁
            if (!StaticFunctionDataMgr.funcitonIsOpen(player, MedalConst.MEDAL_SYS_LOCK)) {
                continue;
            }
            for (Hero hero : player.heros.values()) {
                // 根据将领id 查询是否有特技
                int specialSkillId = medalDataManager.getHeroSpecialSkill(player, hero.getHeroId(), MedalConst.HERO_MEDAL_INDEX_0);
                if (specialSkillId > 0) {// 有特技
                    // 获取特技效果值
                    StaticMedalSpecialSkill staticMedalSpecialSkill = StaticMedalDataMgr
                            .getSpecialSkillById(specialSkillId);
                    if (staticMedalSpecialSkill != null
                            && !CheckNull.isEmpty(staticMedalSpecialSkill.getSkillEffect())) {
                        int skillEffect = staticMedalSpecialSkill.getSkillEffect().get(0);// 技能效果值
                        if (skillEffect > 0 && specialSkillId == MedalConst.WESTERN_POINT_SPECIAL_TRAINING) {
                            // 触发西点特训特技 佩戴勋章将领每天获得400体力的副本经验，每日5点生效
                            int combatId = player.lord.combatId;// 玩家当前的副本进度
                            do {
                                StaticCombat staticCombat = StaticCombatDataMgr.getStaticCombat(combatId);
                                // 关卡配置不存在 【或】 当前关卡不是 普通关卡 【或】 挑战次数 不是 0 【或】 没有将领经验
                                if (staticCombat == null || staticCombat.getType() != CombatType.type_1
                                        || staticCombat.getCnt() != 0 || staticCombat.getExp() == 0) {
                                    combatId--;
                                    continue;
                                }
                                int cnt = skillEffect / staticCombat.getCost();// 扫荡次数 = 体力/消耗
                                int addExp = cnt * staticCombat.getExp();// 获得的经验
                                int addHeroExp = heroService.addHeroExp(hero, addExp, player.lord.getLevel(), player);// 返回实际增加的经验
                                LogUtil.debug("勋章特技-西点特训触发 角色id:", player.roleId, ", 将领id:", hero.getHeroId(),
                                        ", 副本id：", combatId, ", 副本消耗：", staticCombat.getCost(), ", 特技加成的体力：",
                                        skillEffect, ", 可以扫荡的次数：", cnt, ", 获得的经验:", addExp, ", 实际增加的经验:", addHeroExp);
                                break;
                            } while (combatId > Constant.INIT_COMBAT_ID);// 大于初始关卡id
                        }
                    }
                }
            }
        }
    }

}
