package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticMedalDataMgr;
import com.gryphpoem.game.zw.gameplay.local.service.CrossWorldMapService;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4.SyncBuffRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Effect;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.medal.Medal;
import com.gryphpoem.game.zw.resource.pojo.medal.RedMedal;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.random.MedalGoodsRandom;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chenqi
 * @ClassName: MedalDataManager
 * @Description: 勋章
 * @date 2018年9月11日
 */
@Component
public class MedalDataManager {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;

    /**
     * 根据将领id 获取穿戴的勋章  没有穿戴返回null
     *
     * @param player 玩家对象
     * @param heroId 将领id
     * @return 将领穿戴的勋章
     */
    public List<Medal> getHeroMedalByHeroId(Player player, int heroId) {
        // 获取将领
        Hero hero = player.heros.get(heroId);
        if (hero == null) {
            return null;
        }
        List<Integer> medalKeys = hero.getMedalKeys();
        if (CheckNull.isEmpty(medalKeys)) {
            return null;
        }
        List<Medal> medals = new ArrayList<>(medalKeys.size());
        for (int medalKey : medalKeys) {
            // 获取勋章
            Medal medal = player.medals.get(medalKey);
            if (medal != null) {
                medals.add(medal);
            }
        }
        return medals;
    }

    /**
     * 获取指定将领穿戴位置的勋章
     *
     * @param player
     * @param heroId
     * @param index
     * @return
     */
    public Medal getHeroMedalByHeroIdAndIndex(Player player, int heroId, int index) {
        // 获取将领
        Hero hero = player.heros.get(heroId);
        if (hero == null) {
            return null;
        }
        List<Integer> medalKeys = hero.getMedalKeys();
        if (CheckNull.isEmpty(medalKeys)) {
            return null;
        }
        int medalKey = medalKeys.get(index);
        if (medalKey == 0) {
            return null;
        }
        return player.medals.get(medalKey);
    }

    /**
     * @param player
     * @return List<Integer>
     * @Title: initMedalGoods
     * @Description: 初始化玩家 勋章商店
     */
    public List<Integer> initMedalGoods(Player player, int refreshType) {
        //获取勋章商品配置
        List<Integer> list = new ArrayList<Integer>();
        // 如果开启了红色勋章功能
        if (StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ID_RED_MEDAL)) {
            // 3个红色勋章
            MedalGoodsRandom.randomMedalGoods(MedalConst.RED_MEDAL_GOODS_TYPE, 3, list);
            // 5个普通勋章
            MedalGoodsRandom.randomMedalGoods(MedalConst.MEDAL_GOODS_TYPE, 5, list);
        } else {
            // 8个普通勋章
            MedalGoodsRandom.randomMedalGoods(MedalConst.MEDAL_GOODS_TYPE, 8, list);
        }
        // 1个免费商品
        if (StaticMedalDataMgr.getGratisMedalGoodsId() != null) {
            list.add(StaticMedalDataMgr.getGratisMedalGoodsId());
        }
        player.setMedalGoods(list);
        //LogUtil.debug("初始化玩家勋章商品 roleId=" + player.roleId, ",勋章商品=" + list, ",刷新类型=" + refreshType);
        return list;
    }

    /**
     * @param medalGoodsId
     * @return boolean true 免费
     * @Title: checkGratis
     * @Description: 根据商品id 判断是否是免费商品
     */
    public boolean checkGratis(int medalGoodsId) {
        return StaticMedalDataMgr.getGratisMedalGoodsId() == medalGoodsId;
    }

    /**
     * @param medalGoodsId
     * @return int
     * @Title: getMedalGoodsHonor
     * @Description: 根据商品id  获取荣誉点数
     */
    public int getMedalGoodsHonor(int medalGoodsId) {
        StaticMedalGoods medalGoods = StaticMedalDataMgr.getMedalGoodsById(medalGoodsId);
        if (medalGoods != null) {
            return medalGoods.getBuyHonor();
        }
        return 0;
    }

    /**
     * 初始化 勋章属性、技能
     *
     * @param staticMedalGoods 勋章商店配置
     * @param staticmedal      勋章配置
     * @return 初始化的勋章
     * @throws MwException
     */
    public Medal initMedal(StaticMedalGoods staticMedalGoods, StaticMedal staticmedal) throws MwException {
        Medal medal = staticmedal.getQuality() == Constant.Quality.red ? new RedMedal() : new Medal();
        // 勋章id
        medal.setMedalId(staticmedal.getMedalId());
        // 初始等级
        medal.setLevel(staticmedal.getLevel());
        // 勋章品质
        medal.setQuality(staticmedal.getQuality());
        // 勋章属性
        medal.setMedalAttr(initMedalAttr(staticmedal.getInitAttr(), staticmedal.getQuality()));

        // 初始化光环技能
        // 是勋章商品 且是橙色品质 并含有光环
        if (staticmedal.getQuality() >= Constant.Quality.orange) {
            if (staticMedalGoods != null && staticMedalGoods.getAuraSkillId() != null
                    && staticMedalGoods.getAuraSkillId() != 0) {
                // 商品里的光环
                medal.setInitAuraSkillId(staticMedalGoods.getAuraSkillId());
            } else {
                // 根据权重获取光环技能
                int auraSkillId = initSkill(StaticMedalDataMgr
                        .getMedalSkillWeightByType(staticmedal.getMedalId(), MedalConst.SkillType.AURA));
                if (auraSkillId == 0) {
                    // 该勋章没有配置光环
                    throw new MwException(GameError.MEDAL_AURA_SKILL_CONFIG_ERROR.getCode(), "勋章没有配置光环技能权重, medalId:",
                            staticmedal.getMedalId());
                }
                medal.setInitAuraSkillId(auraSkillId);
            }
        }

        // 根据权重获取  初始化技能数量
        int initSkillNum = initSkillNum(staticmedal.getInitSkillNum());
        // 说明还有技能需要初始化
        if (initSkillNum > 0) {
            activationSkill(initSkillNum, staticmedal, staticMedalGoods, medal);
        }
        return medal;
    }

    /**
     * @param skillNum         要激活的技能数量
     * @param staticmedal      勋章配置
     * @param staticMedalGoods 购买勋章就传
     * @param medal            勋章
     * @return void
     * @Title: activationSkill
     * @Description: 激活技能
     */
    public void activationSkill(int skillNum, StaticMedal staticmedal, StaticMedalGoods staticMedalGoods, Medal medal) {
        for (int i = 0; i < skillNum; i++) {
            int skillType = 0;
            // 先根据权重 获取技能类型
            List<List<Integer>> skillTypeWeight = new ArrayList<List<Integer>>();
            if (medal.getQuality() >= Constant.Quality.orange) {
                // 橙色勋章  筛选技能类型权重
                if (medal.getAuraSkillId() == null || medal.getAuraSkillId() == 0) {
                    // 该勋章还没有光环  则加上光环技能类型权重
                    skillTypeWeight.add(staticmedal.getInitGeneralSkill().get(0));
                }
                if (medal.getSpecialSkillId() == null || medal.getSpecialSkillId() == 0) {
                    // 该勋章还没有特技 则加上特技技能类型权重
                    skillTypeWeight.add(staticmedal.getInitGeneralSkill().get(1));
                }
                // 普通技能权重
                skillTypeWeight.add(staticmedal.getInitGeneralSkill().get(2));
            } else {
                // 橙色以下的   只有普通技能类型权重
                skillTypeWeight = staticmedal.getInitGeneralSkill();
            }
            // 根据权重获取 激活的技能类型
            skillType = initSkillType(skillTypeWeight);
            switch (skillType) {
                // 光环
                case MedalConst.SkillType.AURA:
                    // 判断是否已经有光环
                    if (medal.hasAuraSkill()) {
                        // 已有
                        continue;
                    }
                    //激活光环
                    medal.setAuraSkillId(medal.getInitAuraSkillId());
                    break;
                // 特技
                case MedalConst.SkillType.SPECIAL:
                    // 判断是否已经有特技
                    if (medal.getSpecialSkillId() != null && medal.getSpecialSkillId() > 0) {
                        // 已有
                        continue;
                    }
                    List<List<Integer>> list = StaticMedalDataMgr
                            .getMedalSkillWeightByType(staticmedal.getMedalId(), skillType);
                    if (!CheckNull.isEmpty(list) && !CheckNull.isEmpty(list.get(0))) {
                        medal.setSpecialSkillId(list.get(0).get(0));
                    }
                    break;
                // 普通
                case MedalConst.SkillType.GENERAL:
                    // 已有的普通技能
                    List<Integer> skillIds = medal.getGeneralSkillId();
                    // 过滤掉已有的普通技能权重
                    List<List<Integer>> lists = StaticMedalDataMgr
                            .getMedalSkillWeightByType(staticmedal.getMedalId(), skillType).stream()
                            .filter(l -> !skillIds.contains(l.get(0))).collect(Collectors.toList());
                    // 根据权重获取技能
                    int generalSkillId = initSkill(lists);
                    // 配置少了
                    if (generalSkillId == 0) {
                        continue;
                    }
                    medal.getGeneralSkillId().add(generalSkillId);
                    break;
            }
        }
    }

    /**
     * @param list    勋章的初始化属性权重配置
     * @param quality 勋章品质
     * @return Turple<Integer, Integer>  A为属性id  B为属性值
     * @Title: initMedalAttr
     * @Description: 根据权重 随机获取 勋章初始属性
     */
    public Turple<Integer, Integer> initMedalAttr(List<List<Integer>> list, int quality) {
        // 根据权重获取属性id
        List<Integer> attr = RandomUtil.getRandomByWeight(list, 1, false);
        if (CheckNull.isEmpty(attr)) {
            return null;
        }
        // 根据属性id 和品质   获取初始化的属性值
        List<Integer> attrValue = StaticMedalDataMgr.getMedalAttrConfig(quality, attr.get(0));
        if (CheckNull.isEmpty(attrValue)) {
            return null;
        }
        return new Turple<Integer, Integer>(attr.get(0), attrValue.get(0));
    }

    /**
     * @param list 勋章的初始化技能数量权重配置
     * @return int 技能数量
     * @Title: initSkillNum
     * @Description: 根据权重 随机获取 勋章初始化技能数量
     */
    public int initSkillNum(List<List<Integer>> list) {
        // 根据权重获取技能数量
        List<Integer> skillNum = RandomUtil.getRandomByWeight(list, 1, false);
        if (CheckNull.isEmpty(skillNum)) {
            return 0;
        }
        return skillNum.get(0);
    }

    /**
     * @param list 勋章的技能类型范围 权重配置
     * @return int 技能类型
     * @Title: initSkillType
     * @Description: 根据权重 随机获取 技能类型
     */
    public int initSkillType(List<List<Integer>> list) {
        List<Integer> skillType = RandomUtil.getRandomByWeight(list, 1, false);//根据权重获取技能类型
        if (CheckNull.isEmpty(skillType)) {
            return 0;
        }
        return skillType.get(0);
    }

    /**
     * @param list 勋章的技能权重配置
     * @return int 技能id
     * @Title: initSkill
     * @Description: 根据权重 随机获取 技能
     */
    public int initSkill(List<List<Integer>> list) {
        // 根据权重获取技能类型
        List<Integer> skill = RandomUtil.getRandomByWeight(list, 1, false);
        if (CheckNull.isEmpty(skill)) {
            return 0;
        }
        return skill.get(0);
    }

    /**
     * 获取强化所需的金条数
     *
     * @param level    等级
     * @param redMedal 是否是红色勋章
     * @return 强化消耗
     */
    public int getIntensifyGoldBar(int level, boolean redMedal) {
        for (List<Integer> list : redMedal ?
                MedalConst.MEDAL_INTENSIFY_GOLD_INGOT :
                MedalConst.MEDAL_INTENSIFY_GOLD_BAR) {
            if (list.size() >= 2 && list.get(0) == level) {
                return list.get(1);
            }
        }
        return 0;
    }

    /**
     * @param medal
     * @return void
     * @Title: addMedalLv
     * @Description: 提升勋章等级
     */
    public void addMedalLv(Medal medal, StaticMedal staticmedal) throws MwException {
        medal.setLevel(medal.getLevel() + 1);//等级+1
        //根据属性id 和 品质   获取属性配置
        List<Integer> attrValue = StaticMedalDataMgr
                .getMedalAttrConfig(medal.getQuality(), medal.getMedalAttr().getA());
        if (CheckNull.isEmpty(attrValue)) {
            throw new MwException(GameError.MEDAL_ATTR_CONFIG_ERROR.getCode(), "勋章属性配置异常, medalId:", medal.getMedalId(),
                    ", quality:", medal.getQuality(), ", attrId:", medal.getMedalAttr().getA());
        }
        //属性提升
        medal.setMedalAttr(new Turple<Integer, Integer>(medal.getMedalAttr().getA(),
                medal.getMedalAttr().getB() + attrValue.get(1)));

        //小于等于8级 且能整除2  或者 大于8级
        if ((medal.getLevel() <= MedalConst.MEDAL_INTENSIFY_LV && medal.getLevel() % 2 == 0)
                || medal.getLevel() > MedalConst.MEDAL_INTENSIFY_LV) {
            //判断技能是否已满
            if (getMedalSkillNum(medal) >= staticmedal.getSkillNum()) {//已激活最大数量的技能
                //升级一个普通技能
                List<List<Integer>> skillWeight = new ArrayList<List<Integer>>();//普通技能升级权重
                for (int skillId : medal.getGeneralSkillId()) {//循环遍历普通技能  将没有升满的普通技能加入权重
                    StaticMedalGeneralSkill staticMedalGeneralSkill = StaticMedalDataMgr.getGeneralSkillById(skillId);
                    if (staticMedalGeneralSkill == null) {
                        throw new MwException(GameError.MEDAL_GENERAL_SKILL_CONFIG_ERROR.getCode(),
                                "勋章-普通技能配置异常, generalSkillId:", skillId);
                    }
                    if (staticMedalGeneralSkill.getLevel() < staticmedal.getGeneralSkillMaxLv()) {//没有到达最大等级
                        List<Integer> list = new ArrayList<Integer>();
                        list.add(skillId);
                        list.add(1000);
                        skillWeight.add(list);
                    }
                }
                //根据权重获取升级的技能
                if (skillWeight.size() > 0) {
                    int skillId = initSkill(skillWeight);
                    StaticMedalGeneralSkill staticMedalGeneralSkill = StaticMedalDataMgr
                            .getGeneralSkillById(skillId + 1);
                    if (staticMedalGeneralSkill == null) {
                        throw new MwException(GameError.MEDAL_GENERAL_SKILL_CONFIG_ERROR.getCode(),
                                "勋章-普通技能配置异常, generalSkillId:", skillId + 1);
                    }
                    for (int i = 0; i < medal.getGeneralSkillId().size(); i++) {
                        if (medal.getGeneralSkillId().get(i) == skillId) {
                            medal.getGeneralSkillId().remove(i);//去除旧技能
                            break;
                        }
                    }
                    medal.getGeneralSkillId().add(skillId + 1);//添加新技能
                }
            } else {//激活一个技能
                activationSkill(1, staticmedal, null, medal);
            }
        }
    }

    /**
     * @param medal
     * @return int
     * @Title: getMedalSkillNum
     * @Description: 获取勋章已经激活的技能数量
     */
    public int getMedalSkillNum(Medal medal) {
        int skillNum = 0;
        if (medal.hasAuraSkill()) {
            skillNum++;
        }
        if (medal.getSpecialSkillId() != null && medal.getSpecialSkillId() > 0) {
            skillNum++;
        }
        if (!CheckNull.isEmpty(medal.getGeneralSkillId())) {
            skillNum += medal.getGeneralSkillId().size();
        }
        return skillNum;
    }

    /**
     * @param player
     * @return void
     * @throws MwException
     * @Title: getMedalBydoCombat
     * @Description: 玩家打副本胜利后随机获取勋章
     */
    public List<Medal> getMedalBydoCombat(Player player) throws MwException {
        List<Medal> medals = new ArrayList<>();
        //判断是否解锁
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, MedalConst.MEDAL_SYS_LOCK)) {
            return medals;
        }
        if (!CheckNull.isEmpty(MedalConst.TASK_MEDAL_BURST_WEIGHT)) {
            List<Integer> taskMedal = RandomUtil.getRandomByWeight(MedalConst.TASK_MEDAL_BURST_WEIGHT, 2, false);
            if (!CheckNull.isEmpty(taskMedal)) {
                if (taskMedal.get(0) > 0) {
                    medals.addAll(rewardDataManager.addMedal(player, taskMedal.get(0), 0, taskMedal.get(1), AwardFrom.GAIN_COMBAT));
                }
            }
        }
        return medals;
    }

    /***************************  勋章特技效果 start  *********************************/
    /**
     * @param fighter
     * @param recoverArmyAwardMap
     * @return void
     * @Title: angelInWhite
     * @Description: 勋章特技效果  【白衣天使】   战斗结束后执行的
     */
    public void angelInWhite(Fighter fighter, Map<Long, List<CommonPb.Award>> recoverArmyAwardMap) {
        //判断是否是玩家
        if (fighter.roleType != Constant.Role.PLAYER) {
            return;
        }

        for (Force force : fighter.getForces()) {
            Player player = checkForces(force.ownerId);
            if (player == null) {
                continue;
            }

            Map<Integer, Integer> cntMap = new HashMap<>();

            List<CommonPb.Award> awards = recoverArmyAwardMap.get(player.roleId);
            if (CheckNull.isNull(awards)) {
                awards = new ArrayList<>();
                recoverArmyAwardMap.put(player.roleId, awards);
            }

            //根据将领id  查询是否有特技
            int specialSkillId = getHeroSpecialSkill(player, force.id, MedalConst.HERO_MEDAL_INDEX_0);
            if (specialSkillId > 0) {//有特技
                //获取特技效果值
                StaticMedalSpecialSkill staticMedalSpecialSkill = StaticMedalDataMgr
                        .getSpecialSkillById(specialSkillId);
                if (staticMedalSpecialSkill != null && !CheckNull.isEmpty(staticMedalSpecialSkill.getSkillEffect())) {
                    int skillEffect = staticMedalSpecialSkill.getSkillEffect().get(0);//技能效果值
                    if (skillEffect > 0 && specialSkillId == MedalConst.ANGEL_IN_WHITE) {
                        //触发 白衣天使特技  增加 skillEffect%  的伤兵恢复
                        int recovery = (int) (force.totalLost * (skillEffect / MedalConst.MEDAL_CONFIG_PERCENTAGE));
                        if (recovery == 0) {
                            continue;
                        }
                        Hero hero = player.heros.get(force.id);
                        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
                        if (!CheckNull.isNull(staticHero)) {
                            //判断是否超过最大兵力
                            int max = hero.getAttr()[HeroConstant.ATTR_LEAD];//最大兵力
                            if (force.maxHp < max * 0.2) {
                                continue;
                            }
                            if (recovery + hero.getCount() >= max) {
                                recovery = max - hero.getCount();
                            }
                            hero.addArm(recovery);//返还兵力
                            int armyType = staticHero.getType();
                            int cnt = cntMap.getOrDefault(armyType, 0);
                            cntMap.put(armyType, cnt + recovery);
                            LogUtil.debug("勋章特技-白衣天使触发 角色id:", player.roleId, ", 将领id:", force.id, ", 恢复的兵力:",
                                    recovery);
                            //记录玩家兵力变化信息
                            // LogLordHelper.filterHeroArm(AwardFrom.MEDAL_SKILL_ACTION, player.account, player.lord, hero.getHeroId(), hero.getCount(), recovery,
                            //         Constant.ACTION_ADD, armyType, hero.getQuality());

                            // 记录玩家兵力变化
                            LogLordHelper.playerArm(
                                    AwardFrom.MEDAL_SKILL_ACTION,
                                    player, armyType,
                                    Constant.ACTION_ADD,
                                    recovery,
                                    playerDataManager.getArmCount(player.resource, armyType)
                            );
                        }
                    }
                }
            }

            // 兵力恢复换算成奖励
            if (!CheckNull.isEmpty(cntMap)) {
                for (Map.Entry<Integer, Integer> kv : cntMap.entrySet()) {
                    awards.add(PbHelper.createAwardPb(AwardType.ARMY, kv.getKey(), kv.getValue()));
                }
            }
        }
    }

    /**
     * 勋章特技效果  【以战养战】   战斗结束后执行的
     *
     * @param attacker            进攻方
     * @param defender            防守方
     * @param recoverArmyAwardMap 伤兵恢复
     * @param atkSuccess          战斗结果
     */
    public void sustainTheWarByMeansOfWar(Fighter attacker, Fighter defender,
                                          Map<Long, List<CommonPb.Award>> recoverArmyAwardMap, boolean atkSuccess) {
        if (atkSuccess) {
            sustainTheWarByMeansOfWar(attacker, recoverArmyAwardMap);
        } else {
            sustainTheWarByMeansOfWar(defender, recoverArmyAwardMap);
        }
    }

    private void sustainTheWarByMeansOfWar(Fighter fighter, Map<Long, List<CommonPb.Award>> recoverArmyAwardMap) {
        //判断是否是玩家
        if (fighter.roleType != Constant.Role.PLAYER) {
            return;
        }
        //以战养战
        for (Force force : fighter.getForces()) {
            Player player = checkForces(force.ownerId);
            if (player == null) {
                continue;
            }

            Map<Integer, Integer> cntMap = new HashMap<>();

            List<CommonPb.Award> awards = recoverArmyAwardMap.get(player.roleId);
            if (CheckNull.isNull(awards)) {
                awards = new ArrayList<>();
                recoverArmyAwardMap.put(player.roleId, awards);
            }

            //根据将领id  查询是否有特技
            int specialSkillId = getHeroSpecialSkill(player, force.id, MedalConst.HERO_MEDAL_INDEX_0);
            if (specialSkillId > 0) {//有特技
                //获取特技效果值
                StaticMedalSpecialSkill staticMedalSpecialSkill = StaticMedalDataMgr
                        .getSpecialSkillById(specialSkillId);
                if (staticMedalSpecialSkill != null && !CheckNull.isEmpty(staticMedalSpecialSkill.getSkillEffect())) {
                    int skillEffect = staticMedalSpecialSkill.getSkillEffect().get(0);//技能效果值
                    if (skillEffect > 0 && specialSkillId == MedalConst.SUSTAIN_THE_WAR_BY_MEANS_OF_WAR) {
                        //触发 以战养战特技  杀敌数skillEffect%转化为本方兵力
                        int recovery = (int) (force.killed * (skillEffect / MedalConst.MEDAL_CONFIG_PERCENTAGE));
                        if (recovery == 0) {
                            continue;
                        }
                        Hero hero = player.heros.get(force.id);
                        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
                        if (hero != null && !CheckNull.isNull(staticHero)) {
                            // 最大损兵
                            if (recovery >= force.totalLost) {
                                recovery = force.totalLost;
                            }
                            hero.addArm(recovery);//增加兵力
                            int armyType = staticHero.getType();
                            int cnt = cntMap.getOrDefault(armyType, 0);
                            cntMap.put(armyType, cnt + recovery);
                            LogUtil.debug("勋章特技-以战养战触发 角色id:", player.roleId, ", 将领id:", force.id, ", 恢复的兵力:", recovery,
                                    ", 总损兵:", force.totalLost);
                            //记录玩家兵力变化信息
                            // LogLordHelper.filterHeroArm(AwardFrom.MEDAL_SKILL_ACTION, player.account, player.lord, hero.getHeroId(), hero.getCount(), recovery,
                            //         Constant.ACTION_ADD, armyType, hero.getQuality());

                            // 上报玩家兵力变化信息
                            LogLordHelper.playerArm(
                                    AwardFrom.MEDAL_SKILL_ACTION,
                                    player,
                                    armyType,
                                    Constant.ACTION_ADD,
                                    recovery,
                                    playerDataManager.getArmCount(player.resource, armyType)
                            );
                        }
                    }
                }
            }

            // 兵力恢复换算成奖励
            if (!CheckNull.isEmpty(cntMap)) {
                for (Map.Entry<Integer, Integer> kv : cntMap.entrySet()) {
                    awards.add(PbHelper.createAwardPb(AwardType.ARMY, kv.getKey(), kv.getValue()));
                }
            }
        }
    }

    /**
     * @param defender
     * @return void
     * @Title: peacekeepingForces
     * @Description: 勋章特技效果  【维和部队】   战斗结束后,防守方玩家被击飞,且生成重建资源后执行
     */
    public void peacekeepingForces(Fighter defender, Player defPlayer) {
        //判断是否是玩家
        if (defender.roleType != Constant.Role.PLAYER) {
            return;
        }
        //记录玩家是否触发  key 玩家id
        Map<Long, Boolean> map = new HashMap<Long, Boolean>();
        for (Force force : defender.getForces()) {
            Player player = checkForces(force.ownerId);
            if (player == null) {
                continue;
            }
            if (CrossWorldMapService.isOnCrossMap(player)) {
                continue;
            }
            if (defPlayer.lord.getLordId() != player.lord.getLordId() ||
                    player.getPeacekeepingForcesNum() > 0) {//当天已触发
                LogUtil.common(String.format("defPlayer: %d, player: %d, player.getPeacekeepingForcesNum(): %d", defPlayer.lord.getLordId(), player.lord.getLordId(), player.getPeacekeepingForcesNum()));
                continue;
            }

            //根据将领id  查询是否有特技
            int specialSkillId = getHeroSpecialSkill(player, force.id, MedalConst.HERO_MEDAL_INDEX_0);
            if (specialSkillId > 0) {//有特技
                //获取特技效果值
                StaticMedalSpecialSkill staticMedalSpecialSkill = StaticMedalDataMgr
                        .getSpecialSkillById(specialSkillId);
                if (staticMedalSpecialSkill != null && !CheckNull.isEmpty(staticMedalSpecialSkill.getSkillEffect())) {
                    int skillEffect = staticMedalSpecialSkill.getSkillEffect().get(0);//技能效果值
                    if (skillEffect > 0 && specialSkillId == MedalConst.PEACEKEEPING_FORCES) {
                        //触发 维和部队特技  保护罩时间增加skillEffect，多个将领佩戴可累计，每日可生效1次
                        int now = TimeHelper.getCurrentSecond();
                        Effect effect = player.getEffect().get(EffectConstant.PROTECT);
                        if (effect == null) {
                            player.getEffect().put(EffectConstant.PROTECT,
                                    effect = new Effect(EffectConstant.PROTECT, 0, now + skillEffect));
                        } else {
                            int oldEndTime = effect.getEndTime();
                            if (oldEndTime < now) { // 已经过期
                                effect.setEndTime(now + skillEffect);
                            } else {
                                effect.setEndTime(oldEndTime + skillEffect);
                            }
                        }
                        syncBuffRs(player, effect);
                        LogUtil.debug("勋章特技-维和部队触发 角色id:", player.roleId, ", 将领id:", force.id, ", 罩子结束时间:",
                                effect.getEndTime());
                        map.put(player.roleId, true);
                    }
                }
            }
        }
        //记录玩家触发
        for (long roleId : map.keySet()) {
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                continue;
            }
            player.setPeacekeepingForcesNum(1);
        }
    }

    /**
     * @param fighter
     * @return double  加成的倍数
     * @Title: aSurpriseAttackOnTheBanditArmy
     * @Description: 勋章特技效果  【奇袭匪军】 战斗结束后执行
     */
    public double aSurpriseAttackOnTheBanditArmy(Fighter fighter) {
        //判断是否是玩家
        if (fighter.roleType != Constant.Role.PLAYER) {
            return 1.00;
        }

        if (fighter.getForces().size() > 1) {//不是单将领出征
            return 1.00;
        }

        Player player = checkForces(fighter.getForces().get(0).ownerId);
        if (player == null) {
            return 1.00;
        }

        //根据将领id  查询是否有特技
        int specialSkillId = getHeroSpecialSkill(player, fighter.getForces().get(0).id, MedalConst.HERO_MEDAL_INDEX_0);
        if (specialSkillId > 0) {//有特技
            //获取特技效果值
            StaticMedalSpecialSkill staticMedalSpecialSkill = StaticMedalDataMgr.getSpecialSkillById(specialSkillId);
            if (staticMedalSpecialSkill != null && !CheckNull.isEmpty(staticMedalSpecialSkill.getSkillEffect())) {
                int skillEffect = staticMedalSpecialSkill.getSkillEffect().get(0);//技能效果值
                if (skillEffect >= MedalConst.MEDAL_CONFIG_PERCENTAGE
                        && specialSkillId == MedalConst.A_SURPRISE_ATTACK_ON_THE_BANDIT_ARMY) {
                    //触发 奇袭匪军特技  出征杀死匪军获得的资源1.5倍
                    LogUtil.debug("勋章特技-奇袭匪军触发 角色id:", player.roleId, ", 将领id:", fighter.getForces().get(0).id,
                            ", 加成倍数:", skillEffect / MedalConst.MEDAL_CONFIG_PERCENTAGE);
                    return skillEffect / MedalConst.MEDAL_CONFIG_PERCENTAGE;
                }
            }
        }
        return 1.00;
    }

    /**
     * @param attacker
     * @param defender
     * @return int
     * @Title: militaryMeritIsProminent
     * @Description: 勋章特技效果   【军功显赫】  战斗结束后执行
     */
    public void militaryMeritIsProminent(Fighter attacker, Fighter defender,
                                         HashMap<Long, Map<Integer, Integer>> exploitAwardMap) {
        militaryMeritIsProminent(attacker, exploitAwardMap);
        militaryMeritIsProminent(defender, exploitAwardMap);
    }

    public void militaryMeritIsProminent(Fighter fighter, HashMap<Long, Map<Integer, Integer>> exploitAwardMap) {
        //判断是否是玩家
        if (fighter.roleType != Constant.Role.PLAYER) {
            return;
        }
        int now = TimeHelper.getCurrentSecond();
        //记录玩家是否触发  key 玩家id
        Map<Long, Boolean> map = new HashMap<Long, Boolean>();
        //军功显赫
        for (Force force : fighter.getForces()) {
            Player player = checkForces(force.ownerId);
            if (player == null) {
                continue;
            }

            //根据将领id  查询是否有特技
            int heroId = force.id;
            Long roleId = player.roleId;
            Map<Integer, Integer> exploitAward = exploitAwardMap.get(roleId);
            if (CheckNull.isNull(exploitAward)) {
                exploitAward = new HashMap<>();
                exploitAwardMap.put(roleId, exploitAward);
            }
            Medal medal = getHeroMedalByHeroIdAndIndex(player, heroId,
                    MedalConst.HERO_MEDAL_INDEX_0);
            int specialSkillId = getHeroSpecialSkill(player, heroId, MedalConst.HERO_MEDAL_INDEX_0);
            if (specialSkillId > 0 && !CheckNull.isNull(medal) && now - medal.getLastTime() >= TimeHelper.HALF_HOUR_S) {//有特技
                //获取特技效果值
                StaticMedalSpecialSkill staticMedalSpecialSkill = StaticMedalDataMgr
                        .getSpecialSkillById(specialSkillId);
                if (staticMedalSpecialSkill != null && !CheckNull.isEmpty(staticMedalSpecialSkill.getSkillEffect())) {
                    int skillEffect = staticMedalSpecialSkill.getSkillEffect().get(0);//技能效果值
                    if (skillEffect > 0 && specialSkillId == MedalConst.MILITARY_MERIT_IS_PROMINENT) {
                        //触发 军功显赫特技  有击杀时默认获得10万威望，半小时生效1次
                        if (force.killed > 0) {//有击杀
                            int exploit = exploitAward.getOrDefault(heroId, 0);
                            //增加军功
                            rewardDataManager.addAward(player, AwardType.MONEY, AwardType.Money.EXPLOIT, skillEffect,
                                    AwardFrom.MILITARY_MERIT_IS_PROMINENT);
                            LogUtil.debug("勋章特技-军功显赫触发 角色id:", roleId, ", 将领id:", heroId, ", 增加的军功:", skillEffect);
                            map.put(roleId, true);
                            exploitAward.put(heroId, exploit + skillEffect);

                            // 更新勋章触发时间
                            medal.setLastTime(now);
                        }
                    }
                }
            }
        }
    }

    /**
     * @param player
     * @param heroId
     * @return double 返回的加成值
     * @Title: logisticService
     * @Description: 勋章特技效果   【后勤保障】  计算采集资源的时候执行
     */
    public double logisticService(Player player, int heroId) {
        //判断是否解锁
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, MedalConst.MEDAL_SYS_LOCK)) {
            return 0.0;
        }
        //根据将领id  查询是否有特技
        int specialSkillId = getHeroSpecialSkill(player, heroId, MedalConst.HERO_MEDAL_INDEX_0);
        if (specialSkillId > 0) {//有特技
            //获取特技效果值
            StaticMedalSpecialSkill staticMedalSpecialSkill = StaticMedalDataMgr.getSpecialSkillById(specialSkillId);
            if (staticMedalSpecialSkill != null && !CheckNull.isEmpty(staticMedalSpecialSkill.getSkillEffect())) {
                int skillEffect = staticMedalSpecialSkill.getSkillEffect().get(0);//技能效果值
                if (skillEffect > 0 && specialSkillId == MedalConst.LOGISTIC_SERVICE) {
                    //触发 后勤保障特技  采集资源量增加+skillEffect%
                    LogUtil.debug("勋章特技-后勤保障触发 角色id:", player.roleId, ", 将领id:", heroId, ", 采集加成:", skillEffect);
                    return skillEffect / MedalConst.MEDAL_CONFIG_PERCENTAGE;
                }
            }
        }
        return 0.0;
    }

    /**
     * 采集铀矿资源量加成
     *
     * @param player 玩家对象
     * @param heroId 将领Id
     * @return 加成
     */
    public double getUraniumCollectEffect(Player player, int heroId) {
        int specialSkillId = getHeroSpecialSkill(player, heroId, MedalConst.HERO_MEDAL_INDEX_1);
        if (specialSkillId > 0 && specialSkillId == MedalConst.INTENSIFY_LOGISTIC_SERVICE) {
            StaticMedalSpecialSkill sMedalSpecialSkill = StaticMedalDataMgr.getSpecialSkillById(specialSkillId);
            if (sMedalSpecialSkill != null && !CheckNull.isEmpty(sMedalSpecialSkill.getSkillEffect())) {
                int skillEffect = sMedalSpecialSkill.getSkillEffect().get(0);//技能效果值
                if (skillEffect > 0) {
                    return skillEffect / Constant.TEN_THROUSAND;
                }
            }
        }
        return 0;
    }

    /**
     * 勋章特技效果   【闪击奇兵】  对打的时候执行    判断进攻方是否有该特技
     *
     * @param player 玩家对象
     * @param heroId 将领对象
     * @return 是否激活了闪击奇兵特技
     */
    public boolean blitzCuriousSoldier(Player player, int heroId) {
        //玩家不存在
        if (player == null) {
            return false;
        }
        // 判断是否解锁
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, MedalConst.MEDAL_SYS_LOCK)) {
            return false;
        }
        // 根据将领id  查询是否有特技
        int specialSkillId = getHeroSpecialSkill(player, heroId, MedalConst.HERO_MEDAL_INDEX_0);
        // 有特技
        if (specialSkillId > 0) {
            // 获取特技效果值
            StaticMedalSpecialSkill staticMedalSpecialSkill = StaticMedalDataMgr.getSpecialSkillById(specialSkillId);
            if (staticMedalSpecialSkill != null && !CheckNull.isEmpty(staticMedalSpecialSkill.getSkillEffect())) {
                // 技能效果值
                int skillEffect = staticMedalSpecialSkill.getSkillEffect().get(0);
                if (skillEffect > 0 && specialSkillId == MedalConst.BLITZ_CURIOUS_SOLDIER) {
                    // 触发闪击奇兵特技  在营地战（打玩家）和  据点战（打城）时， 据守属性可在进攻时生效   【也就是进攻方有该特技，攻击 = 攻击力+攻坚+据守】
                    LogUtil.debug("勋章特技-闪击奇兵触发 角色id:", player.roleId, ", 将领id:", heroId);
                    return true;
                }
            }
        }
        int redSpecialSkill = getHeroSpecialSkill(player, heroId, MedalConst.HERO_MEDAL_INDEX_1);
        if (redSpecialSkill == MedalConst.INTENSIFY_ANGEL_IN_WHITE) {
            // 获取特技效果值
            LogUtil.debug("强化勋章特技-强化白衣天使 角色id:", player.roleId, ", 将领id:", heroId);
            return true;
        }
        return false;
    }

    /**
     * 勋章特技效果   【铜墙铁壁】  对打的时候执行    判断防守方是否有该特技
     * @param player    玩家对象
     * @param heroId 将领对象
     * @return 是否激活了铜墙铁壁特技
     */
    public boolean ironBastions(Player player, int heroId) {
        // 玩家不存在
        if (player == null) {
            return false;
        }
        // 判断是否解锁
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, MedalConst.MEDAL_SYS_LOCK)) {
            return false;
        }
        // 根据将领id  查询是否有特技
        int specialSkillId = getHeroSpecialSkill(player, heroId, MedalConst.HERO_MEDAL_INDEX_0);
        // 有特技
        if (specialSkillId > 0) {
            // 获取特技效果值
            StaticMedalSpecialSkill staticMedalSpecialSkill = StaticMedalDataMgr.getSpecialSkillById(specialSkillId);
            if (staticMedalSpecialSkill != null && !CheckNull.isEmpty(staticMedalSpecialSkill.getSkillEffect())) {
                int skillEffect = staticMedalSpecialSkill.getSkillEffect().get(0);//技能效果值
                if (skillEffect > 0 && specialSkillId == MedalConst.IRON_BASTIONS) {
                    // 触发 铜墙铁壁特技     在营地战（打玩家）和  据点战（打城）时，攻坚属性可在防守时生效    【也就是防守方有该特技，防御 = 防御力+据守+攻坚】
                    LogUtil.debug("勋章特技-铜墙铁壁触发 角色id:", player.roleId, ", 将领id:", heroId);
                    return true;
                }
            }
        }
        int redSpecialSkill = getHeroSpecialSkill(player, heroId, MedalConst.HERO_MEDAL_INDEX_1);
        if (redSpecialSkill == MedalConst.INTENSIFY_SUSTAIN_THE_WAR_BY_MEANS_OF_WAR) {
            // 获取特技效果值
            LogUtil.debug("强化勋章特技-强化以战养战 角色id:", player.roleId, ", 将领id:", heroId);
            return true;
        }
        return false;
    }

    /**
     * 同步buff
     *
     * @param player
     * @param effects
     */
    public void syncBuffRs(Player player, Effect... effects) {
        if (player.isLogin && player.ctx != null && effects != null && effects.length > 0) {
            SyncBuffRs.Builder b = SyncBuffRs.newBuilder();
            for (Effect e : effects) {
                b.addEffect(PbHelper.createEffectPb(e));
            }
            // 推送
            Base.Builder msg = PbHelper.createSynBase(SyncBuffRs.EXT_FIELD_NUMBER, SyncBuffRs.ext, b.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }

    }

    /**
     * @param roleId
     * @return Player
     * @Title: checkForces
     * @Description: 特技效果执行  校验Forces
     */
    public Player checkForces(long roleId) {
        Player player = playerDataManager.getPlayer(roleId);
        if (player == null) {
            return null;
        }
        //判断是否解锁
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, MedalConst.MEDAL_SYS_LOCK)) {
            return null;
        }
        return player;
    }

    /**
     * 获取指定将领, 指定位置的勋章特技
     *
     * @param player 玩家对象
     * @param heroId 将领Id
     * @param index  勋章位置
     * @return 勋章特技
     */
    public int getHeroSpecialSkill(Player player, int heroId, int index) {
        Medal medal = getHeroMedalByHeroIdAndIndex(player, heroId, index);
        if (medal != null && medal.getSpecialSkillId() != null && medal.getSpecialSkillId() > 0) {
            if (index == MedalConst.HERO_MEDAL_INDEX_1 && medal instanceof RedMedal) {
                RedMedal redMedal = (RedMedal) medal;
                // 红色勋章特技未激活
                if (!redMedal.isSpecialSkillUnLock()) {
                    return 0;
                }
            }
            return medal.getSpecialSkillId();

        }
        return 0;
    }

    public static void main(String[] args) {
        System.out.println((int) (1000000 * (20 / 100.0)));
    }

    /***************************  勋章特技效果 end  *********************************/
}
