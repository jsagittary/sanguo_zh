package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.util.random.HeroSearchRandom;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author TanDonghai
 * @ClassName StaticHeroDataMgr.java
 * @Description 武将相关
 * @date 创建时间：2017年3月11日 下午4:28:40
 */
public class StaticHeroDataMgr {

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    // key:heroId
    private static Map<Integer, StaticHero> heroMap;

    // key:heroId_quality
    private static Map<String, StaticHero> heroQualityMap;

    private static Map<Integer, StaticHeroBreak> heroBreakMap;

    // Map<quality, Map<heroLv, exper>>
    private static Map<Integer, Map<Integer, Integer>> levelMap;

    // 良将寻访配置信息
    private static Map<Integer, StaticHeroSearch> heroSearchMap;

    // 英雄洗髓总属性的配置表
    private static List<StaticResetTotal> resetTotalList;
    // 技能
    private static Map<Integer, StaticSkill> skillMap;
    // 将领授勋, key:cnt
    private static Map<Integer, StaticHeroDecorated> heroDecoratedMap;
    // 将领觉醒, 进化配置, key: s_hero表中的evolveGroup字段
    private static Map<Integer, List<StaticHeroEvolve>> heroEvolveGroupMap;
    //赛季将领神职, KEY0:heroId, KEY1:cgyStage, KEY2: cgyLv
    private static Map<Integer, TreeMap<Integer, TreeMap<Integer, StaticHeroClergy>>> clergyMap = new HashMap<>();
    //赛季将领技能, KEY0:heroId, KEY1:技能ID,  KEY2:技能等级
    private static Map<Integer, Map<Integer, TreeMap<Integer, StaticHeroSeasonSkill>>> heroSkillMap = new HashMap<>();
    //赛季将领列表, KEY:heroId
    private static Map<Integer, StaticHeroSeason> seasonHeroMap = new HashMap<>();


    public static void init() {
        Map<Integer, StaticHero> heroMap = staticDataDao.selectHeroMap();
        Map<String, StaticHero> heroQualityMap = new HashMap<>();
        StaticHeroDataMgr.heroMap = heroMap;
        for (StaticHero hero : heroMap.values()) {
            heroQualityMap.put(hero.getHeroType() + "_" + hero.getQuality(), hero);
        }
        StaticHeroDataMgr.heroQualityMap = heroQualityMap;

        StaticHeroDataMgr.heroBreakMap = staticDataDao.selectHeroBreakMap();
        StaticHeroDataMgr.resetTotalList = staticDataDao.selectRestTotal();
        initHeroLevel();
        skillMap = staticDataDao.selectSkill();
        StaticHeroDataMgr.heroDecoratedMap = staticDataDao.selectHeroDecoratedMap();
        List<StaticHeroEvolve> heroEvolves = staticDataDao.selectHeroEvolveList();
        StaticHeroDataMgr.heroEvolveGroupMap = heroEvolves.stream().collect(Collectors.groupingBy(StaticHeroEvolve::getGroup));

        //初始化赛季英雄
        initSessionHero();
        //初始化英雄神职配置
        List<StaticHeroClergy> heroClergyList = staticDataDao.selectHeroClergy();
        initHeroClergy(heroClergyList);
        //初始化赛季英雄技能配置
        initSessHeroSkill();

    }

    private static void initSessHeroSkill() {
        List<StaticHeroSeasonSkill> heroSkills = staticDataDao.selectHeroSeasonSkill();
        Objects.requireNonNull(heroSkills);
        heroSkillMap.clear();
        for (StaticHeroSeasonSkill heroSkill : heroSkills) {
            Map<Integer, TreeMap<Integer, StaticHeroSeasonSkill>> skillMap0 = heroSkillMap.computeIfAbsent(heroSkill.getHeroId(), t -> new HashMap<>());
            TreeMap<Integer, StaticHeroSeasonSkill> lvMap0 = skillMap0.computeIfAbsent(heroSkill.getSkillId(), t -> new TreeMap<>());
            lvMap0.put(heroSkill.getSkillLv(), heroSkill);
        }
    }

    /**
     * 初始化英雄神职配置
     *
     * @param heroClergyList 数据库查询
     */
    private static void initHeroClergy(List<StaticHeroClergy> heroClergyList) {
        Objects.requireNonNull(heroClergyList);
        clergyMap.clear();
        for (StaticHeroClergy cgy : heroClergyList) {
            TreeMap<Integer, TreeMap<Integer, StaticHeroClergy>> clergylevelMap = clergyMap.computeIfAbsent(cgy.getHeroId(), t -> new TreeMap<>());
            TreeMap<Integer, StaticHeroClergy> clergyStepMap = clergylevelMap.computeIfAbsent(cgy.getStage(), t -> new TreeMap<>());
            clergyStepMap.put(cgy.getLevel(), cgy);
        }
    }

    private static void initSessionHero() {
        Map<Integer, StaticHeroSeason> sessHeroMap0 = staticDataDao.selectSeasonHeroMap();
        Objects.requireNonNull(sessHeroMap0);
        seasonHeroMap.clear();
        seasonHeroMap.putAll(sessHeroMap0);
    }

    private static void initHeroLevel() {
        Map<Integer, Map<Integer, Integer>> levelMap = new HashMap<>();
        List<StaticHeroLv> heroLvList = staticDataDao.selectHeroLvList();
        Map<Integer, Integer> qualityMap;
        for (StaticHeroLv heroLv : heroLvList) {
            qualityMap = levelMap.computeIfAbsent(heroLv.getQuality(), k -> new HashMap<>());

            qualityMap.put(heroLv.getLevel(), heroLv.getExp());
        }
        StaticHeroDataMgr.levelMap = levelMap;

        Map<Integer, StaticHeroSearch> heroSearchMap = staticDataDao.selectHeroSearchMap();
        StaticHeroDataMgr.heroSearchMap = heroSearchMap;
        HeroSearchRandom.init(heroSearchMap);
    }

    public static Map<Integer, StaticHero> getHeroMap() {
        return heroMap;
    }

    public static StaticHero getHeroByHeroIdAndQuality(int heroId, int quality) {
        return heroQualityMap.get(heroId + "_" + quality);
    }

    public static Map<Integer, Map<Integer, Integer>> getLevelMap() {
        return levelMap;
    }

    public static StaticHeroBreak getHeroBreak(int quality) {
        return heroBreakMap.get(quality);
    }

    public static List<StaticHeroEvolve> getHeroEvolve(int group) {
        return heroEvolveGroupMap.get(group);
    }

    /**
     * 根据将领品质和等级获取升到本级的经验，如果该级不存在，返回0
     *
     * @param quality
     * @param heroLv
     * @return
     */
    public static int getExperByQuality(int quality, int heroLv) {
        Map<Integer, Integer> qualityMap = levelMap.get(quality);
        if (null == qualityMap) {
            LogUtil.error("未配置的武将品质, quality:" + quality);
            return 0;
        }

        Integer exper = qualityMap.get(heroLv);
        return null == exper ? 0 : exper;
    }

    // public static void addHeroExp(Hero hero, int exp) {
    // int lv = hero.getLevel();
    // int need = 0;
    //
    // StaticHero staticHero = getHeroMap().get(hero.getHeroId());
    // if (null == staticHero) {
    // LogUtil.error("将领加经验，heroId为配置, heroId:", hero.getHeroId());
    // return;
    // }
    //
    // while (exp > 0) {
    // lv++;
    // need = getExperByQuality(staticHero.getQuality(), lv);
    // if (need <= 0) {
    // break;
    // }
    //
    // if (hero.getExp() + exp < need) {
    // hero.setExp(hero.getExp() + exp);
    // exp = 0;
    // } else {
    // exp -= need;
    // hero.setLevel(lv);
    // }
    // }
    // }

    public static Map<Integer, StaticHeroSearch> getHeroSearchMap() {
        return heroSearchMap;
    }

    public static List<StaticResetTotal> getResetTotalList() {
        return StaticHeroDataMgr.resetTotalList;
    }

    /**
     * 根据距离上限差值,获取值总属性配置信息
     *
     * @return
     */
    public static StaticResetTotal getResetTotalByLm(int limit) {
        if (StaticHeroDataMgr.resetTotalList == null) {
            return null;
        }
        for (StaticResetTotal rt : StaticHeroDataMgr.resetTotalList) {
            if (rt.getLowerlimit() <= limit && limit <= rt.getUpperlimit()) {
                return rt;
            }
        }
        return null;
    }

    public static StaticSkill getSkillMapById(int id) {
        return skillMap.get(id);
    }

    public static Map<Integer, StaticHeroDecorated> getHeroDecoratedMap() {
        return heroDecoratedMap;
    }

    public static TreeMap<Integer, TreeMap<Integer, StaticHeroClergy>> getHeroClergyMap(int heroId) {
        return clergyMap.get(heroId);
    }

    public static StaticHeroClergy getHeroClergy(int heroId, int stage, int lv) {
        TreeMap<Integer, TreeMap<Integer, StaticHeroClergy>> stageMap = clergyMap.get(heroId);
        TreeMap<Integer, StaticHeroClergy> lvMap = stageMap != null ? stageMap.get(stage) : null;
        return lvMap != null ? lvMap.get(lv) : null;
    }

    public static Map<Integer, StaticHeroSeason> getSeasonHeroMap() {
        return seasonHeroMap;
    }

    public static StaticHeroSeason getSeasonHero(int heroId) {
        return seasonHeroMap.get(heroId);
    }

    public static TreeMap<Integer, StaticHeroSeasonSkill> getHeroSkillMap(int heroId, int skillId) {
        Map<Integer, TreeMap<Integer, StaticHeroSeasonSkill>> skillMap0 = heroSkillMap.get(heroId);
        if (Objects.nonNull(skillMap0)) {
            return skillMap0.get(skillId);
        }
        return null;
    }

    public static StaticHeroSeasonSkill getHeroSkill(int heroId, int skillId, int lv) {
        Map<Integer, TreeMap<Integer, StaticHeroSeasonSkill>> skillMap0 = heroSkillMap.get(heroId);
        TreeMap<Integer, StaticHeroSeasonSkill> lvMap = skillMap0 != null ? skillMap0.get(skillId) : null;
        return lvMap != null ? lvMap.get(lv) : null;
    }

    public static Map<Integer, TreeMap<Integer, StaticHeroSeasonSkill>> getHeroSkill(int heroId){
        return heroSkillMap.get(heroId);
    }
}
